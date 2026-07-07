# BC-03 Booking (Task 1, standalone)

We built this as the operational heart of the platform: it owns the entire ride lifecycle
(search, create, active ride, end or cancel), enforces booking invariants, computes ride
cost, and triggers payment. Since we run every context standalone in Task 1, all data it
would normally fetch from Identity & Access, Fleet Management, and Payment is supplied by
mock clients instead of real HTTP calls.

Port: **8083**. Swagger UI: `/swagger-ui.html`. Server-rendered UI: `/ui`. H2 console: `/h2-console`.

## How we covered the DDD design (Assignment 04)

| Building block | Class | Notes |
|---|---|---|
| Aggregate Root / Entity | `Booking` | `id`, `userId`, start/end lat-lon, start/end time, embedded `vehicleSnapshot`, embedded `rideSummary`, `status`. `complete(...)` and `cancel()` are its only mutators. |
| Value Object | `VehicleSnapshot` | `vehicleId`, `type`, `pricePerUnit`, `billingModel`. We capture this at booking time so a later price change on the vehicle never retroactively affects an in-progress or historical ride. |
| Value Object | `RideSummary` | `distanceKm`, `totalCost`. Both null until the ride completes. |
| Value Object | `RideLocation` | Plain `record(latitude, longitude)`. We use this as a method-parameter type for start/end coordinates, not a JPA `@Embeddable`, since the aggregate needs two independent pairs and we store those as four plain columns instead. |
| Value Object | `TimeInterval` | Plain `record(start, end)`. Same rationale, we use it in `CostCalculationService`'s signature rather than as a persisted field. |
| Enum | `BookingStatus` | `ACTIVE`, `COMPLETED`, `CANCELLED`. |
| Domain Service | `BookingService` | `createBooking`, `endBooking`, `cancelBooking`. This is the orchestrator for the whole lifecycle. |
| Domain Service | `CostCalculationService` | `computeDistanceKm` (Haversine) and `computeCost` (rate times distance or time depending on `billingModel`). |
| Domain Service | `RestrictionValidator` | Checks the rider's age and the vehicle's `UsageRestrictions` before we allow a booking. |
| Domain Service | `VehicleSearchService` | Wraps `VehicleClient.searchAvailable` for the standalone search-and-book UI page. |
| Repository | `BookingRepository` | `findByUserId`, `findByUserIdAndStatus`, `findByVehicleIdAndStatus`, `findByVehicleId`. |
| Domain Events | `BookingCreated`, `BookingCompleted`, `BookingCancelled`, `PaymentTriggered` | We log these at the point `BookingService` completes each transition. |

One deliberate deviation from our Assignment 04 design worth calling out: we never put
`paymentMethod` on the `Booking` aggregate. We modeled cost and payment as downstream
concerns in the design, and that's exactly how we implemented it too: payment method is
only ever a parameter to `endBooking`, passed straight through to the Payment context. We
never persist it on `Booking` itself.

