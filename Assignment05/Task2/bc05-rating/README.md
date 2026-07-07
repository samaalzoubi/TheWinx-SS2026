# BC-05 Rating (Task 2, integrated)

We built this to let riders leave feedback on a completed ride. The domain model is
identical to Task 1. For Task 2 we replaced the Task 1 mock booking client with a real
Feign call to BC-03 Booking, and added a second Feign call to BC-02 Fleet Management,
because Booking's own API doesn't expose a booking's `providerId`; this context has to
resolve that itself.

Port: **8085**. Swagger UI: `/swagger-ui.html`. Server-rendered UI: `/ui`. H2 console: `/h2-console`.
Also reachable through the integrated web portal at `infra-api-gateway` (port **8080**)'s
"Rate this ride" flow.

## How we covered the DDD design (Assignment 04)

Identical to Task 1, see that README for the full table (`Rating` aggregate, immutable
after creation, with embedded `RatingTarget`/`Review`, two domain services, one
repository, one logged domain event, and the deliberate `Score`-value-object-to-plain-int
simplification versus our Assignment 04 design).

Per Assignment 03, Rating is downstream of Fleet Management (OHS/PL to ACL) and Booking
(Customer/Supplier to Conformist). In Task 2 we turned both relationships into real HTTP calls:

| Dependency | Real client | Fallback (Task 1's mock, reused) | Circuit breaker name |
|---|---|---|---|
| Booking (status + vehicleId/userId) | `BookingFeignClient` via `BookingLookupGateway` | `MockBookingClient` | `bookingClient` |
| Fleet Management (providerId for a vehicle) | `FleetFeignClient` via `FleetLookupGateway` | `PROVIDER_UNKNOWN` sentinel (`-1L`) | `fleetClient` |

`ResilientBookingClient` (the `@Primary` `BookingClient` implementation) composes both
calls: it asks `BookingLookupGateway` for the booking's status/vehicleId/userId, then asks
`FleetLookupGateway` to resolve that vehicle's `providerId`, and assembles a complete
`BookingView` from the two. It's a small but genuine example of one context's
Anti-Corruption Layer needing to talk to two upstream services to build one coherent
read model.

## Requirements we covered (Assignment 02)

Same as Task 1: R14 (rating submission), R15 (rating history/reporting), now genuinely
cross-service, and this is also where a provider's earnings/ratings view on the gateway
composes data pulled from this context.

## File-by-file

The domain layer (`domain/model/`, `domain/event/`, `domain/exception/`,
`domain/repository/`, `domain/service/`) and the REST API (`api/`, `api/ui/`) are
unchanged from Task 1, see that README for the full breakdown. What we changed:

### `infrastructure/booking/`, now a real Feign call, composed with Fleet lookup
- **`BookingFeignClient.java`**: `@FeignClient(name = "bc03-booking")`,
  `GET /api/bookings/{id}`.
- **`BookingFeignResponse.java`**: `record(id, userId, vehicleId, status)`. Note there is
  no `providerId` here, since Booking's own response doesn't carry one.
- **`BookingLookupOutcome.java`**: a sealed interface (`Found`/`NotFound`/`Unavailable`)
  distinguishing "booking genuinely doesn't exist" from "the service is unreachable," so
  the caller can tell a 404 apart from a technical failure.
- **`BookingLookupGateway.java`**: wraps `BookingFeignClient` in
  `@CircuitBreaker(name = "bookingClient")`. We treat a `FeignException.NotFound` as a
  legitimate `NotFound` outcome, it does not trip the breaker, only genuine technical
  failures do.
- **`ResilientBookingClient.java`**: `@Primary` `BookingClient` implementation. We combine
  `BookingLookupGateway`'s result with `FleetLookupGateway.resolveProviderId(vehicleId)` to
  build a complete `BookingView`, falling back to `MockBookingClient`'s fabricated data
  (still re-resolving the provider ID fresh via Fleet) if Booking itself is `Unavailable`.
- **`MockBookingClient.java`**: unchanged from Task 1, now doubling as the circuit breaker
  fallback data source.

### `infrastructure/fleet/`, new in Task 2
- **`FleetFeignClient.java`**: `@FeignClient(name = "bc02-fleet-management")`,
  `GET /api/vehicles/{id}`.
- **`FleetVehicleResponse.java`**: `record(id, providerId)`.
- **`FleetLookupGateway.java`**: wraps the Feign call in
  `@CircuitBreaker(name = "fleetClient")`. Its fallback returns the sentinel
  `PROVIDER_UNKNOWN` (`-1L`) rather than throwing, so a Fleet outage degrades a rating's
  `providerId` to "unknown" instead of blocking rating submission entirely.

### `src/main/resources/application.yml`
We add Eureka registration, an `optional:` Config Server import, and two named circuit
breakers, `bookingClient` and `fleetClient` (10-call sliding window, 50% failure
threshold, 10s open-state wait, automatic half-open transition), plus
`management.endpoints.web.exposure.include: health,info,circuitbreakers,circuitbreakerevents`.

### `pom.xml`
We add `spring-cloud-starter-netflix-eureka-client`, `spring-cloud-starter-config`,
`spring-cloud-starter-openfeign`, `spring-cloud-starter-loadbalancer`, and
`resilience4j-spring-boot3`.

### Consumers of this service in Task 2
- `infra-api-gateway`'s `RatingClient` backs the portal's "Rate this ride" form and shows
  a booking's existing rating (if any) on the dashboard/ride-detail pages.

## How to run

Standalone (Feign calls fail over to the mock/fallback data immediately):
```bash
cd Assignment05/Task2
./mvnw -pl bc05-rating spring-boot:run
```

Fully integrated (recommended, needed to exercise the real Feign paths):
```bash
cd Assignment05/Task2
./start.sh        # macOS/Linux
start.bat         # Windows
```

- Swagger UI: http://localhost:8085/swagger-ui.html
- Standalone browser UI: http://localhost:8085/ui
- Integrated portal: http://localhost:8080 ("Rate this ride" after completing a booking)
- Circuit breaker status: http://localhost:8085/actuator/circuitbreakers
- Eureka dashboard: http://localhost:8761 (confirm `BC05-RATING` is registered)
- H2 console: http://localhost:8085/h2-console

## How to test

We didn't write automated tests for this module. Task 1's manual test steps (submit,
duplicate, not-completed, bad-score checks against the `MockBookingClient` seed data)
apply directly against port 8085 when run standalone. We also test the Task 2 integration
and resilience behavior:

1. We start the full stack, complete a real ride from the portal
   (http://localhost:8080), then rate it. We confirm the resulting rating's `providerId`
   matches the vehicle's actual provider (from bc02), not a mock or sentinel value.
2. `GET http://localhost:8085/actuator/circuitbreakers`. We confirm `bookingClient` and
   `fleetClient` both show `CLOSED`.
3. We stop `bc02-fleet-management` and submit a new rating for a still-valid completed
   booking. The `fleetClient` breaker should eventually open, and the new rating should
   still be created, but with `providerId: -1` (the `PROVIDER_UNKNOWN` sentinel) instead of
   failing outright.
4. We stop `bc03-booking` (with bc02 back up) and try rating any booking. Since
   `ResilientBookingClient` can't resolve the booking at all without bc03, the request
   should fall back to whatever `MockBookingClient`'s seeded bookings allow
   (5001/5002/5003), demonstrating the difference between "one upstream degrades
   gracefully" (Fleet) and "the direct upstream is required" (Booking).
