# BC-01 Identity & Access (Task 1, standalone)

We own account management and authentication for both Users (riders) and Providers
(fleet owners) in this context. We made it the global upstream context: every other
bounded context authenticates against it, but it has no outbound dependencies of its own,
so for Task 1 we run it as a fully self-contained Spring Boot service with its own
in-memory H2 database.

Port: **8081**. Swagger UI: `/swagger-ui.html`. Server-rendered UI: `/ui`. H2 console: `/h2-console`.

## How we covered the DDD design (Assignment 04)

We gave this context two aggregate roots instead of one, because Users and Providers are
genuinely different kinds of accounts with different attributes and no shared lifecycle:

| Building block | Class | Notes |
|---|---|---|
| Aggregate Root / Entity | `UserAccount` | `id`, `personalInfo`, `email`, `passwordHash`, `registeredAt`, `status`. `deactivate()` is the only real behavior we put on the aggregate. |
| Aggregate Root / Entity | `ProviderAccount` | Same shape, but we gave it `companyInfo` instead of `personalInfo`, plus its own `phoneNumber`. |
| Value Object | `PersonalInfo` | `name`, `dateOfBirth`, `phoneNumber`. Embedded and immutable. |
| Value Object | `CompanyInfo` | `companyName`, `contactName`. Embedded and immutable. |
| Value Object | `AuthToken` | Opaque bearer token (`value`, `expiresAt`) we issue on successful login. |
| Value Object | `PrincipalRef` | `(id, type)` pair identifying which kind of account a validated token belongs to. |
| Enum | `AccountStatus` | `PENDING`, `ACTIVE`, `DEACTIVATED`. |
| Domain Service | `RegistrationService` | Enforces email uniqueness across both account types and hashes passwords before persisting. |
| Domain Service | `AuthenticationService` | Authenticates by email/password, issues and validates `AuthToken`s. |
| Repository | `UserAccountRepository`, `ProviderAccountRepository` | Thin `JpaRepository` extensions plus `findByEmail`/`existsByEmail`. |

Where we simplified the Assignment 04 design on purpose: we had sketched `Email` and
`Password` as their own dedicated Value Object classes with `validate()`/`matches()`
methods. We ended up using a plain `String email` field validated by the `EmailValidator`
utility instead, and we delegate hashing and matching entirely to Spring Security's
`PasswordEncoder` bean. Same invariants (unique, validated email, never-stored-in-plaintext
password), fewer classes. We also didn't wire up a message broker, so the domain events the
design called for (`UserRegistered`, `ProviderRegistered`, `UserAuthenticated`) are logged
rather than published (see `AuthenticationService`/`RegistrationService`).

Per our Assignment 03 context map, we made Identity & Access the platform's single
upstream for identity: Fleet Management, Booking, and Rating all treat it as an Open Host
Service with a Published Language and wrap it behind their own Anti-Corruption Layer, so
this context never needs to know about vehicles, bookings, or ratings. It has zero
outbound dependencies, which is exactly why we could run it in complete isolation for Task 1.

## Requirements we covered (Assignment 02)

| Req | Description | Covered by |
|---|---|---|
| R01 | User registration | `POST /api/users/register` calling `RegistrationService.registerUser` |
| R02 | User login | `POST /api/users/login` calling `AuthenticationService.authenticateUser` |
| R03 | Provider registration | `POST /api/providers/register` calling `RegistrationService.registerProvider` |
| R04 | Provider login | `POST /api/providers/login` calling `AuthenticationService.authenticateProvider` |

Everything else (vehicle management, booking, payment, rating) is out of scope for this
context by design; it only ever hands out and validates identity.

## File-by-file

### `IdentityAccessApplication.java`
Our Spring Boot entry point. No extra annotations beyond `@SpringBootApplication`, since
Task 1 doesn't register with Eureka or import config from a Config Server.

### `domain/`, the model
- **`UserAccount.java`** / **`ProviderAccount.java`**: `@Entity` aggregate roots we built.
  Constructors enforce `status = ACTIVE` on creation. `deactivate()` is the only mutator
  besides JPA's own field access.
- **`PersonalInfo.java`** / **`CompanyInfo.java`**: `@Embeddable` value objects, immutable by
  convention (no setters after construction).
- **`AccountStatus.java`**: `PENDING | ACTIVE | DEACTIVATED` enum.
- **`AuthToken.java`**: `record(value, expiresAt)` with `isExpired()`. Not a JPA entity; see
  `AuthenticationService` below for where we actually keep instances.
- **`PrincipalRef.java`**: `record(id, type)` with `USER`/`PROVIDER` string constants, what
  `validateToken(...)` resolves a token to.
