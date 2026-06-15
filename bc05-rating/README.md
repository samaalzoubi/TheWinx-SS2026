# BC-05 Rating Microservice

**Owner:** Member E (Mae) | **Port:** 8085

Allows users to rate a vehicle and its provider after a completed booking.
One rating per booking, scores 1–5, immutable once submitted.

Integrates with **bc03-booking** (verify booking is COMPLETED) and **bc02-fleet-management** (vehicle info)
via Feign clients protected by Resilience4j circuit breakers.

---

## File Structure

```
TheWinx-SS2026/bc05-rating/
├── pom.xml                          Maven module config (dependencies, build)
├── README.md                        This file
└── src/main/
    ├── java/com/winx/rating/
    │   ├── RatingApplication.java                  Spring Boot entry point (@EnableFeignClients)
    │   │
    │   ├── domain/                                 DDD building blocks
    │   │   ├── Score.java                          Value object — integer 1–5, throws if out of range
    │   │   ├── Review.java                         Value object — vehicleScore + providerScore + comment
    │   │   ├── RatingTarget.java                   Value object — links rating to vehicleId/providerId/bookingId
    │   │   └── Rating.java                         Aggregate root / JPA entity
    │   │
    │   ├── infrastructure/
    │   │   ├── RatingRepository.java               JPA repository — queries by vehicle, provider, booking
    │   │   └── client/                             Feign clients (Task 2)
    │   │       ├── BookingFeignClient.java          Calls GET /api/bookings/{id} on bc03-booking
    │   │       ├── BookingFeignFallback.java        Circuit open → returns COMPLETED (graceful degrade)
    │   │       ├── FleetFeignClient.java            Calls GET /api/vehicles/{id} on bc02-fleet-management
    │   │       ├── FleetFeignFallback.java          Circuit open → returns placeholder vehicle
    │   │       └── dto/
    │   │           ├── BookingStatusResponse.java   bookingId, userId, status
    │   │           └── VehicleResponse.java         vehicleId, providerId, vehicleType, description, status
    │   │
    │   ├── application/
    │   │   ├── RatingSubmissionService.java         Submits a rating; checks no-duplicate + booking COMPLETED
    │   │   └── RatingQueryService.java              Reads ratings; computes average vehicle score
    │   │
    │   ├── api/
    │   │   ├── RatingController.java               REST controller — all POST/GET endpoints
    │   │   ├── GlobalExceptionHandler.java         Maps exceptions to HTTP codes (400/404/409)
    │   │   ├── dto/
    │   │   │   ├── SubmitRatingRequest.java         Input record for POST /api/ratings
    │   │   │   └── RatingResponse.java              Output record returned by all endpoints
    │   │   └── ui/
    │   │       ├── RatingForm.java                  Mutable form bean for Thymeleaf binding
    │   │       └── RatingUiController.java          Serves the HTML pages (list, submit, detail)
    │   │
    │   └── config/
    │       └── OpenApiConfig.java                  Swagger UI title and description
    │
    └── resources/
        ├── application.yml                          Port, H2, Eureka, Config Server, Feign, Resilience4j
        ├── data.sql                                 5 sample ratings loaded on startup
        └── templates/ratings/
            ├── list.html                            All ratings (filterable by vehicle/provider)
            ├── submit.html                          Form to submit a new rating
            └── detail.html                          Single rating detail view
```

---

## Build

> Requires **Java 21**. If not set, run first:
> ```bash
> export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
> export PATH="$JAVA_HOME/bin:$PATH"
> ```

From the repo root (`TheWinx-SS2026/`):

```bash
# Compile and package (skip tests)
./mvnw -pl bc05-rating -DskipTests clean package
```

You should see `BUILD SUCCESS` at the end.

---

## Run

### Standalone - Task 1 (no other services needed)

```bash
./mvnw -pl bc05-rating spring-boot:run
```