Per our Assignment 03 context map, Booking is simultaneously downstream of Identity &
Access and Fleet Management (both OHS/PL, wrapped in an ACL here) and upstream of Payment
and Rating (Customer/Supplier, with Payment and Rating as Conformist consumers of
Booking's `bookingId`/`totalCost`/`status`). In Task 1, we simulate the OHS calls to
Identity and Fleet: `UserClient`/`VehicleClient` are interfaces with only a mock
implementation, so the ACL exists in the code (the interface boundary) but talks to
fabricated data, not a real service.

## Requirements we covered (Assignment 02)

| Req | Description | Covered by |
|---|---|---|
| R09 | Vehicle search | `VehicleSearchService` (used by the standalone `/ui/search-book` page; the REST API instead exposes cost/availability implicitly through booking creation) |
| R11 | Booking creation | `POST /api/bookings` calling `BookingService.createBooking` |
| R12 | Ride completion and cost calculation | `POST /api/bookings/{id}/end` calling `BookingService.endBooking` plus `CostCalculationService` |
| R13 | Payment processing (trigger) | `endBooking` calls `PaymentClient.charge(...)` after computing cost |
| R15 (partial) | Booking history | `GET /api/bookings/user/{userId}`, `GET /api/bookings/vehicle/{vehicleId}` |

## File-by-file

### `BookingApplication.java`
Plain `@SpringBootApplication` entry point.

### `domain/model/`
- **`Booking.java`**: `@Entity`. `complete(endLocation, endTime, distanceKm, totalCost)`
  transitions `ACTIVE` to `COMPLETED` and fills in `rideSummary`. `cancel()` transitions
  `ACTIVE` to `CANCELLED`. Neither throws anything itself; `BookingService` checks the
  current status before calling them.
- **`VehicleSnapshot.java`**, **`RideSummary.java`**: `@Embeddable` value objects.
- **`RideLocation.java`**, **`TimeInterval.java`**: plain records, not persisted directly.
- **`BookingStatus.java`**: enum.

### `domain/event/`
`BookingCreated`, `BookingCompleted`, `BookingCancelled`, `PaymentTriggered`: plain
records, logged (not published) by `BookingService`.

### `domain/exception/`
`BookingNotFoundException`/`UserNotFoundException`/`VehicleNotFoundException` map to 404,
`VehicleUnavailableException`/`ActiveBookingExistsException`/`InvalidBookingStateException`
map to 409, `RestrictionViolationException` maps to 400. All mapped by
`api/GlobalExceptionHandler`.

### `domain/repository/BookingRepository.java`
`JpaRepository<Booking, Long>` plus the four query methods listed above.

### `domain/service/`
- **`BookingService.java`**: the orchestrator. `createBooking(userId, vehicleId)` fetches
  the user and vehicle (via the client interfaces below), checks the vehicle is
  `AVAILABLE`, checks the user has no other active booking, runs `RestrictionValidator`,
  snapshots the vehicle into a `VehicleSnapshot`, saves, and flips the vehicle to `BOOKED`.
  `endBooking(bookingId, endLat, endLon, paymentMethod)` computes distance and cost via
  `CostCalculationService`, completes the booking, flips the vehicle back to `AVAILABLE`,
  then calls `PaymentClient.charge(...)`. `cancelBooking(bookingId)` cancels and releases
  the vehicle.
- **`CostCalculationService.java`**: Haversine distance, then cost equals `pricePerUnit`
  times distanceKm or elapsed hours, depending on billingModel.
- **`GeoMath.java`**: the actual Haversine formula, we pulled it out as a small static helper.
- **`RestrictionValidator.java`**: throws `RestrictionViolationException` if the rider's
  age or the ride would violate the vehicle's `UsageRestrictions`.
- **`VehicleSearchService.java`**: thin wrapper around `VehicleClient.searchAvailable`,
  used by the standalone UI's search page.
- **`BookingEndResult.java`**: a small `record(Booking, PaymentOutcome)` we return from
  `endBooking` so the controller can report both the booking and payment status together.

### `infrastructure/`, the ACL boundary to other contexts
- **`UserClient.java`** / **`MockUserClient.java`**: interface plus the only implementation
  in Task 1, a hardcoded in-memory map of fake users.
- **`VehicleClient.java`** / **`MockVehicleClient.java`**: same pattern for vehicles
  (location, pricing, restrictions, status).
- **`PaymentClient.java`** / **`MockPaymentClient.java`**: same pattern for charging.
  Always succeeds with a fabricated `PaymentOutcome`.
- **`UserView.java`**, **`VehicleView.java`**, **`PaymentOutcome.java`**: plain read-model
  records the client interfaces above return. This context's own lightweight projections
  of the upstream data, per the ACL pattern.
- **`SeedDataLogger.java`**: logs the mock seed data on startup for visibility.

### `api/`
- **`BookingController.java`** (`/api/bookings`): `POST` (create), `POST /{id}/end`,
  `POST /{id}/cancel`, `GET /{id}`, `GET /user/{userId}`, `GET /user/{userId}/active`,
  `GET /vehicle/{vehicleId}`.
- **`api/dto/`**: `CreateBookingRequest(userId, vehicleId)`,
  `EndBookingRequest(endLatitude, endLongitude, paymentMethod)` (payment method defaults to
  `"CARD"` if omitted or blank, see `EndBookingRequest.paymentMethodOrDefault()`),
  `BookingResponse` (flattens booking and payment outcome for the client).
- **`GlobalExceptionHandler.java`**: maps the domain exceptions listed above to HTTP status codes.

### `api/ui/`
- **`BookingUiController.java`**: Thymeleaf search, book, end, cancel, and history pages at `/ui`.
- **`VehicleIcons.java`**: maps `VehicleType` to an emoji for the UI. Purely cosmetic.

## How to run

From `Assignment05/Task1`:
```bash
./mvnw -pl bc03-booking spring-boot:run
```
or via `./start.sh` / `start.bat`.

- Swagger UI: http://localhost:8083/swagger-ui.html
- Browser UI: http://localhost:8083/ui
- H2 console: http://localhost:8083/h2-console (`jdbc:h2:mem:bookingdb`)

## How to test

We didn't write automated tests for this module. `MockUserClient`/`MockVehicleClient`
seed a small set of fake users/vehicles on startup (check `SeedDataLogger`'s log output
for the exact IDs to use).

Via Swagger UI:
1. `POST /api/bookings` with a seeded `userId`/`vehicleId`. We expect `201`, `status:"ACTIVE"`.
2. `POST /api/bookings` again with the same `userId` and a different vehicle. We expect
   `409` (`ActiveBookingExistsException`, only one active booking per user).
3. `POST /api/bookings/{id}/end` with `endLatitude`/`endLongitude` a few km from the start
   and `paymentMethod:"PAYPAL"`. We expect `200`, `status:"COMPLETED"`, non-zero
   `distanceKm`/`totalCost`, and a `paymentStatus` from the mock payment client.
4. `GET /api/bookings/user/{userId}`. We expect the completed booking in the list, most
   recent first.
5. We book another vehicle, then `POST /api/bookings/{id}/cancel`. We expect
   `status:"CANCELLED"` and the vehicle available again (verify via bc02, or trust the
   mock's in-memory state if running bc03 alone).
6. We try booking a vehicle whose mock `minAge` restriction the seeded user doesn't meet.
   We expect `400` (`RestrictionViolationException`).

Via the browser UI (http://localhost:8083/ui): we walk through search, book, and end ride
with a chosen payment method, then confirm the ride-detail page shows the right payment
method label. This is the exact flow behind the PayPal/card payment-method bug we fixed in
the Task 2 gateway, so it's worth double-checking here too since the underlying
`BookingService.endBooking` code path is shared.