- **`EmailValidator.java`**: static regex-based format check we use in `RegistrationService`.
- **`domain/service/RegistrationService.java`**: `registerUser(...)`/`registerProvider(...)`.
  Validates email format, checks uniqueness against both repositories (a user and a
  provider can't share an email), hashes the password, saves.
- **`domain/service/AuthenticationService.java`**: `authenticateUser`/`authenticateProvider`
  check credentials and mint a new `AuthToken` (random UUID, 2 hour TTL) that we hold in an
  in-memory `ConcurrentHashMap` (`tokenStore`). Tokens don't survive a restart, by design,
  since building real JWT/session infrastructure was out of scope for this lab.
  `validateToken(...)` looks a token up, checks expiry, and removes it if expired.

### `repository/`
- **`UserAccountRepository.java`** / **`ProviderAccountRepository.java`**: `JpaRepository<...,
  Long>` plus `findByEmail`/`existsByEmail`.

### `application/`, thin exception types
- **`EmailAlreadyRegisteredException.java`**, **`InvalidCredentialsException.java`**,
  **`NotFoundException.java`**: mapped to HTTP 409, 401, 404 respectively by
  `api/GlobalExceptionHandler.java`.

### `api/`, the REST surface
- **`UserController.java`** (`/api/users`): `POST /register`, `POST /login`, `GET /{id}`.
- **`ProviderController.java`** (`/api/providers`): the same three operations for providers.
- **`AuthController.java`** (`/api/auth`): `GET /validate?token=...`. Calls
  `AuthenticationService.validateToken` and always returns `200` with `valid:false` for a
  bad, missing, or expired token (never throws). We built this specifically so other
  bounded contexts can call it to check a bearer token.
- **DTOs** (`UserRegisterRequest`, `ProviderRegisterRequest`, `LoginRequest`, `UserResponse`,
  `ProviderResponse`, `UserLoginResponse`, `ProviderLoginResponse`, `ValidateTokenResponse`,
  `ErrorResponse`): plain Java `record`s, one per request/response shape.
- **`GlobalExceptionHandler.java`**: `@RestControllerAdvice` mapping the three exceptions
  above (plus bean validation failures) to proper HTTP status codes.

### `api/ui/UiController.java`
Our server-rendered Thymeleaf UI at `/ui` (registration forms, login, a token inspector,
and read-only account directory/profile pages), so we can exercise the service in a browser
without a REST client. It delegates to the exact same `RegistrationService`/
`AuthenticationService` the REST API uses; we didn't duplicate any business logic for it.

### `infrastructure/`
- **`SecurityConfig.java`**: defines the `PasswordEncoder` bean and disables Spring
  Security's default form-login/CSRF. We're not using Spring Security's own auth model
  here; authentication is entirely custom via `AuthenticationService`.
- **`SeedDataRunner.java`**: `CommandLineRunner` that seeds a couple of demo users/providers
  on startup so the UI/Swagger has data to look at immediately.

## How to run

From `Assignment05/Task1`:
```bash
./mvnw -pl bc01-identity-access spring-boot:run
```
or start all 5 Task 1 services together via `./start.sh` (macOS/Linux) or `start.bat`
(Windows) from the `Task1` folder.

Once up:
- Swagger UI: http://localhost:8081/swagger-ui.html
- Browser UI: http://localhost:8081/ui
- H2 console: http://localhost:8081/h2-console (JDBC URL `jdbc:h2:mem:identitydb`, user `sa`, no password)

## How to test

We didn't write automated tests for this module, so we verify behavior manually.

Via Swagger UI (http://localhost:8081/swagger-ui.html):
1. `POST /api/users/register` with a name, email, password, and dateOfBirth. We expect `201` and a `UserResponse`.
2. `POST /api/users/register` again with the same email. We expect `409 Conflict`.
3. `POST /api/users/login` with that email/password. We expect `200` with a `token` and `expiresAt`.
4. `POST /api/users/login` with a wrong password. We expect `401`.
5. Copy the token from step 3, then `GET /api/auth/validate?token=<token>`. We expect `valid:true` with the matching `principalId`/`principalType:"USER"`.
6. `GET /api/auth/validate?token=garbage`. We expect `200` with `valid:false` (never an error).
7. We repeat 1 through 3 against `/api/providers/register` and `/api/providers/login` to cover the provider side.

Via the browser UI (http://localhost:8081/ui): we register a user, log in, and use the
Token Inspector link on the login-success page to visually confirm token validation. This
exercises the identical code path as step 5 above, just through a form instead of curl.

Via the H2 console (http://localhost:8081/h2-console): after registering a couple of
accounts, we run `SELECT * FROM USER_ACCOUNTS` / `SELECT * FROM PROVIDER_ACCOUNTS` to
confirm `password_hash` is never the plaintext password and `status` defaults to `ACTIVE`.
