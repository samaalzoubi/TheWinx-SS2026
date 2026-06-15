# BC-05 Rating Microservice

**Owner:** Member E (Mae) | **Port:** 8085

Allows users to rate a vehicle and its provider after a completed booking.
One rating per booking, scores 1–5, immutable once submitted.

---

## File Structure

```
TheWinx-SS2026/bc05-rating/
├── pom.xml                          Maven module config (dependencies, build)
├── README.md                        This file
└── src/main/
    ├── java/com/winx/rating/
    │   ├── RatingApplication.java                  Spring Boot entry point
    │   │
    │   ├── domain/                                 DDD building blocks
    │   │   ├── Score.java                          Value object — integer 1–5, throws if out of range
    │   │   ├── Review.java                         Value object — vehicleScore + providerScore + comment
    │   │   ├── RatingTarget.java                   Value object — links rating to vehicleId/providerId/bookingId
    │   │   └── Rating.java                         Aggregate root / JPA entity
    │   │
    │   ├── infrastructure/
    │   │   └── RatingRepository.java               JPA repository — queries by vehicle, provider, booking
    │   │
    │   ├── application/
    │   │   ├── RatingSubmissionService.java         Submits a rating; enforces no-duplicate-per-booking rule
    │   │   └── RatingQueryService.java              Reads ratings; computes average vehicle score
    │   │
    │   ├── api/
    │   │   ├── RatingController.java               REST controller — all POST/GET endpoints
    │   │   ├── GlobalExceptionHandler.java         Maps exceptions to HTTP codes (400/404/409)
    │   │   ├── dto/
    │   │   │   ├── SubmitRatingRequest.java         Input record for POST /api/ratings
    │   │   │   └── RatingResponse.java              Output record returned by all endpoints
    │   │   └── ui/
    │   │       ├── RatingForm.java                  Mutable form bean used by Thymeleaf (records lack setters)
    │   │       └── RatingUiController.java          Serves the HTML pages (list, submit, detail)
    │   │
    │   └── config/
    │       └── OpenApiConfig.java                  Swagger UI title and description
    │
    └── resources/
        ├── application.yml                          Server port, H2, Eureka, Resilience4j config
        ├── data.sql                                 5 sample ratings loaded on startup
        └── templates/ratings/
            ├── list.html                            Shows all ratings (filterable by vehicle/provider)
            ├── submit.html                          Form to submit a new rating
            └── detail.html                          Single rating detail view
```

---

## REST Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/ratings` | Submit a new rating |
| `GET` | `/api/ratings` | List all ratings |
| `GET` | `/api/ratings/{id}` | Get one rating by ID |
| `GET` | `/api/ratings/booking/{bookingId}` | Check if a booking was already rated |
| `GET` | `/api/ratings/vehicle/{vehicleId}` | All ratings for a vehicle |
| `GET` | `/api/ratings/vehicle/{vehicleId}/average` | Average vehicle score |
| `GET` | `/api/ratings/provider/{providerId}` | All ratings for a provider |

---

## Run Standalone (Task 1)

```bash
./mvnw -pl bc05-rating spring-boot:run
```

- Swagger UI: `http://localhost:8085/swagger-ui.html`
- UI: `http://localhost:8085/ratings`
- H2 console: `http://localhost:8085/h2-console`
- Health: `http://localhost:8085/actuator/health`

Starts with 5 sample ratings pre-loaded from `data.sql`.
