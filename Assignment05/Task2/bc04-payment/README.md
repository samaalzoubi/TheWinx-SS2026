# BC-04 Payment (Task 2, integrated)

We designed this as a pure leaf context, per our Assignment 03/04 work: it consumes a
payment request (from Booking, via Feign in Task 2) and produces no data any other context
reads back. The domain model is identical to Task 1. For Task 2 we added a Resilience4j
circuit breaker around the one external boundary this context actually has: the (still
mocked) payment gateway call itself, since Payment has no other bounded context to call into.

Port: **8084**. Swagger UI: `/swagger-ui.html`. H2 console: `/h2-console`.
Also reachable indirectly through the integrated web portal (payment status shown on the
booking dashboard) at `infra-api-gateway` (port **8080**).

## How we covered the DDD design (Assignment 04)

Identical to Task 1, see that README for the full table (`Payment` aggregate with embedded
`Money`/`PaymentMethod`/`PaymentResult`, `PaymentStatus` enum with exactly three values,
`PaymentProcessingService`, one repository, three logged domain events).

Per Assignment 03, Booking to Payment is a Customer/Supplier relationship where we made
Payment Conformist: it accepts Booking's `bookingId`/`amount`/`currency` data structure as
is, no translation layer, because both contexts had input into the interface during
design. That's still true in Task 2: `PaymentFeignClient`/`CreatePaymentRequest` on the
Booking side map directly onto this context's own `CreatePaymentRequest` fields, with no
ACL needed on this end. Payment is the downstream conformist here, not the one building an
anti-corruption layer; Booking is.

## Requirements we covered (Assignment 02)

Same as Task 1: R13 (payment processing/status), R15 (partial, per-booking payment history).

## File-by-file

The domain layer, REST API, and standalone "Create Test Payment" UI are unchanged from
Task 1, see that README for the full breakdown of `domain/model/`, `domain/event/`,
`domain/exception/`, `domain/repository/`, `domain/service/`, `api/`, and `api/ui/`. What
we changed:

### `infrastructure/`, now circuit-breaker protected
- **`PaymentGatewayAdapter.java`**: unchanged interface.
- **`MockPaymentGatewayAdapter.java`**: same "fail"-in-masked-reference hook as Task 1,
  plus a second hook we added: a masked reference containing `"gateway-down"` throws
  `PaymentGatewayUnavailableException` to simulate the external gateway being technically
  unreachable (as opposed to a normal card decline). This only exists in Task 2, so the
  circuit breaker has something to actually trip on.
- **`ResilientPaymentGatewayAdapter.java`**: `@Primary` bean, wraps the mock adapter's
  `charge(...)` in `@CircuitBreaker(name = "paymentGateway")`. When the breaker trips (or
  the mock throws `PaymentGatewayUnavailableException`), its fallback method returns a
  `FAILED` `PaymentResult` with a "circuit breaker open" reason instead of propagating an
  exception up to `PaymentProcessingService`.
- **`PaymentGatewayUnavailableException.java`**: the technical-failure signal used above.

### `src/main/resources/application.yml`
We add Eureka registration, an `optional:` Config Server import, and one named circuit
breaker, `paymentGateway` (10-call sliding window, 50% failure threshold, 15s open-state wait).

### `pom.xml`
We add `spring-cloud-starter-netflix-eureka-client`, `spring-cloud-starter-config`, and
`resilience4j-spring-boot3`. No Feign starter here, since this context never calls out to
another microservice, only to its (mocked) external gateway.

### Consumers of this service in Task 2
- `bc03-booking`'s `PaymentFeignClient` / `ResilientPaymentClient` calls `POST
  /api/payments` after a ride ends, guarded by bc03's own `paymentClient` circuit breaker
  (defined in bc03, separate from this context's `paymentGateway` breaker).
- `infra-api-gateway`'s `PaymentClient` reads payment status back for the booking
  dashboard (`GET /api/payments/booking/{bookingId}`).

## How to run

Standalone:
```bash
cd Assignment05/Task2
./mvnw -pl bc04-payment spring-boot:run
```

Fully integrated (recommended):
```bash
cd Assignment05/Task2
./start.sh        # macOS/Linux
start.bat         # Windows
```

- Swagger UI: http://localhost:8084/swagger-ui.html
- Browser UI: http://localhost:8084/ui
- Circuit breaker status: http://localhost:8084/actuator/circuitbreakers
- Eureka dashboard: http://localhost:8761 (confirm `BC04-PAYMENT` is registered)
- H2 console: http://localhost:8084/h2-console

## How to test

We didn't write automated tests for this module. Task 1's manual test steps (create,
retry, fail a payment via Swagger or the "Create Test Payment" UI page) apply directly. We
also test the Task 2 resilience behavior:

1. `POST /api/payments` with `maskedReference:"gateway-down"`. We expect `201` with
   `status:"FAILED"` and a failure reason mentioning the circuit breaker or gateway
   unavailability. This is a simulated technical failure, not a normal decline.
2. `GET http://localhost:8084/actuator/circuitbreakers` after step 1. The `paymentGateway`
   breaker's failure count should have incremented. We repeat step 1 enough times (per the
   50% threshold over a 10-call window) to see it flip to `OPEN`.
3. While `OPEN`, `POST /api/payments` with a perfectly normal request. It should still
   return `201`/`FAILED` immediately via the fallback (no real gateway call attempted),
   confirming the breaker is actually short-circuiting rather than just logging.
4. We wait about 15 seconds (the configured `wait-duration-in-open-state`) and try again.
   The breaker should move to `HALF_OPEN` then back to `CLOSED` once calls succeed again.
5. End to end: we start the full stack, complete a ride from the portal
   (http://localhost:8080), and confirm the dashboard shows the resulting payment's status.
   This exercises bc03's `paymentClient` breaker and this context's `paymentGateway`
   breaker together in the one real request.
