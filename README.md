# TheWinx-SS2026 / bc-01-identity-access 🔐
## Project Overview 🥑

The <mark> **BC01-Identity-Access** </mark> microservice is a foundational component of the Software Intensive Solution project. It manages user authentication, authorization, and profile management. This service ensures that only authorized users can access specific fleet resources and provides a secure gateway for the entire system.

## Architecture 🔨

This project follows **Domain-Driven Design (DDD)** principles to ensure a clear separation of concerns:


**API**: Contains REST Controllers and Data Transfer Objects (DTOs) for external communication.

**Application**: Implements the business logic and service layer (e.g., IdentityAccessService, FleetService).

**Domain**: The core of the system, containing entities like UserAccount, Role, and AccountStatus.

**Infrastructure**: Handles data persistence (Repositories) and external service integrations (FleetClient).

**Web**: Manages the UI controllers for the frontend templates.

## Key Features 🗝

- User Authentication: Secure login and registration system.

- Role-Based Access Control (RBAC): Manage user permissions and roles.

- Fleet Integration: Connected fleet management with login capabilities.

- User Dashboard: A dedicated interface for users to manage their profiles and bookings.

- Global Exception Handling: Centralized error management for a robust API.

## Tech Stack 👌🏻

- Backend: Java, Spring Boot

- Frontend: Thymeleaf (HTML/CSS/JS)

- Database: Spring Data JPA


## API Endpoints (/api/v1/identity) 📡

### User Management Endpoints 👤
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :---: |
| `POST` | `/api/v1/identity/users` | Register a new user account | No |
| `GET` | `/api/v1/identity/users` | List all registered users | Yes |
| `GET` | `/api/v1/identity/users/{userId}` | Get profile details for a specific user | Yes |
| `PUT` | `/api/v1/identity/users/{userId}` | Update user profile (username/email) | Yes |
| `DELETE` | `/api/v1/identity/users/{userId}` | Deactivate a user account (Sets status to `INACTIVE`) | Yes |

### Authentication & Authorization 🔐
| Method | Endpoint | Description | Return Type |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/identity/auth/login` | Authenticate user via SHA-256 hash | `AuthResponse` |
| `GET` | `/api/v1/identity/permissions/check` | Verify if a user has a specific permission | `Boolean` |
| `POST` | `/api/v1/identity/users/{userId}/roles` | Assign a security role (e.g., `ADMIN`) | `UserResponse` |
| `DELETE` | `/api/v1/identity/users/{userId}/roles` | Revoke a security role from a user | `UserResponse` |

### Fleet Integration (Proxy Endpoints) 🚗
| Method | Endpoint | Description | Service Source |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/identity/fleet/vehicles` | List all available vehicles | `FleetClient` |
| `GET` | `/api/v1/identity/fleet/bookings` | List bookings (Filter by `username` optional) | `FleetService` |
| `POST` | `/api/v1/identity/fleet/bookings` | Create a new vehicle booking | `FleetService` |
| `POST` | `/api/v1/identity/fleet/bookings/{id}/cancel` | Cancel an active booking | `FleetService` |


## Configuration & Infrastructure ⚙️

The service relies on a centralized configuration server. Key parameters in application.yml include:

  **Eureka Discovery**: Registered at `http://localhost:8761/eureka/`

  **Circuit Breaker**:

  `failureRateThreshold`: <mark>50%</mark>

  `waitDurationInOpenState`: <mark>10s</mark>

**Persistence**: Managed via UserAccount entity with EAGER role fetching for performance.

## Getting Started 🧠


  #### 1️⃣Clone the repository:
    bash
    git clone <https://github.com/samaalzoubi/TheWinx-SS2026.git>


  #### 2️⃣Configure Environment: 
  Check `src/main/resources/application.yml` for database and server configurations.

  #### 3️⃣Run the Application:
    bash
    ./mvnw spring-boot:run


  #### 4️⃣Access the UI: 
Open your browser and navigate to http://localhost:8080 

  #### 5️⃣Communication: 
Client (for internal microservice communication) 




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
