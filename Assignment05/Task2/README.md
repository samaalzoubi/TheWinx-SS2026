# Task 2: integrated system

*DMSA Lab Assignment 05, Task 2. Instant Mobility Platform. Team: The Winx.*

We wired the five bounded-context services from [Task 1](../Task1/) into one real,
running platform, plus a combined web portal so the whole "search, book, ride, pay, rate"
flow can be exercised from a single UI.

## Modules

| Module | Port | Role | Details |
|---|---|---|---|
| `infra-eureka-server` | 8761 | Service discovery | |
| `infra-config-server` | 8888 | Centralized config, serves [`config/`](./config/) | |
| `infra-api-gateway` | 8080 | Combined web portal: login, dashboard, search and book, end ride, rate | |
| `bc01-identity-access` | 8081 | Users, providers, and authentication | [README](bc01-identity-access/README.md) |
| `bc02-fleet-management` | 8082 | Vehicles, search, and status | [README](bc02-fleet-management/README.md) |
| `bc03-booking` | 8083 | Ride lifecycle, calls Identity, Fleet, and Payment | [README](bc03-booking/README.md) |
| `bc04-payment` | 8084 | Payments, with its own circuit breaker on the gateway adapter | [README](bc04-payment/README.md) |
| `bc05-rating` | 8085 | Ratings, calls Booking and Fleet | [README](bc05-rating/README.md) |

## What we changed versus Task 1

Each `bc0X` service's `Mock*Client` (hardcoded example data) is now wrapped by a
`Resilient*Client` that calls the real sibling service through a Feign client resolved via
Eureka, guarded by a named Resilience4j circuit breaker (tuned in
[`config/bc0X-*.yml`](./config/)). If a dependency is unreachable, the circuit breaker's
fallback method drops back to the same mock data Task 1 used, so every service still boots
and works standalone, and can be restarted in any order.

### Why only bc03, bc04, and bc05 have circuit breakers

A circuit breaker guards an outbound call: it protects a service from a dependency it
calls into. It does nothing for a service that only ever gets called by others. Per our
Assignment 03 context map:

- **BC-01 Identity & Access** is the platform's global upstream. Every other context calls
  into it (OHS/PL), but it never calls out to Fleet, Booking, Payment, or Rating. Zero
  outbound calls means there's nothing for a breaker to protect.
- **BC-02 Fleet Management** is likewise upstream to Booking and Rating (OHS/PL). It
  exposes vehicle and provider data but never calls another bounded context itself.
- **BC-03 Booking** calls out to Identity, Fleet, and Payment, so it has three breakers
  (`identityClient`, `fleetClient`, `paymentClient`, all in
  `bc03-booking/.../infrastructure/`).
- **BC-04 Payment** wraps its own gateway adapter call (the stand-in for a real external
  payment processor) in a breaker, even though nothing in our context map calls into it as
  a downstream dependency of bc04 itself. It's guarding against that external dependency,
  not another bounded context.
- **BC-05 Rating** calls out to Booking and Fleet, so it has two breakers.

Adding decorative breakers to bc01 or bc02 with no outbound call behind them would just be
dead configuration. The assignment asks us to apply the pattern where synchronous
cross-service calls actually exist, not to sprinkle it onto every service regardless of
whether it calls anything.

## How to run it

You need a JDK between 17 and 21 installed first. Check with `java -version`.

The easiest way is the provided start script. It builds everything, starts Eureka and
Config first, waits for them to report healthy, then starts the 5 services and the portal.

### macOS or Linux, using the script

```bash
./start.sh          # or: ./start.sh start
./start.sh status   # check what's up
./start.sh stop     # shut everything down
./start.sh urls     # reprint the URL list below
```

This defaults to `JAVA_HOME=/opt/homebrew/opt/openjdk@21/...`. Export your own
`JAVA_HOME` first if your JDK lives somewhere else. If it refuses to run with a
"permission denied" error, make it executable first: `chmod +x start.sh`.

### Windows, using the script

```cmd
start.bat
start.bat status
start.bat stop
start.bat urls
```

