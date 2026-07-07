# BC-05 Rating (Task 1, standalone)

We built this to let riders leave feedback on a completed ride: a 1 to 5 score for the
vehicle, a 1 to 5 score for the provider, and an optional comment. We enforce the
one-rating-per-booking invariant and require a booking to be `COMPLETED` before it can be
rated. Since we run standalone in Task 1, the booking it validates against comes from a
mock booking client, not a real call to BC-03.

Port: **8085**. Swagger UI: `/swagger-ui.html`. Server-rendered UI: `/ui`. H2 console: `/h2-console`.

## How we covered the DDD design (Assignment 04)

| Building block | Class | Notes |
|---|---|---|
| Aggregate Root / Entity | `Rating` | `id`, `userId`, `createdAt`, embedded `ratingTarget`, embedded `review`. We only ever create it via the static factory `Rating.create(...)`, it's immutable once persisted, there is no update path at all. |
| Value Object | `Review` | `vehicleScore`, `providerScore` (plain `Integer`, 1 to 5), `comment` (nullable). |
| Value Object | `RatingTarget` | `vehicleId`, `providerId`, `bookingId`. The triplet identifying what and why is being rated. |
| Domain Service | `RatingSubmissionService` | `submitRating(...)` validates both scores are 1 to 5, looks up the booking via `BookingClient`, checks it's `COMPLETED`, checks no rating already exists for that booking, then creates and saves. |
| Domain Service | `RatingQueryService` | `getById`, `getVehicleRatings`, `getProviderRatings`, `getRatingForBooking`, and `getAverageScore(vehicleId)`. Note this only ever averages vehicle scores; we never built a provider-level average anywhere in this codebase. |
| Repository | `RatingRepository` | `findByRatingTarget_BookingId`, `findByRatingTarget_VehicleId`, `findByRatingTarget_ProviderId`, `existsByRatingTarget_BookingId`. |
| Domain Event | `RatingSubmitted` | Logged when `RatingSubmissionService` successfully persists a new `Rating`. |

One deliberate simplification versus our Assignment 04 design worth calling out: we had
sketched a dedicated `Score` value object (`value:Integer(1..5)` with a validating static
factory) in the design. We ended up validating the 1 to 5 range directly in
`RatingSubmissionService.validateScore(...)` and storing plain `Integer` fields on
`Review` instead. Same invariant, one fewer wrapper class.

Per our Assignment 03 context map, Rating is downstream of three upstream contexts:
Identity & Access (author identity, via `userId`, no local lookup needed), Fleet
Management (vehicle/provider display data, OHS/PL to ACL), and Booking (Customer/Supplier
to Conformist: Rating uses Booking's own `bookingId` and completion status directly, with
no translation). In Task 1, we simulate the Booking side of that relationship with
`MockBookingClient`, seeded with three fake bookings.

## Requirements we covered (Assignment 02)

| Req | Description | Covered by |
|---|---|---|
| R14 | Rating submission (vehicle, provider, and booking) | `POST /api/ratings` calling `RatingSubmissionService.submitRating` |
| R15 (partial) | Rating history/reporting | `GET /api/ratings/vehicle/{id}`, `GET /api/ratings/provider/{id}`, `GET /api/ratings/vehicle/{id}/average` |

## File-by-file

### `RatingApplication.java`
Plain `@SpringBootApplication` entry point (Task 1 adds no discovery/Feign annotations).

### `domain/model/`
- **`Rating.java`**: `@Entity`. `protected Rating()` for JPA, plus a `private` constructor
  and the public static `create(userId, ratingTarget, review)` factory that stamps
  `createdAt`. We didn't add any setters, so once created a rating cannot be edited,
  matching "once submitted, a Rating is immutable" from our Assignment 04 design.
- **`RatingTarget.java`**, **`Review.java`**: `@Embeddable` value objects.

### `domain/event/RatingSubmitted.java`
Plain record, logged by `RatingSubmissionService`.

### `domain/exception/`
`BookingNotFoundException`/`RatingNotFoundException` map to 404,
`BookingNotCompletedException`/`DuplicateRatingException` map to 409,
`InvalidScoreException` maps to 400.

