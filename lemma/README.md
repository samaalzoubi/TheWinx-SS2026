# LEMMA models (Lab Assignment 06)

This folder models our architecture in LEMMA, per Lab Assignment 06. It sits at the top of
the repository, alongside the actual Java source it describes in `Assignment05/Task2`.

We modeled Task 2, the fully integrated system (Eureka, Config Server, the 5 bounded
contexts, and the API gateway), since that's the version where deployment and
service-discovery relationships actually exist to model. Task 1's standalone services share
the same domain and REST API, so nothing here would change if we'd modeled that version
instead.

## What's here

One domain model, one service model, one deployment model, and one technology mapping per
bounded context, plus three infrastructure-only deployment models for Eureka, the Config
Server, and the API gateway. Every model matches the real Task 2 source code: field names,
enum values, REST paths, ports, and configuration all come straight from what's actually
implemented.

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
models from ILIAS that Assignment 06 tells us to reuse, we only reference them from our
own `.operation` and `.mapping` files.

## File roles

Each bounded context has four files, and they build on each other in this order:

1. **`.data`** (Domain Data Modeling Language): the domain concepts for that context,
   structures, value objects, enums, repositories, and application services, tagged with
   DDD features like `<aggregate>`, `<entity>`, `<valueObject>`, `<part>`. Since we hadn't
   learned how to attach technology information directly to domain models this semester,
   we added the real JPA annotations as comments instead (for example `// @Entity`,
   `// @Embedded`), right above the field or structure they apply to.
2. **`.services`** (Service Modeling Language): the microservice's interfaces and
   operations, imported from the `.data` file. Each operation is documented with a `---`
   comment block describing its purpose and parameters.
3. **`.operation`** (Operation Modeling Language): wraps the microservice in a Docker
   container, points the Dockerfile at the real JAR name and version, and declares which
   infrastructure nodes (Eureka, Config Server) it depends on via `depends on nodes`.
4. **`.mapping`** (Technology Mapping Language): maps each interface and operation onto
   REST endpoints, HTTP verbs, and Spring aspects (`@RequestParam`, `@PathVariable`,
   `@ResponseStatus`, and so on), plus the service's port, datasource, and Hibernate
   configuration.

## Per-context summary

- **BC-01 Identity & Access**: two aggregates, `UserAccount` and `ProviderAccount`, plus
  `AuthToken`/`PrincipalRef` for the bearer-token mechanism. Faults modeled on
  register/login/getById: `emailAlreadyRegistered`, `invalidCredentials`, `notFound`.
- **BC-02 Fleet Management**: the `Vehicle` aggregate with three embedded value objects
  (`VehicleLocation`, `PricingPolicy`, `UsageRestrictions`). REST base path is
  `/api/vehicles`, `VehicleStatus` has two values (`AVAILABLE`/`BOOKED`), and search takes
  `minAge`/`maxDurationMinutes` filters.
- **BC-03 Booking**: `paymentMethod` is a parameter of `endBooking`, not something stored
  on the aggregate or supplied at creation time, matching how payment actually gets chosen
  in the real flow.
- **BC-04 Payment**: `PaymentMethod` is modeled as a value object (`type` +
  `maskedReference`), not an enum. `PaymentStatus` has three values (`PENDING`/`PAID`/
  `FAILED`), and the `retry` operation is included alongside create/get.
- **BC-05 Rating**: a `Score` value object wraps the 1-5 rating value, validated on
  construction. Repository methods are named after the real field (`ratingTarget`, not
  `target`), and only vehicle scores get averaged, there's no provider-level average in the
  real code.
- **Infrastructure** (`eureka-server`, `infra-config-server`, `infra-api-gateway`): Maven
  group id `com.winx` and JAR version `0.0.1-SNAPSHOT`, matching the actual `pom.xml`.

Across every context, the H2 datasource URLs and `HibernateConfiguration` match the real
`application.yml` values.

