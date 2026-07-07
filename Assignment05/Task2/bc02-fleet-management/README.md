# BC-02 Fleet Management (Task 2, integrated)

We built this as the single source of truth for vehicles. The domain and REST API are
identical to the Task 1 standalone version. For Task 2 we only added Eureka registration
and Config Server import. Like BC-01, we kept this context free of outbound dependencies
even in Task 2: it's a pure upstream, consumed by Booking and Rating, consuming nothing itself.

Port: **8082**. Swagger UI: `/swagger-ui.html`. Server-rendered UI: `/ui`. H2 console: `/h2-console`.
Also reachable through the integrated web portal at `infra-api-gateway` (port **8080**),
for example `/search` and provider fleet pages.

## How we covered the DDD design (Assignment 04)

Identical to Task 1, see that README for the full table (`Vehicle` aggregate with
`VehicleLocation`/`PricingPolicy`/`UsageRestrictions` as embedded value objects,
`VehicleType`/`VehicleStatus`/`BillingModel` enums, three domain services, one
repository, four logged domain events).

Per our Assignment 03 context map, Fleet Management is upstream to both Booking and Rating
via an Open Host Service. In Task 2 that OHS is discoverable through Eureka instead of a
fixed localhost address, and both consumers reach it through Feign clients
(`FleetFeignClient` in bc03, `FleetFeignClient` in bc05) wrapped in their own
Anti-Corruption Layer and circuit breaker. None of that lives in this module, since Fleet
Management itself makes no outbound calls.

## Requirements we covered (Assignment 02)

Same as Task 1: R05 (vehicle management), R06 (pricing), R07 (usage restrictions), R08
(fleet status tracking), R09 (vehicle search), R10 (vehicle filtering), all via
`VehicleController`'s 8 endpoints, unchanged from Task 1.

## File-by-file

The Java source is identical to Task 1's `bc02-fleet-management` (see that README for the
full `domain/`, `api/`, `api/ui/`, and `config/` breakdown). Only the configuration differs:

### `src/main/resources/application.yml`
We add Eureka client registration and an `optional:` Config Server import, on top of Task
1's datasource/JPA/actuator/springdoc config. Same pattern as BC-01's Task 2 README.

### `pom.xml`
We add `spring-cloud-starter-netflix-eureka-client` and `spring-cloud-starter-config`.

### Consumers of this service in Task 2
- `bc03-booking`'s `FleetFeignClient` / `ResilientVehicleClient`: vehicle lookup,
  availability search, and status updates (`AVAILABLE` and `BOOKED`) during the booking lifecycle.
- `bc05-rating`'s `FleetFeignClient` / `FleetLookupGateway`: resolves a vehicle's
  `providerId` when a rating is submitted, guarded by a `fleetClient` circuit breaker.
- `infra-api-gateway`'s `FleetClient`: backs the portal's vehicle search page directly, we
  don't proxy this through Booking.

## How to run

Standalone:
```bash
cd Assignment05/Task2
./mvnw -pl bc02-fleet-management spring-boot:run
```

Fully integrated (recommended):
```bash
cd Assignment05/Task2
./start.sh        # macOS/Linux
start.bat         # Windows
```

- Swagger UI: http://localhost:8082/swagger-ui.html
- Standalone browser UI: http://localhost:8082/ui
- Integrated portal search page: http://localhost:8080/search
- Eureka dashboard: http://localhost:8761 (confirm `BC02-FLEET-MANAGEMENT` is registered)
- H2 console: http://localhost:8082/h2-console

## How to test

We didn't write automated tests for this module. Task 1's manual test steps (create,
search, delete-conflict via Swagger, the `/ui/provider/{id}/fleet` and `/ui/browse` pages)
apply unchanged.

We also test the Task 2 integration path:
1. We start the full stack, register a user, and log in through http://localhost:8080.
2. We go to http://localhost:8080/search and search near Dortmund. This calls this
   service's `GET /api/vehicles/search` directly from the gateway (not via bc03),
   confirming the gateway's own `FleetClient` reaches this service through Eureka.
3. We book one of the returned vehicles, then check http://localhost:8082/ui/browse. The
   vehicle's status should now show `BOOKED`, confirming bc03's cross-service status
   update landed here correctly.
4. We check that http://localhost:8761 shows `BC02-FLEET-MANAGEMENT` as `UP`.
5. We stop this service alone and retry the search page. The gateway should show "Could
   not reach the Fleet Management service" instead of crashing (see
   `SearchBookController.search`'s `FeignException` handling in the gateway).