### `domain/repository/RatingRepository.java`
`JpaRepository<Rating, Long>` plus the four `findByRatingTarget_*`/`existsBy...` methods
listed above.

### `domain/service/`
- **`RatingSubmissionService.java`**: the invariant enforcer. Score range (1 to 5), then
  booking exists, then booking is `COMPLETED`, then no existing rating for that booking,
  then build `RatingTarget`/`Review`, then `Rating.create(...)`, then save.
- **`RatingQueryService.java`**: read-only queries, all `@Transactional(readOnly = true)`.
  `getAverageScore(vehicleId)` averages `review.vehicleScore` across that vehicle's
  ratings, returning `0.0` if there are none.

### `infrastructure/booking/`
- **`BookingClient.java`**: the interface, `getBooking(bookingId): Optional<BookingView>`.
- **`BookingView.java`**: `record(id, userId, vehicleId, providerId, status)` with
  `isCompleted()`. This context's own lightweight projection of a booking, the ACL.
- **`MockBookingClient.java`**: the only implementation in Task 1. Three hardcoded
  bookings (two `COMPLETED`, one `ACTIVE`), so we can exercise both the happy path and the
  "booking not completed" rejection path without BC-03 running.

### `api/`
- **`RatingController.java`** (`/api/ratings`): `POST` (submit), `GET /{id}`,
  `GET /booking/{bookingId}`, `GET /vehicle/{vehicleId}`, `GET /provider/{providerId}`,
  `GET /vehicle/{vehicleId}/average`.
- **`api/dto/CreateRatingRequest.java`**: `bookingId`, `userId`, `vehicleScore`,
  `providerScore`, `comment`. We deliberately left `vehicleId`/`providerId` out of this
  request; we resolve them server-side from the referenced booking instead.
- **`api/dto/RatingDto.java`**, **`api/dto/AverageScoreResponse.java`**: response shapes.
- **`GlobalExceptionHandler.java`**: maps the five domain exceptions above to their HTTP
  status codes.

### `api/ui/`
- **`RatingUiController.java`**: Thymeleaf pages at `/ui`, a rating submission form
  (`/rate`), a vehicle-ratings view, a full ratings list, and a lookup page.
- **`support/Stars.java`**: renders a 1 to 5 integer score as a `"★★★☆☆"` string for the
  UI. Purely cosmetic, not referenced by any domain or REST code.

## How to run

From `Assignment05/Task1`:
```bash
./mvnw -pl bc05-rating spring-boot:run
```
or via `./start.sh` / `start.bat`.

- Swagger UI: http://localhost:8085/swagger-ui.html
- Browser UI: http://localhost:8085/ui
- H2 console: http://localhost:8085/h2-console (`jdbc:h2:mem:ratingdb`)

## How to test

We didn't write automated tests for this module, so we verify manually.
`MockBookingClient` seeds bookings `5001` (`COMPLETED`, userId 1, vehicleId 1, providerId
1), `5002` (`COMPLETED`, userId 1, vehicleId 2, providerId 1), and `5003` (`ACTIVE`, userId
2, vehicleId 3, providerId 2).

Via Swagger UI:
1. `POST /api/ratings` with `bookingId:5001, userId:1, vehicleScore:5, providerScore:4`.
   We expect `201` with a `RatingDto` where `vehicleId`/`providerId` were filled in
   automatically from the mock booking (1 and 1).
2. `POST /api/ratings` again with the same `bookingId:5001`. We expect `409`
   (`DuplicateRatingException`).
3. `POST /api/ratings` with `bookingId:5003` (the `ACTIVE` one). We expect `409`
   (`BookingNotCompletedException`).
4. `POST /api/ratings` with `bookingId:9999` (doesn't exist). We expect `404`
   (`BookingNotFoundException`).
5. `POST /api/ratings` with `vehicleScore:7`. We expect `400` (`InvalidScoreException`).
6. `GET /api/ratings/vehicle/1/average`. After step 1, we expect `averageScore:5.0, count:1`.
7. We confirm there is no `GET /api/ratings/provider/{id}/average` endpoint; only vehicle
   averages exist, matching the domain service above.

Via the browser UI (http://localhost:8085/ui/rate): we submit a rating for booking 5002
and confirm the star rendering and the vehicle/provider rating list pages reflect it.
