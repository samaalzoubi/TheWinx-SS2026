# BC-02 Fleet Management (Task 1, standalone)

We built this as the single source of truth for vehicles: registration, pricing, usage
restrictions, location, and availability status. Like Identity & Access, we made this an
upstream context with no outbound dependencies. Booking and Rating consume it, it consumes
nothing.

Port: **8082**. Swagger UI: `/swagger-ui.html`. Server-rendered UI: `/ui`. H2 console: `/h2-console`.

## How we covered the DDD design (Assignment 04)

| Building block | Class | Notes |
|---|---|---|
| Aggregate Root / Entity | `Vehicle` | `id`, `providerId`, `type`, `description`, `status`, plus three embedded value objects. |
| Value Object | `VehicleLocation` | `latitude`, `longitude`. We replace it wholesale on each location update. |
| Value Object | `PricingPolicy` | `pricePerUnit`, `billingModel`. |
| Value Object | `UsageRestrictions` | `maxDurationMinutes`, `maxKilometers`, `minAge`, `maxPersons`, all nullable, meaning "no restriction." |
| Enum | `VehicleType` | `E_SCOOTER`, `BICYCLE`, `E_BIKE`, `E_CAR`. |
| Enum | `VehicleStatus` | `AVAILABLE`, `BOOKED`. Exactly two values, matching our Assignment 04 design (we never added `MAINTENANCE`/`UNAVAILABLE` to either the design or the code). |
| Enum | `BillingModel` | `PER_HOUR`, `PER_KILOMETER`. Again exactly two, matching the design. |
| Domain Service | `VehicleRegistrationService` | `createVehicle`, `updateVehicle`, `deleteVehicle` (rejects deletion while `BOOKED`). |
| Domain Service | `VehicleAvailabilityService` | `findAvailableNear` (in-memory Haversine distance filter, not a repository query) and `updateStatus`. |
| Domain Service | `FleetStatusService` | `getFleetOverview(providerId)`, `updateLocation`. |
| Repository | `VehicleRepository` | `findByProviderId`, `findByStatus` (plus inherited `JpaRepository` methods). |
| Domain Events | `VehicleCreated`, `VehicleDeleted`, `VehicleLocationUpdated`, `VehicleStatusUpdated` | We log these (we don't publish them to a broker) at the point each domain service completes its action. |

We never duplicate provider identity here. `Vehicle.providerId` is a bare reference to
BC-01's `ProviderAccount.id`, exactly as our Assignment 03 context map specifies: Identity
& Access exposes an Open Host Service, and every downstream context (including this one)
only ever stores the ID, never a copy of the provider's data.

## Requirements we covered (Assignment 02)

| Req | Description | Covered by |
|---|---|---|
| R05 | Vehicle management (create/edit/delete/inspect) | `VehicleController`: `POST /api/vehicles`, `PUT /api/vehicles/{id}`, `DELETE /api/vehicles/{id}`, `GET /api/vehicles/{id}` |
| R06 | Pricing management | `PricingPolicy` embedded in `CreateVehicleRequest`/`UpdateVehicleRequest` |
| R07 | Usage restrictions | `UsageRestrictions` embedded in the same requests |
| R08 | Fleet status tracking | `PATCH /api/vehicles/{id}/status`, `PATCH /api/vehicles/{id}/location` |
| R09 | Vehicle search (by location/availability) | `GET /api/vehicles/search` |
| R10 | Vehicle filtering (type, billing model, price, restrictions) | Same search endpoint's `vehicleType`/`maxPrice`/`minAge`/`maxDurationMinutes` params |

## File-by-file

### `FleetManagementApplication.java`
Plain `@SpringBootApplication` entry point.

### `domain/model/`
- **`Vehicle.java`**: `@Entity`. Constructor sets `status = AVAILABLE`. We didn't put any
  business methods on it beyond getters/setters; we enforce invariants (price > 0,
  non-negative restrictions) in the service layer (`VehicleRegistrationService`) instead.
- **`VehicleLocation.java`**, **`PricingPolicy.java`**, **`UsageRestrictions.java`**: `@Embeddable`
  value objects, each a plain data holder for its slice of the aggregate.
- **`VehicleType.java`**, **`VehicleStatus.java`**, **`BillingModel.java`**: enums.

### `domain/event/`
- **`VehicleCreated.java`**, **`VehicleDeleted.java`**, **`VehicleLocationUpdated.java`**,
  **`VehicleStatusUpdated.java`**: plain records, logged via SLF4J at the point of the
  triggering action (see the domain services below).

### `domain/exception/`
- **`VehicleNotFoundException.java`** maps to 404. **`InvalidVehicleStateException.java`**
  maps to 409 (we use this when trying to delete a `BOOKED` vehicle).

### `domain/repository/VehicleRepository.java`
`JpaRepository<Vehicle, Long>` plus `findByProviderId`/`findByStatus`.

### `domain/service/`
- **`VehicleRegistrationService.java`**: `createVehicle` validates `pricePerUnit > 0` and
  that restriction values aren't negative before saving. `updateVehicle` does a null-safe
  partial update. `deleteVehicle` throws `InvalidVehicleStateException` if the vehicle is
  currently `BOOKED`.
- **`VehicleAvailabilityService.java`**: `findAvailableNear` loads all vehicles and filters
  them in memory using a Haversine distance calculation plus optional type/price/age/
  duration filters. There's no geospatial index; we didn't need one at this scale.
  `updateStatus` flips `AVAILABLE` and `BOOKED`.
- **`FleetStatusService.java`**: `getFleetOverview(providerId)` (all vehicles for one
  provider) and `updateLocation` (GPS update).

### `api/`
- **`VehicleController.java`** (`/api/vehicles`): 8 endpoints. `POST` (create), `GET /{id}`,
  `PUT /{id}` (update), `DELETE /{id}`, `GET` (list, optional `providerId` filter),
  `GET /search` (availability search), `PATCH /{id}/status`, `PATCH /{id}/location`.
- **`api/dto/`**: `CreateVehicleRequest`, `UpdateVehicleRequest`, `UpdateLocationRequest`,
  `UpdateStatusRequest`, `VehicleResponse`. All `record`s.
- **`GlobalExceptionHandler.java`**: maps `VehicleNotFoundException` to 404,
  `InvalidVehicleStateException` to 409, bean validation/`IllegalArgumentException`/
  type mismatch/unreadable body to 400.

### `api/ui/`
- **`HomeUiController.java`**, **`FleetUiController.java`** (provider-facing fleet CRUD forms),
  **`BrowseUiController.java`** (browse and toggle status), **`SearchUiController.java`**
  (search form). Thymeleaf views at `/ui/...`, all delegating to the same domain services
  as the REST API.
- **`FleetStats.java`**: a small view-model helper (total/available/booked counts, average
  price) we compute for the fleet overview page; it's not a domain object.

### `config/FleetSeedDataLoader.java`
`CommandLineRunner` that seeds 5 demo vehicles (centered around Dortmund) for two fake
provider IDs on startup.

## How to run

From `Assignment05/Task1`:
```bash
./mvnw -pl bc02-fleet-management spring-boot:run
```
or via `./start.sh` / `start.bat` to bring up all 5 Task 1 services together.

- Swagger UI: http://localhost:8082/swagger-ui.html
- Browser UI: http://localhost:8082/ui
- H2 console: http://localhost:8082/h2-console (`jdbc:h2:mem:fleetdb`)

## How to test

We didn't write automated tests for this module, so we verify manually.

Via Swagger UI:
1. `POST /api/vehicles` with `providerId`, `type`, `pricePerUnit`, `billingModel`,
   `latitude`/`longitude`. We expect `201` with the new vehicle's `id` and `status:"AVAILABLE"`.
2. `GET /api/vehicles/search?lat=51.5136&lon=7.4653&radiusKm=5`. We expect the seeded
   Dortmund vehicles plus our new one to show up.
3. `PATCH /api/vehicles/{id}/status` with `{"status":"BOOKED"}`, then `DELETE /api/vehicles/{id}`.
   We expect `409 Conflict` since we can't delete a booked vehicle.
4. We set it back to `AVAILABLE` via `PATCH .../status`, then `DELETE` again. We expect `204`.
5. `POST /api/vehicles` with `pricePerUnit: -5`. We expect `400` since the price must be positive.

Via the browser UI (http://localhost:8082/ui): we use `/ui/provider/1/fleet` to add, edit,
and delete a vehicle through the form, and `/ui/browse` to toggle a vehicle's status and
see it disappear and reappear from `/ui/search` results depending on filters.

Via H2 console: `SELECT * FROM VEHICLES` to confirm the embedded columns
(`latitude`, `longitude`, `price_per_unit`, `billing_model`, `max_duration_minutes`, and so on)
are populated correctly for a vehicle we just created.
