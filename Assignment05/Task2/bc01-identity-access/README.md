# BC-01 Identity & Access (Task 2, integrated)

We own account management and authentication for both Users (riders) and Providers
(fleet owners) here, same as in Task 1. The domain and REST API are identical to the Task 1
standalone version. For Task 2 we only added service discovery (Eureka) and centralized
configuration (Spring Cloud Config) so the API gateway and other bounded contexts can find
and call it by name instead of a hardcoded localhost URL. We kept this context free of any
outbound calls to other services, it's still the platform's global upstream for identity,
per our Assignment 03 context map.

## Current status

This bounded context is already running through the Task 2 startup flow and is available both
standalone and through the integrated portal. It provides user and provider registration, login,
token validation, Swagger UI, a lightweight browser UI, and the H2 console without requiring
any additional setup beyond the existing startup script.

Port: **8081**. Swagger UI: `/swagger-ui.html`. Server-rendered UI: `/ui`. H2 console: `/h2-console`.
Also reachable through the integrated web portal at `infra-api-gateway` (port **8080**).

## How we covered the DDD design (Assignment 04)

Same as Task 1, the domain layer is byte-for-byte identical between the two tasks:

| Building block          | Class                                                | Notes                                                                    |
| ----------------------- | ---------------------------------------------------- | ------------------------------------------------------------------------ |
| Aggregate Root / Entity | `UserAccount`                                        | `id`, `personalInfo`, `email`, `passwordHash`, `registeredAt`, `status`. |
| Aggregate Root / Entity | `ProviderAccount`                                    | Same shape with `companyInfo` instead of `personalInfo`.                 |
| Value Object            | `PersonalInfo`                                       | `name`, `dateOfBirth`, `phoneNumber`. Embedded and immutable.            |
| Value Object            | `CompanyInfo`                                        | `companyName`, `contactName`. Embedded and immutable.                    |
| Value Object            | `AuthToken`                                          | Opaque bearer token (`value`, `expiresAt`).                              |
| Value Object            | `PrincipalRef`                                       | `(id, type)`, what a validated token resolves to.                        |
| Enum                    | `AccountStatus`                                      | `PENDING`, `ACTIVE`, `DEACTIVATED`.                                      |
| Domain Service          | `RegistrationService`                                | Cross-checks email uniqueness across both account types.                 |
| Domain Service          | `AuthenticationService`                              | Issues and validates `AuthToken`s from an in-memory store.               |
| Repository              | `UserAccountRepository`, `ProviderAccountRepository` | `findByEmail`/`existsByEmail`.                                           |

See the Task 1 README for the full rationale on where we intentionally simplified
Assignment 04's design (`Email`/`Password` collapsed into a plain field plus
`EmailValidator`/`PasswordEncoder`, domain events logged rather than published to a broker).

What we added for Task 2 was architectural, not domain related. Per Assignment 03, every
other context treats this one as an Open Host Service behind their own Anti-Corruption
Layer. In Task 1 that OHS was just a REST API on `localhost:8081`. In Task 2 it's a REST
API registered with Eureka, so the gateway's `IdentityClient` (a Feign client) and other
services can resolve it by logical name (`bc01-identity-access`) instead of a fixed host.
We kept the Published Language (the JSON request/response DTOs) unchanged.

## Requirements we covered (Assignment 02)

| Req | Description           | Covered by                                                                       |
| --- | --------------------- | -------------------------------------------------------------------------------- |
| R01 | User registration     | `POST /api/users/register`, also reachable via the gateway at `/register/user`   |
| R02 | User login            | `POST /api/users/login`, also via the gateway at `/login`                        |
| R03 | Provider registration | `POST /api/providers/register`, also via the gateway at `/register/provider`     |
| R04 | Provider login        | `POST /api/providers/login`, also via the gateway at `/login` (role=PROVIDER)    |
| R05 | User lookup           | `GET /api/users/{id}` returning the stored user account                          |
| R06 | Provider lookup       | `GET /api/providers/{id}` returning the stored provider account                  |
| R07 | Token validation      | `GET /api/auth/validate?token=...` returning whether the supplied token is valid |

## File-by-file

The Java source is identical to Task 1's `bc01-identity-access` (see that README for the
full breakdown of `domain/`, `repository/`, `application/`, `api/`, `api/ui/`, and
`infrastructure/`). The only differences are configuration:

### `src/main/resources/application.yml`

We added, relative to Task 1:

```yaml
spring:
  config:
    import: "optional:configserver:http://localhost:8888"
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
```

We used `optional:` so the service still boots fine if the Config Server happens to be
down. It just falls back to this local `application.yml`.

### `pom.xml`

We added `spring-cloud-starter-netflix-eureka-client` and `spring-cloud-starter-config` on
top of Task 1's dependency set (web, data-jpa, thymeleaf, validation, actuator, h2, springdoc).

### Consumers of this service in Task 2

- `infra-api-gateway`'s `IdentityClient` (a `@FeignClient`) calls `/api/users/register`,
  `/api/users/login`, `/api/providers/register`, `/api/providers/login` to back the
  gateway's own `AuthController` (session-based login for the combined web portal).
- No other bc0X service calls Identity & Access directly in our codebase. Booking's own
  `IdentityFeignClient` exists in the source tree, but the gateway is what actually drives
  the login/registration flow end-to-end in the current UI.

## How to run

Standalone (identical to Task 1, Eureka/Config Server are optional, not required):

```bash
cd Assignment05/Task2
./mvnw -pl bc01-identity-access spring-boot:run
```

Fully integrated (recommended, so Eureka, Config Server, and the gateway are all up together):

```bash
cd Assignment05/Task2
./start.sh        # macOS/Linux
start.bat         # Windows
```

This builds and starts `infra-eureka-server` (8761) and `infra-config-server` (8888) first,
waits for them to report healthy, then starts all 5 bounded contexts plus the gateway, and
finally waits for every service to show up in Eureka's registry before printing the URL list.

Once up:

- Swagger UI: http://localhost:8081/swagger-ui.html
- Standalone browser UI: http://localhost:8081/ui
- Integrated portal: http://localhost:8080
- Eureka dashboard: http://localhost:8761 (confirm `BC01-IDENTITY-ACCESS` is registered)
- H2 console: http://localhost:8081/h2-console

## How to test

We verify behavior manually. All of Task 1's manual test steps (register/login/validate via Swagger, the browser UI's Token
Inspector, H2 console inspection) apply unchanged here, this module's own behavior didn't change.

We also test the Task 2 integration path via the combined portal:

1. Start the full stack with `./start.sh` / `start.bat`.
2. Go to http://localhost:8080/register/user and register a user through the gateway.
3. Go to http://localhost:8080/login and log in. The gateway's `AuthController` calls this
   service's `/api/users/login` via Feign and stores `role`/`principalId`/`principalName`
   in the HTTP session.
4. Check http://localhost:8761 (Eureka dashboard) shows `BC01-IDENTITY-ACCESS` as `UP`.
5. We stop this service alone (kill the java process listening on port 8081) and confirm
   the gateway's login page degrades gracefully (shows a "backend returned HTTP ..." error
   via `AuthController.describeFeignError`) instead of crashing.
