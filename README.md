# The Winx – Instant Mobility Platform

A microservice platform that bundles e-scooters, bikes, e-bikes and e-cars from different
providers into one app. Providers list their vehicles, users search for one nearby, book it,
ride it, pay for it and rate it afterwards.

Built with Domain-Driven Design: each business area is its own bounded context, and each
bounded context is its own independently deployable Spring Boot service.

## What it does

- Users and providers can register and log in
- Providers can add, edit and remove vehicles (e-scooter, bike, e-bike, e-car)
- Providers set pricing (per hour or per km) and restrictions (max age, max duration, etc.)
- Providers can see which of their vehicles are booked and where they currently are
- Users can search for free vehicles near their location and filter by type/restrictions
- Users can book a free vehicle, use it, then end the booking
- The platform computes the ride cost and processes payment automatically
- After a ride, users can rate the vehicle and the provider
- Users can view their past bookings

## Services

| Service | Port | Responsibility |
|---|---|---|
| `infra-eureka-server` | 8761 | Service registry |
| `infra-config-server` | 8888 | Central configuration, serves `config/` |
| `infra-api-gateway` | 8080 | Single entry point, routes `/api/*` (optional) |
| `bc01-identity-access` | 8081 | Registration, login, credentials |
| `bc02-fleet-management` | 8082 | Vehicle CRUD, pricing, restrictions, status & location |
| `bc03-booking` | 8083 | Search, booking, active ride, cost computation |
| `bc04-payment` | 8084 | Charging the user, recording PAID/FAILED |
| `bc05-rating` | 8085 | Vehicle & provider ratings |

Every service only talks to the others over REST (Feign clients) — no service reaches
directly into another service's database. Calls between services go through Resilience4j
circuit breakers, so a failing downstream service degrades gracefully instead of taking
everything else down with it.

## Domain model

Six core entities: **User**, **Provider**, **Vehicle**, **Booking**, **Payment**, **Rating**.

- A Provider owns many Vehicles
- A User makes many Bookings
- A Booking is settled by one Payment and can have one Rating
- A Vehicle keeps its own pricing, GPS location, status (`AVAILABLE`/`BOOKED`) and
  restrictions (max duration, max km, min age, max persons)
- A Booking snapshots the vehicle's price at booking time, so a later price change never
  affects an active ride
- A Rating scores both the vehicle and the provider (1–5) with an optional comment

## Tech stack

- Java 17, Spring Boot 3.2.5, Spring MVC, Spring Data JPA
- REST + Feign for service-to-service calls
- Eureka for service discovery
- Spring Cloud Config for centralized configuration
- Resilience4j for circuit breakers
- H2 in-memory database per service, seeded with sample data for demos
- Maven, via the committed wrapper `mvnw` (no local Maven install needed)

## Project structure

```
.
├── infra-eureka-server     service registry (8761)
├── infra-config-server     central config server, serves /config (8888)
├── infra-api-gateway       single entry point, routes /api/* (8080, optional)
├── bc01-identity-access    Identity & Access (8081)
├── bc02-fleet-management   Fleet Management (8082)
├── bc03-booking            Booking (8083)
├── bc04-payment            Payment (8084)
├── bc05-rating             Rating (8085)
└── config/                 yml files served by the config server
```

Each `bcXX-*` service follows the same internal package layout:

```
com.winx.<context>
├── api/             REST controllers + DTOs
├── domain/          aggregates, entities, value objects, domain events
├── application/     services holding the business logic
└── infrastructure/  JPA repositories, Feign clients to other services
```

## Getting the code

```bash
git clone https://github.com/samaalzoubi/TheWinx-SS2026.git
cd TheWinx-SS2026
```

## Building

You need Java 17+ installed. Maven itself is not required, use the wrapper.

```bash
./mvnw -DskipTests clean install
```

All modules should report `BUILD SUCCESS`.

## Running a single service

Handy while working on just one part of the system:

```bash
./mvnw -pl bc02-fleet-management spring-boot:run
```

Once it's running:
- Swagger / API docs: `http://localhost:<port>/swagger-ui.html`
- H2 database console: `http://localhost:<port>/h2-console`

## Running the full system

Start these in order, each in its own terminal:

```bash
./mvnw -pl infra-eureka-server spring-boot:run     # 8761
./mvnw -pl infra-config-server spring-boot:run     # 8888
./mvnw -pl bc01-identity-access spring-boot:run    # 8081
./mvnw -pl bc02-fleet-management spring-boot:run   # 8082
./mvnw -pl bc03-booking spring-boot:run            # 8083
./mvnw -pl bc04-payment spring-boot:run            # 8084
./mvnw -pl bc05-rating spring-boot:run             # 8085
./mvnw -pl infra-api-gateway spring-boot:run       # 8080 (optional)
```

Check `http://localhost:8761` (Eureka dashboard) to confirm everything registered.

## Git workflow

- `main` holds the shared baseline — parent pom, infra services, and the `bcXX-*` module
  skeletons. Don't touch the module list in the root `pom.xml`.
- One feature branch per bounded context: `feature/bcXX-name`.
- Work only inside your own `bcXX-*` folder and your own `config/bcXX-*.yml`.

```bash
git switch main
git pull
git switch -c feature/bc03-booking
git push -u origin feature/bc03-booking
```

- Commit in small steps, push regularly:

```bash
git add bc03-booking
git commit -m "bc03: add Booking aggregate and repository"
git push
```

- Keep your branch up to date with `main`:

```bash
git switch main && git pull
git switch feature/bc03-booking && git merge main
```

- Open a pull request `feature/bcXX-name → main` once your service builds
  (`./mvnw -pl bcXX-name -DskipTests package`) and boots. Get one teammate to review,
  then merge.

## Troubleshooting

- **`./mvnw` permission denied** → `chmod +x mvnw`
- **Wrong Java version errors** → confirm `java -version` shows 17 or newer
- **Port already in use** → another instance is already running on that port, stop it or
  change `server.port` in that module's `application.yml`
- **Config Server can't find files** → start it from the repo root, or via
  `./mvnw -pl infra-config-server`; it looks in both `file:./config` and `file:../config`

## Known limitations

- Databases are in-memory (H2) — data resets whenever a service restarts.
- The API gateway is optional for now; services can be called directly on their own ports.
