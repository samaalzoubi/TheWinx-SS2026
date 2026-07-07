DMSA Lab Assignment 05, Task 2 | Instant Mobility Platform | Team: The Winx

# Task 2 — Integrated system

The five bounded-context services from [Task 1](../Task1/), wired into one real, running platform, plus a combined web portal so the whole "search → book → ride → pay → rate" flow can be exercised from a single UI.

## Modules

| Module | Port | Role |
|---|---|---|
| `infra-eureka-server` | 8761 | Service discovery |
| `infra-config-server` | 8888 | Centralized config, serves [`config/`](./config/) |
| `infra-api-gateway` | 8080 | **Combined web portal** (login, dashboard, search & book, end ride, rate) |
| `bc01-identity-access` | 8081 | Users & providers, auth |
| `bc02-fleet-management` | 8082 | Vehicles, search, status |
| `bc03-booking` | 8083 | Ride lifecycle (calls Identity, Fleet, Payment) |
| `bc04-payment` | 8084 | Payments (own circuit breaker on its gateway adapter) |
| `bc05-rating` | 8085 | Ratings (calls Booking, Fleet) |

## What changed vs. Task 1

Each `bc0X` service's `Mock*Client` (hardcoded example data) is now wrapped by a `Resilient*Client` that calls the real sibling service via a Feign client resolved through Eureka, guarded by a named Resilience4j circuit breaker (tuned in [`config/bc0X-*.yml`](./config/)). If a dependency is unreachable, the circuit breaker's fallback method transparently drops back to the same mock data Task 1 used — so every service still boots and works standalone, restartable in any order.

### Why only bc03, bc04 and bc05 have circuit breakers

A circuit breaker guards an **outbound** call — it protects a service from a dependency it calls into. It does nothing for a service that only ever gets called *by* others. Per the Assignment 03 context map:

- **bc01 Identity & Access** is the platform's global upstream: every other context calls into it (OHS/PL), but it never calls out to Fleet, Booking, Payment, or Rating. Zero outbound calls → nothing for a breaker to protect.
- **bc02 Fleet Management** is likewise upstream to Booking and Rating (OHS/PL) — it exposes vehicle/provider data but never calls another bounded context itself.
- **bc03 Booking** calls out to Identity, Fleet, and Payment → 3 breakers (`identityClient`, `fleetClient`, `paymentClient`, all in `bc03-booking/.../infrastructure/`).
- **bc04 Payment** wraps its own gateway adapter call (the stand-in for a real external payment processor) in a breaker, even though nothing in this project's context map calls into it as a downstream dependency of bc04 itself — it's guarding against *that* external dependency, not another bounded context.
- **bc05 Rating** calls out to Booking and Fleet → 2 breakers.

Adding decorative breakers to bc01/bc02 with no outbound call behind them would just be dead configuration — the assignment's requirement is to apply the pattern where synchronous cross-service calls actually exist, not to sprinkle it onto every service regardless of whether it calls anything.

## Running it

The easiest way is the provided script, which builds everything, starts Eureka and Config first, waits for them to be healthy, then starts the 5 services and the portal:

```bash
./start.sh          # or: ./start.sh start
./start.sh status   # check what's up
./start.sh stop     # shut everything down
./start.sh urls     # reprint the URL list below
```

(It uses `JAVA_HOME=/opt/homebrew/opt/openjdk@21/...` by default — override by exporting your own `JAVA_HOME` first if you're on a different setup.)

To start things individually instead:

```bash
export JAVA_HOME=<path to a JDK 17-21>
./mvnw clean install -DskipTests

./mvnw -pl infra-eureka-server spring-boot:run    # 8761
./mvnw -pl infra-config-server spring-boot:run    # 8888
./mvnw -pl bc01-identity-access spring-boot:run
./mvnw -pl bc02-fleet-management spring-boot:run
./mvnw -pl bc03-booking spring-boot:run
./mvnw -pl bc04-payment spring-boot:run
./mvnw -pl bc05-rating spring-boot:run
./mvnw -pl infra-api-gateway spring-boot:run      # 8080
```

## Try it: register → book → pay → rate, in the browser

Open **http://localhost:8080** — the login page has one-click buttons that fill in the seeded accounts below.

**As a User**: log in → **Search & Book** → book a vehicle → on the **Dashboard**, click **End ride** (picks CARD or PayPal from a dropdown, uses your real device location via the browser's Geolocation API, then auto-charges via Payment — no separate "pay" step) → click **Rate this ride**. The dashboard pulls and merges live data from Booking, Payment and Rating for every row — that's the whole point of this module.

**As a Provider**: log in with a Provider account → **My fleet** (add/edit/delete vehicles, live status) → **Ratings** (every rating your fleet received) → **Earnings** (every completed ride on your vehicles with its real payment status and a running revenue total, composed live from Fleet + Booking + Payment).

### Seeded test accounts (no need to register first — login page has fill-in buttons for these)

| Role | Email | Password |
|---|---|---|
| User | `marianne@instant-mobility.example` | `password123` |
| User | `rowena@instant-mobility.example` | `password123` |
| User | `priyanka@instant-mobility.example` | `password123` |
| Provider | `sama@providers.instant-mobility.example` | `password123` |
| Provider | `mae@providers.instant-mobility.example` | `password123` |

## URL reference

| What | URL |
|---|---|
| **Combined portal (start here)** | http://localhost:8080 |
| Eureka dashboard | http://localhost:8761 |
| Config server (example) | http://localhost:8888/bc03-booking/default |
| bc01 Identity & Access — UI / Swagger | http://localhost:8081/ui — http://localhost:8081/swagger-ui.html |
| bc02 Fleet Management — UI / Swagger | http://localhost:8082/ui — http://localhost:8082/swagger-ui.html |
| bc03 Booking — UI / Swagger / circuit breakers | http://localhost:8083/ui — http://localhost:8083/swagger-ui.html — http://localhost:8083/actuator/circuitbreakers |
| bc04 Payment — UI / Swagger / circuit breakers | http://localhost:8084/ui — http://localhost:8084/swagger-ui.html — http://localhost:8084/actuator/circuitbreakers |
| bc05 Rating — UI / Swagger / circuit breakers | http://localhost:8085/ui — http://localhost:8085/swagger-ui.html — http://localhost:8085/actuator/circuitbreakers |
| H2 console (any bc0X service) | http://localhost:`<port>`/h2-console |
