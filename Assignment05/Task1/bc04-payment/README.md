# BC-04 Payment (Task 1, standalone)

We designed this as a pure leaf context, per our Assignment 03/04 work: it consumes a
payment request, posted directly by Booking
after a ride completes, and produces no data any other context reads back. It owns
financial transaction consistency: an amount, a payment method, and a strictly
one-directional status machine (`PENDING` to `PAID` or `PENDING` to `FAILED`, never backwards).

Port: **8084**. Swagger UI: `/swagger-ui.html`. H2 console: `/h2-console`.

## How we covered the DDD design (Assignment 04)

| Building block | Class | Notes |
|---|---|---|
| Aggregate Root / Entity | `Payment` | `id`, `bookingId`, `userId` (both bare cross-context references, no FK), embedded `money`, `paymentMethod`, `result`. `markPaid(paidAt)`, `markFailed(reason)`, `resetForRetry()` are its only mutators, each guarded by the current status. |
| Value Object | `Money` | `amount`, `currency`. We made the invariant non-negative, not strictly positive, since a ride of about 0 km under per-km billing is a legitimate zero-cost payment, not an invalid one. |
| Value Object | `PaymentMethod` | `type` (a free-form string, e.g. `"CARD"`/`"PAYPAL"`, not an enum, so we don't need a schema migration for new methods), `maskedReference` (e.g. `"****-CARD"`, stored masked for PCI-style compliance). |
| Value Object | `PaymentResult` | `status`, `paidAt`, `failureReason`. `paidAt`/`failureReason` are mutually exclusive depending on outcome. |
| Enum | `PaymentStatus` | `PENDING`, `PAID`, `FAILED`. Exactly three values, matching our Assignment 04 design (we never added `REFUNDED`). |
| Domain Service | `PaymentProcessingService` | `initiatePayment(...)` creates a `PENDING` payment and immediately settles it against the gateway adapter. `retryPayment(paymentId)` re-invokes the gateway for a `FAILED` payment. |
| Domain Service | `PaymentGatewayAdapter` | Infrastructure-facing interface: `charge(money, method): PaymentResult`. |
| Repository | `PaymentRepository` | `findByBookingId` (plus inherited `JpaRepository` methods). |
| Domain Events | `PaymentInitiated`, `PaymentSucceeded`, `PaymentFailed` | We log these at each corresponding transition inside `PaymentProcessingService`. |

## Requirements we covered (Assignment 02)

| Req | Description | Covered by |
|---|---|---|
| R13 | Payment processing and status handling | `POST /api/payments` calling `PaymentProcessingService.initiatePayment`, `GET /{id}` / `GET /booking/{bookingId}` for status |
| R15 (partial) | Payment history | `GET /booking/{bookingId}` per booking (we didn't build a cross-user listing endpoint into the REST API, see the standalone UI's payment list for that) |

## File-by-file

### `PaymentApplication.java`
Plain `@SpringBootApplication` entry point.

### `domain/model/`
- **`Payment.java`**: `@Entity`. The constructor validates `money.amount >= 0` (throws
  `IllegalArgumentException` otherwise) and starts `result = PaymentResult.pending()`.
  `markPaid`/`markFailed` both throw `InvalidPaymentStateException` if called on anything
  other than a `PENDING` payment. `resetForRetry()` throws unless currently `FAILED`.
- **`Money.java`**, **`PaymentMethod.java`**, **`PaymentResult.java`**: `@Embeddable` value objects.
- **`PaymentStatus.java`**: enum.

### `domain/event/`
`PaymentInitiated`, `PaymentSucceeded`, `PaymentFailed`: plain records, logged by
`PaymentProcessingService`.

### `domain/exception/`
`PaymentNotFoundException` maps to 404, `InvalidPaymentStateException` maps to 409 (for
example retrying a payment that isn't `FAILED`).

### `domain/repository/PaymentRepository.java`
`JpaRepository<Payment, Long>` plus `findByBookingId`.

### `domain/service/PaymentProcessingService.java`
`initiatePayment(bookingId, amount, currency, userId, paymentMethodType,
paymentMethodMaskedRef)` builds `Money`/`PaymentMethod`, constructs and saves a `Payment`,
then immediately calls `settle(...)`, which invokes the gateway adapter and marks the
payment `PAID` or `FAILED` based on the result. `retryPayment(paymentId)` resets a
`FAILED` payment to `PENDING` and re-settles it.

### `infrastructure/`
- **`PaymentGatewayAdapter.java`**: the interface, `charge(Money, PaymentMethod): PaymentResult`.
- **`MockPaymentGatewayAdapter.java`**: the only implementation in Task 1. It inspects the
  `maskedReference` string: if it contains `"fail"` (case-insensitive, e.g. `"FAIL-TEST"`),
  we decline the charge (results in `FAILED`); anything else results in `PAID`.

### `api/`
- **`PaymentController.java`** (`/api/payments`): `POST` (create, returns `201` wrapped in
  `ResponseEntity`), `GET /{id}`, `GET /booking/{bookingId}`, `POST /{id}/retry` (the
  latter three return the plain `PaymentResponse`, not wrapped).
- **`api/dto/CreatePaymentRequest.java`**: `bookingId`, `userId`, `amount` (`@PositiveOrZero`,
  matching `Money`'s non-negative invariant), `currency`, `paymentMethod`, an optional
  `maskedReference` (defaults to `"****"` if blank).
- **`api/dto/PaymentResponse.java`**: flattens `Payment`'s embedded value objects into one
  response shape.
- **`GlobalExceptionHandler.java`**: maps `PaymentNotFoundException` to 404,
  `InvalidPaymentStateException` to 409, bean validation/`IllegalArgumentException` to 400.

### `api/ui/PaymentUiController.java`
Our server-rendered UI at `/ui`: a payments list with status filters and totals, a payment
detail page, and a standalone "Create Test Payment" form (`/ui/payments/new`) that runs
through the exact same `PaymentProcessingService.initiatePayment(...)` call as the REST
API, so we can exercise this whole context in a browser without ever creating a booking first.

### `config/PaymentSeedData.java`
Seeds a couple of demo payments on startup.

## How to run

From `Assignment05/Task1`:
```bash
./mvnw -pl bc04-payment spring-boot:run
```
or via `./start.sh` / `start.bat`.

- Swagger UI: http://localhost:8084/swagger-ui.html
- Browser UI: http://localhost:8084/ui
- H2 console: http://localhost:8084/h2-console (`jdbc:h2:mem:paymentdb`)

## How to test

We didn't write automated tests for this module, so we verify manually.

Via Swagger UI:
1. `POST /api/payments` with `bookingId`, `userId`, `amount`, `currency:"EUR"`,
   `paymentMethod:"CARD"` (no `maskedReference`). We expect `201`, `status:"PAID"`.
2. `POST /api/payments` again with `maskedReference:"FAIL-TEST"`. We expect `201` but
   `status:"FAILED"` with a `failureReason` (payment creation itself always succeeds; it's
   the charge that can fail).
3. `POST /api/payments/{id}/retry` on the failed payment's id (still with the failing
   masked reference baked in). We expect it to fail again, still without state corruption,
   since it resets to `PENDING` then re-fails to `FAILED`.
4. `POST /api/payments/{id}/retry` on an already-`PAID` payment. We expect `409`
   (`InvalidPaymentStateException`).
5. `GET /api/payments/booking/{bookingId}`. We confirm it returns the same payment as `GET /{id}`.

Via the browser UI (http://localhost:8084/ui/payments/new): we use the "Create Test
Payment" form directly with a `maskedReference` containing `fail` to trigger the same
declined-card path as step 2 above, and confirm the payment method icon/label on the
detail page matches whatever we selected (the PayPal icon for PayPal, the card icon for anything else).