Make sure `JAVA_HOME` (or `java` on your `PATH`) points at a JDK 17 to 21 install before
running it. `curl` needs to be available too, it ships with Windows 10 (1803 and later)
and Windows 11 by default.

### Starting each service by hand

If you would rather not use the script, or it does not work in your setup, build once and
then start each module in its own terminal, in this order. Eureka and Config need to be up
first, since every other service looks them up at boot.

```bash
export JAVA_HOME=<path to a JDK 17-21>      # on Windows: set JAVA_HOME=<path>
./mvnw clean install -DskipTests            # on Windows: mvnw.cmd clean install -DskipTests

./mvnw -pl infra-eureka-server spring-boot:run    # 8761, start this first
./mvnw -pl infra-config-server spring-boot:run    # 8888, then this
```

Wait until both report healthy, either by opening [localhost:8761](http://localhost:8761)
and seeing the Eureka dashboard load, or by running
`curl http://localhost:8888/actuator/health` and getting back `{"status":"UP"}`. Once
they're up, start the rest, in any order:

```bash
./mvnw -pl bc01-identity-access spring-boot:run     # 8081
./mvnw -pl bc02-fleet-management spring-boot:run    # 8082
./mvnw -pl bc03-booking spring-boot:run              # 8083
./mvnw -pl bc04-payment spring-boot:run              # 8084
./mvnw -pl bc05-rating spring-boot:run               # 8085
./mvnw -pl infra-api-gateway spring-boot:run         # 8080
```

Substitute `mvnw.cmd` for `./mvnw` on Windows in each of the lines above. You can check any
service the same way, `curl http://localhost:<port>/actuator/health`, and you can watch
them all register on the Eureka dashboard as they come up.

## Try it: register, book, pay, and rate, in the browser

Open [localhost:8080](http://localhost:8080). The login page has one-click buttons that
fill in the seeded accounts below.

**As a User**: log in, go to Search & Book, book a vehicle, then on the Dashboard click End
ride (pick CARD or PayPal from the dropdown, it uses your real device location through the
browser's Geolocation API, then charges automatically through Payment, no separate "pay"
step), then click Rate this ride. The dashboard pulls and merges live data from Booking,
Payment, and Rating for every row, that's the whole point of this module.

**As a Provider**: log in with a Provider account, go to My fleet (add, edit, or delete
vehicles, with live status), Ratings (every rating your fleet received), and Earnings
(every completed ride on your vehicles with its real payment status and a running revenue
total, composed live from Fleet, Booking, and Payment).

### Seeded test accounts

No need to register first, the login page has fill-in buttons for these.

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
| Combined portal, start here | [localhost:8080](http://localhost:8080) |
| Eureka dashboard | [localhost:8761](http://localhost:8761) |
| Config server (example) | [localhost:8888/bc03-booking/default](http://localhost:8888/bc03-booking/default) |
| BC-01 Identity & Access | [README](bc01-identity-access/README.md) &middot; [UI](http://localhost:8081/ui) &middot; [Swagger](http://localhost:8081/swagger-ui.html) |
| BC-02 Fleet Management | [README](bc02-fleet-management/README.md) &middot; [UI](http://localhost:8082/ui) &middot; [Swagger](http://localhost:8082/swagger-ui.html) |
| BC-03 Booking | [README](bc03-booking/README.md) &middot; [UI](http://localhost:8083/ui) &middot; [Swagger](http://localhost:8083/swagger-ui.html) &middot; [circuit breakers](http://localhost:8083/actuator/circuitbreakers) |
| BC-04 Payment | [README](bc04-payment/README.md) &middot; [UI](http://localhost:8084/ui) &middot; [Swagger](http://localhost:8084/swagger-ui.html) &middot; [circuit breakers](http://localhost:8084/actuator/circuitbreakers) |
| BC-05 Rating | [README](bc05-rating/README.md) &middot; [UI](http://localhost:8085/ui) &middot; [Swagger](http://localhost:8085/swagger-ui.html) &middot; [circuit breakers](http://localhost:8085/actuator/circuitbreakers) |
| H2 console, any bc0X service | `http://localhost:<port>/h2-console` |