Eureka/Config Server warnings in the log are **normal** when running standalone - the app works fine without them.

### Full system — Task 2 (all services together)

Start in this order, each in its own terminal:

```bash
./mvnw -pl infra-eureka-server  spring-boot:run   # wait until started
./mvnw -pl infra-config-server  spring-boot:run   # wait until started
./mvnw -pl bc01-identity-access spring-boot:run
./mvnw -pl bc02-fleet-management spring-boot:run
./mvnw -pl bc03-booking         spring-boot:run
./mvnw -pl bc04-payment         spring-boot:run
./mvnw -pl bc05-rating          spring-boot:run
```

---

## What to Check

### 1. Health
```
GET http://localhost:8085/actuator/health
```
Expected: `{"status":"UP"}`

### 2. UI (browser)

| URL | What you see |
|-----|-------------|
| `http://localhost:8085/ratings` | All ratings (5 pre-loaded) |
| `http://localhost:8085/ratings/submit` | Form to submit a new rating |
| `http://localhost:8085/ratings/{id}` | Detail of one rating |
| `http://localhost:8085/ratings/vehicle/{vehicleId}` | Ratings filtered by vehicle + average score |
| `http://localhost:8085/ratings/provider/{providerId}` | Ratings filtered by provider |

### 3. REST API (Swagger or curl)

**Swagger UI:** `http://localhost:8085/swagger-ui.html`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/ratings` | Submit a new rating |
| `GET` | `/api/ratings` | List all ratings |
| `GET` | `/api/ratings/{id}` | Get one rating by ID |
| `GET` | `/api/ratings/booking/{bookingId}` | Check if a booking was already rated |
| `GET` | `/api/ratings/vehicle/{vehicleId}` | All ratings for a vehicle |
| `GET` | `/api/ratings/vehicle/{vehicleId}/average` | Average vehicle score |
| `GET` | `/api/ratings/provider/{providerId}` | All ratings for a provider |

**Example curl - submit a rating:**
```bash
curl -X POST http://localhost:8085/api/ratings \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": 2001,
    "userId": 5,
    "vehicleId": 20,
    "providerId": 200,
    "vehicleScore": 4,
    "providerScore": 5,
    "comment": "Great ride!"
  }'
```

**Example curl - get average score for vehicle 10:**
```bash
curl http://localhost:8085/api/ratings/vehicle/10/average
```

### 4. Validation checks (expected errors)

```bash
# Duplicate booking → 409 Conflict
curl -X POST http://localhost:8085/api/ratings \
  -H "Content-Type: application/json" \
  -d '{"bookingId":1001,"userId":1,"vehicleId":10,"providerId":100,"vehicleScore":4,"providerScore":4}'

# Score out of range → 400 Bad Request
curl -X POST http://localhost:8085/api/ratings \
  -H "Content-Type: application/json" \
  -d '{"bookingId":9999,"userId":1,"vehicleId":10,"providerId":100,"vehicleScore":9,"providerScore":3}'
```

### 5. H2 Database Console

`http://localhost:8085/h2-console`

- JDBC URL: `jdbc:h2:mem:rating`
- Username: `sa` | Password: *(empty)*

Run `SELECT * FROM RATINGS;` to see all stored ratings.

### 6. Circuit Breaker (Task 2)

When bc03-booking is **not running**, submitting a rating still works - the Resilience4j circuit breaker catches the failure and the fallback returns `COMPLETED`. You will see this in the log:

```
WARN  bc03-booking unreachable - circuit open. Allowing rating for booking 2001.
```

When bc03-booking **is running**, the Feign client calls `GET /api/bookings/{id}` for real and rejects the rating if status is not `COMPLETED`.

Circuit breaker status:
```
GET http://localhost:8085/actuator/circuitbreakers
```

### 7. Eureka (Task 2 - full system only)

`http://localhost:8761` - bc05-rating should appear as `BC05-RATING` once registered.
