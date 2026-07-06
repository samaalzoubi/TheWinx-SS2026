DMSA Lab Assignment 05 | Instant Mobility Platform | Team: The Winx

# Lab Assignment 05 — Implementation of the Microservice Architecture

Implements all 5 bounded contexts from [Assignment 04](../Assignment04/) as Spring Boot microservices, split into the two tasks the assignment defines:

## [Task1/](./Task1/) — Bounded Contexts as standalone microservices

Each bounded context runs and is fully testable **on its own**: Spring Boot + Spring MVC + REST, an H2 database, a rudimentary Thymeleaf CRUD UI, and Swagger/OpenAPI docs. Where a service would normally need data from another bounded context (e.g. Booking needs Vehicle data from Fleet Management), it uses a `Mock*Client` seeded with example data instead of a real network call — exactly as the assignment allows for Task 1.

| Module | Port | Owner | Bounded Context |
|---|---|---|---|
| `bc01-identity-access` | 8081 | Sama Alzoubi | Identity & Access |
| `bc02-fleet-management` | 8082 | Priyanka Gupta | Fleet Management |
| `bc03-booking` | 8083 | Rowena Pagayanan | Booking |
| `bc04-payment` | 8084 | Marianne Nosseir | Payment |
| `bc05-rating` | 8085 | Mae Eskandari Borujerdi | Rating |

## [Task2/](./Task2/) — Integrated system

A copy of the Task 1 services, wired into one real, running platform:

- **Service discovery** via Eureka (`infra-eureka-server`, port 8761)
- **Centralized configuration** via Spring Cloud Config (`infra-config-server`, port 8888, serving `Task2/config/`)
- **Resilient inter-service calls**: the `Mock*Client`s from Task 1 are replaced with real `@FeignClient`s resolved through Eureka, guarded by **Resilience4j** circuit breakers
- A user can walk the full happy path across real services: register → log in → search vehicles → book → end ride → pay → rate.

See `Task2/README.md` for how to boot the whole system and exercise that flow.

## Common conventions (both tasks)

- Java 21 (compiled for Java 17 bytecode — Spring Boot 3.2.5 requirement), Spring Boot 3.2.5, Maven (wrapper `./mvnw` committed, no global Maven needed).
- Base package per service: `com.winx.<bc>` (`identity`, `fleet`, `booking`, `payment`, `rating`).
- Per-module layout:
  ```
  com.winx.<bc>
  ├── api/            REST controllers, DTOs (Java records), GlobalExceptionHandler
  │   └── ui/         @Controller for Thymeleaf pages
  ├── domain/         @Entity aggregates/entities, @Embeddable value objects, domain events
  ├── application/    @Service domain/application services
  ├── infrastructure/ JpaRepository interfaces, REST/Feign clients to other contexts
  └── config/         OpenAPI / Feign / Resilience config
  ```
- Domain model, aggregates, value objects, invariants, and REST responsibilities per context come from [Assignment 04](../Assignment04/DMSA_Lab_Assignment_04.pdf) (authoritative spec) and the functional requirements in [Assignment 02](../Assignment02/).
