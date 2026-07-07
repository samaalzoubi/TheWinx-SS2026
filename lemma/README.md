# LEMMA models (Lab Assignment 06)

We modeled our architecture in LEMMA here, per Lab Assignment 06. We put this folder at
the top of the repository so it
sits alongside the actual Java source it describes in `Assignment05/Task2`.

We modeled Task 2 specifically, not Task 1. Task 2 is the fully integrated system (Eureka,
Config Server, the 5 bounded contexts, and the API gateway), so it's the version where
deployment and service-discovery relationships actually exist to model. Task 1's
standalone services share the same domain and REST API, so nothing here would change if
we'd modeled that version instead.

## What we did

We built one domain model, one service model, one technology mapping, and one deployment
model per bounded context, plus a couple of infrastructure-only deployment models for
Eureka, the Config Server, and the API gateway. We didn't just write these once and leave
them: we went back and audited every single model against the real Task 2 source code
after realizing our first pass (particularly BC-01's) had drifted from an earlier
prototype and no longer matched what we'd actually implemented. Where we found a mismatch,
including wrong endpoint paths, enum values that didn't exist in the real code, a whole
domain concept (roles/permissions) that we'd designed but never built, we fixed the model
rather than the code, since the code is the source of truth here.

## Folder structure

```
lemma/
  bc01-identity-access/   IdentityAccess.{data,services,operation,mapping}
  bc02-fleet-management/  fleetManagement.{data,services,operation,mapping}
  bc03-booking/           booking.{data,services,operation,mapping}
  bc04-payment/           paymentCore.{data,services,operation,mapping}
  bc05-rating/            ratingCore.{data,services,operation,mapping}
  eureka-server/          eurekaServer.operation
  infra-config-server/    configServer.operation
  infra-api-gateway/      apiGateway.operation
  technology/             Java, javaWithSpring, map, docker, eureka, and other
                          course-provided technology catalogs
```

We didn't author the files in `technology/`. Those are the prepared LEMMA technology
models from ILIAS that Assignment 06 tells us to reuse; we only reference them from our
own `.operation` files.

Each bounded context has four files, and they build on each other in this order:

1. **`.data`** (Domain Data Modeling Language): the domain concepts for that context.
   Structures, value objects, enums, repositories, and application services, tagged with
   DDD features like `<aggregate>`, `<entity>`, `<valueObject>`, `<part>`. Since we hadn't
   learned how to attach technology information directly to domain models this semester,
   we added the real JPA annotations as comments instead (for example `// @Entity`,
   `// @Embedded`), right above the field or structure they apply to.
2. **`.services`** (Service Modeling Language): the microservice's interfaces and
   operations, imported from the `.data` file. This is where we documented each
   operation's purpose and parameters using the `---` comment blocks.
3. **`.operation`** (Technology Modeling Language applied to the service model): maps each
   interface and operation onto REST endpoints, HTTP verbs, and Spring aspects
   (`@RequestParam`, `@PathVariable`, `@ResponseStatus`, and so on), plus the service's
   port, datasource, and Hibernate configuration.
4. **`.mapping`** (Technology Modeling Language applied as a deployment model): wraps the
   microservice in a Docker container, points the Dockerfile at the real JAR name and
   version, and declares which infrastructure nodes (Eureka, Config Server) it depends on.

## Per-context notes

- **BC-01 Identity & Access**: two aggregates, `UserAccount` and `ProviderAccount`, plus
  `AuthToken`/`PrincipalRef` for the bearer-token mechanism. We removed the entire
  role/permission system and the `fleetGateway` interface that had ended up in here from
  an earlier draft; neither exists in the real bc01 code, and booking/fleet operations
  belong in their own contexts anyway.
- **BC-02 Fleet Management**: the `Vehicle` aggregate with three embedded value objects
  (`VehicleLocation`, `PricingPolicy`, `UsageRestrictions`). We corrected the REST base
  path to `/api/vehicles` (we'd had `/api/v1/vehicles`), trimmed `VehicleStatus` down to
  the two values the code actually uses (`AVAILABLE`/`BOOKED`), and fixed the search
  parameters to match the real ones (`minAge`/`maxDurationMinutes`, not `minPersons`).
- **BC-03 Booking**: we moved `paymentMethod` off the `EndBookingRequest`/booking-creation
  boundary to where it actually belongs, as a parameter to `endBooking`, not something
  stored on the aggregate or supplied at creation time. We also dropped a `vehicleSearch`
  interface we'd modeled that doesn't correspond to any real bc03 endpoint (the gateway
  calls Fleet Management directly for search, it doesn't go through Booking).
- **BC-04 Payment**: we modeled `PaymentMethod` as the value object it actually is in the
  code (`type` + `maskedReference`), not an enum. We cut `PaymentStatus` back to three
  values (no `REFUNDED`, which never existed in the implementation) and removed a
  `cancelPayment` operation that isn't a real endpoint, and added the real `retry` endpoint
  that was missing.
- **BC-05 Rating**: we removed a `Score` wrapper value object we'd designed but never
  actually built (the real `Review` structure just uses plain integers), fixed the
  repository method names to match the real field name (`ratingTarget`, not `target`), and
  removed a `/provider/{id}/average` endpoint that doesn't exist. Only vehicle scores get
  averaged in the real code.
- **Infrastructure** (`eureka-server`, `infra-config-server`, `infra-api-gateway`): we
  fixed the Maven group id (`com.winx`, not `com.thewinx`) and the JAR version
  (`0.0.1-SNAPSHOT`, matching the actual `pom.xml`, not `0.1.0-SNAPSHOT`) in all three.

Across every context, we also corrected the H2 datasource URLs to match the real
`application.yml` values (missing `db` suffixes and `;DB_CLOSE_DELAY=-1`), and fixed
`HibernateConfiguration` to say `update` where the model had wrongly said `create-drop`.

## How to open and validate

We followed Assignment 06's Task 1: install LEMMA in an Eclipse instance (the ILIAS course
folder has the setup video), then import this `lemma` folder as an existing Eclipse
project (the `.project` file here is already set up for that). Eclipse's LEMMA editors give
us syntax highlighting and live validation for `.data`, `.services`, `.operation`, and
`.mapping` files, so any error markers on import mean something regressed.

