# BC-03 Booking (Task 2, integrated)

We built this as the operational heart of the platform: it owns the entire ride lifecycle,
enforces booking invariants, computes ride cost, and triggers payment. The domain model is
identical to Task 1. For Task 2 we replaced the Task 1 mock clients with real Feign HTTP
clients to Identity & Access, Fleet Management, and Payment, each guarded by a Resilience4j
circuit breaker that falls back to the same Task 1 mock data if the real dependency is unreachable.

Port: **8083**. Swagger UI: `/swagger-ui.html`. Server-rendered UI: `/ui`. H2 console: `/h2-console`.
Also reachable through the integrated web portal at `infra-api-gateway` (port **8080**),
which is the primary way we actually drive this context in Task 2 (search, book, end ride, cancel).

## How we covered the DDD design (Assignment 04)

Identical to Task 1, see that README for the full table (`Booking` aggregate with
`VehicleSnapshot`/`RideSummary` as embedded value objects, `RideLocation`/`TimeInterval`
as plain non-persisted records, `BookingStatus` enum, four domain services, one
repository, four logged domain events). Payment method still lives only as a parameter to
`endBooking`, never persisted on the aggregate. That distinction mattered when we traced
and fixed a bug where the gateway silently defaulted a chosen "PayPal" back to "CARD" (see
`EndBookingRequest.paymentMethodOrDefault()` and the gateway's
`<select name="paymentMethod">` forms).

Per our Assignment 03 context map, Booking is downstream of Identity & Access and Fleet
Management (OHS/PL to ACL) and upstream of Payment and Rating (Customer/Supplier to
Conformist). In Task 2, we turned all three ACL boundaries into real HTTP clients:

| Dependency | Real client | Fallback (Task 1's mock, reused) | Circuit breaker name |
|---|---|---|---|
| Identity & Access | `IdentityFeignClient` | `MockUserClient` (via `ResilientUserClient`) | `identityClient` |
| Fleet Management | `FleetFeignClient` | `MockVehicleClient` (via `ResilientVehicleClient`) | `fleetClient` |
| Payment | `PaymentFeignClient` | `MockPaymentClient` (via `ResilientPaymentClient`) | `paymentClient` |

This is the practical meaning of Anti-Corruption Layer here: `BookingService` only ever
talks to the `UserClient`/`VehicleClient`/`PaymentClient` interfaces, unchanged from Task
1. It has no idea whether the concrete implementation behind them is hitting a real
microservice over Feign or returning fabricated data from a circuit breaker fallback.

## Requirements we covered (Assignment 02)

Same as Task 1: R09 (search, now genuinely cross-service), R11 (booking creation), R12
(ride completion and cost), R13 (payment trigger), R15 (booking history), plus this is the
context where R14/R15's cross-context composition actually becomes visible end to end,
since the gateway stitches Booking, Payment, and Rating data together on one dashboard.

## File-by-file

The domain layer (`domain/model/`, `domain/event/`, `domain/exception/`,
`domain/repository/`, `domain/service/`) and the REST API (`api/`, `api/ui/`) are
unchanged from Task 1, see that README for the full breakdown. What we changed:

### `infrastructure/`, now real cross-service clients, not just mocks
- **`IdentityFeignClient.java`**: `@FeignClient(name = "bc01-identity-access")`, resolved via Eureka.
- **`FleetFeignClient.java`**: `@FeignClient(name = "bc02-fleet-management")`.
- **`PaymentFeignClient.java`**: `@FeignClient(name = "bc04-payment")`. `charge(...)`
  posts to `/api/payments`.
- **`ResilientUserClient.java`**, **`ResilientVehicleClient.java`**, **`ResilientPaymentClient.java`**:
  `@Primary` beans implementing `UserClient`/`VehicleClient`/`PaymentClient`. Each wraps
  its Feign client in `@CircuitBreaker(fallbackMethod = "...")`, falling back to the
  corresponding `Mock*Client` from Task 1 on a technical failure (timeout, connection
  refused, 5xx). We made sure a business "not found" is not treated as a technical
  failure, so it doesn't trip the breaker.
- **`MockUserClient.java`**, **`MockVehicleClient.java`**, **`MockPaymentClient.java`**:
  unchanged from Task 1, now serving double duty as circuit breaker fallback data sources.
- **`UserView.java`**, **`VehicleView.java`**, **`PaymentOutcome.java`**, **`PaymentView.java`**:
  the same lightweight ACL read models as Task 1.

### `src/main/resources/application.yml`
We add Eureka registration, an `optional:` Config Server import, and three named
`resilience4j.circuitbreaker.instances` (`identityClient`, `fleetClient`, `paymentClient`),
each with a 10-call sliding window and 50% failure-rate threshold before opening.

### `pom.xml`
We add `spring-cloud-starter-netflix-eureka-client`, `spring-cloud-starter-config`,
`spring-cloud-starter-openfeign`, `spring-cloud-starter-loadbalancer`, and
`resilience4j-spring-boot3`.

### Consumers of this service in Task 2
- `infra-api-gateway`'s `BookingClient` (Feign) drives search, book, end, cancel from the
  combined portal's `SearchBookController` and `DashboardController`.
- `bc05-rating`'s `BookingFeignClient` / `ResilientBookingClient` looks up a booking's
  status, vehicle, and user before allowing a rating to be submitted, guarded by its own
  `bookingClient` circuit breaker (defined in bc05, not here).

## How to run

Standalone (Feign calls will fail over to mock fallbacks immediately since Identity,
Fleet, Payment, and Eureka aren't up):
```bash
cd Assignment05/Task2
./mvnw -pl bc03-booking spring-boot:run
```

Fully integrated (recommended, needed to actually exercise the real Feign paths):
```bash
cd Assignment05/Task2
./start.sh        # macOS/Linux
start.bat         # Windows
```

- Swagger UI: http://localhost:8083/swagger-ui.html
- Standalone browser UI: http://localhost:8083/ui
- Integrated portal: http://localhost:8080 (search, book, end ride, cancel, dashboard)
- Circuit breaker status: http://localhost:8083/actuator/circuitbreakers
- Eureka dashboard: http://localhost:8761 (confirm `BC03-BOOKING` is registered)
- H2 console: http://localhost:8083/h2-console

## How to test

We didn't write automated tests for this module. Task 1's manual test steps (create, end,
cancel a booking, restriction-violation check) apply directly against port 8083. We also
test the Task 2 integration and resilience behavior:

1. We start the full stack, register and log in at http://localhost:8080, search near
   Dortmund, book a real vehicle from bc02, and end the ride choosing PayPal, then check
   `/bookings/{id}` shows `Method: PAYPAL`, not `CARD` (this exact flow is what the
   earlier payment-method display bug affected).
2. `GET http://localhost:8083/actuator/circuitbreakers`. We confirm `identityClient`,
   `fleetClient`, `paymentClient` all show `CLOSED` while everything's healthy.
3. We stop `bc02-fleet-management` (kill its process/port 8082) while bc03 keeps running,
   then try searching from the portal. The `fleetClient` breaker should trip to `OPEN`
   after enough failures, and `ResilientVehicleClient`'s fallback should serve
   `MockVehicleClient`'s fabricated data instead of an error page.
4. We restart bc02 and confirm the breaker returns to `CLOSED`/`HALF_OPEN` and real data
   resumes (we configured Resilience4j with
   `automatic-transition-from-open-to-half-open-enabled: true`).
