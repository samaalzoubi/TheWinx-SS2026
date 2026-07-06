DMSA Lab Assignment 05, Task 1 | Instant Mobility Platform | Team: The Winx

# Task 1 — Bounded contexts as standalone microservices

Each of the 5 bounded contexts from [Assignment 04](../../Assignment04/) runs as its own independent Spring Boot service: own H2 database, own REST API, own rudimentary Thymeleaf UI. Where a service would need data from another bounded context (e.g. Booking needs Vehicle data from Fleet Management), it uses a `Mock*Client` seeded with example data instead of a real network call — exactly as this task allows. Real inter-service calls are wired up in [Task 2](../Task2/).

## Running it

```bash
./start.sh          # or: ./start.sh start
./start.sh status
./start.sh stop
./start.sh urls
```

(Uses `JAVA_HOME=/opt/homebrew/opt/openjdk@21/...` by default — export your own first if different.) Or run any service individually: `./mvnw -pl bc0X-name spring-boot:run`.

## Seeded test accounts (bc01)

| Role | Email | Password |
|---|---|---|
| User | `alice@example.com` | `password123` |
| User | `bob@example.com` | `password123` (deliberately young — good for testing age-restriction rejections) |
| Provider | `petra@greenwheels.example` | `password123` |
| Provider | `sam@urbanride.example` | `password123` |

`bc02` also seeds ~5 sample vehicles, `bc04` seeds a couple of sample payments (one PAID, one FAILED), and `bc05`'s mock booking client recognizes booking ids `5001`/`5002` (COMPLETED) and `5003` (ACTIVE, for testing the "must be completed" rejection).

## URL reference

| Service | UI | API docs |
|---|---|---|
| bc01 Identity & Access | http://localhost:8081/ui | http://localhost:8081/swagger-ui.html |
| bc02 Fleet Management | http://localhost:8082/ui | http://localhost:8082/swagger-ui.html |
| bc03 Booking | http://localhost:8083/ui | http://localhost:8083/swagger-ui.html |
| bc04 Payment | http://localhost:8084/ui | http://localhost:8084/swagger-ui.html |
| bc05 Rating | http://localhost:8085/ui | http://localhost:8085/swagger-ui.html |
| H2 console (any service) | http://localhost:`<port>`/h2-console | |

Since each service is standalone here, there's no combined dashboard yet — that only exists in [Task 2](../Task2/)'s `infra-api-gateway`.
