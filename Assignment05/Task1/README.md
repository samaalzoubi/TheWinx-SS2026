# Task 1: standalone bounded contexts

*DMSA Lab Assignment 05, Task 1. Instant Mobility Platform. Team: The Winx.*

Each of the 5 bounded contexts from [Assignment 04](../../Assignment04/) runs here as its
own independent Spring Boot service: its own H2 database, its own REST API, and its own
lightweight Thymeleaf UI. Where a service needs data from another bounded context, for
example Booking needs vehicle data from Fleet Management, we used a `Mock*Client` seeded
with example data instead of a real network call, exactly as this task allows. We wired up
the real inter-service calls in [Task 2](../Task2/).

## How to run it

You need a JDK between 17 and 21 installed first. Check with `java -version`.

### macOS or Linux, using the script

```bash
./start.sh          # or: ./start.sh start
./start.sh status
./start.sh stop
./start.sh urls
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
running it.

### Running each service by hand

If you would rather not use the script, or it does not work in your setup, run these
directly instead, each in its own terminal:

```bash
./mvnw -pl bc01-identity-access spring-boot:run     # 8081
./mvnw -pl bc02-fleet-management spring-boot:run    # 8082
./mvnw -pl bc03-booking spring-boot:run              # 8083
./mvnw -pl bc04-payment spring-boot:run              # 8084
./mvnw -pl bc05-rating spring-boot:run               # 8085
```

On Windows, replace `./mvnw` with `mvnw.cmd` in each command. Each service is fully
independent (its own H2 database, mock clients standing in for the other bounded
contexts), so you only need to start the one or two you actually want to test, in any
order. Maven compiles the module itself the first time you run it, so there is no
separate build step to run first.

## Seeded test accounts (bc01)

| Role | Email | Password |
|---|---|---|
| User | `marianne@instant-mobility.example` | `password123` |
| User | `rowena@instant-mobility.example` | `password123` |
| User | `priyanka@instant-mobility.example` | `password123` |
| Provider | `sama@providers.instant-mobility.example` | `password123` |
| Provider | `mae@providers.instant-mobility.example` | `password123` |

`bc02` also seeds around 5 sample vehicles, `bc04` seeds a couple of sample payments (one
PAID, one FAILED), and `bc05`'s mock booking client recognizes booking ids `5001` and
`5002` (COMPLETED) and `5003` (ACTIVE, useful for testing the "must be completed"
rejection).

## Bounded contexts

| Service | Details | UI | API docs |
|---|---|---|---|
| BC-01 Identity & Access | [README](bc01-identity-access/README.md) | [localhost:8081/ui](http://localhost:8081/ui) | [localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html) |
| BC-02 Fleet Management | [README](bc02-fleet-management/README.md) | [localhost:8082/ui](http://localhost:8082/ui) | [localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html) |
| BC-03 Booking | [README](bc03-booking/README.md) | [localhost:8083/ui](http://localhost:8083/ui) | [localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html) |
| BC-04 Payment | [README](bc04-payment/README.md) | [localhost:8084/ui](http://localhost:8084/ui) | [localhost:8084/swagger-ui.html](http://localhost:8084/swagger-ui.html) |
| BC-05 Rating | [README](bc05-rating/README.md) | [localhost:8085/ui](http://localhost:8085/ui) | [localhost:8085/swagger-ui.html](http://localhost:8085/swagger-ui.html) |

Each linked README covers that context's domain model, requirements coverage, a
file-by-file breakdown, and how to test it on its own. The H2 console for any service
lives at `http://localhost:<port>/h2-console`.

Since every service is standalone here, there is no combined dashboard yet. That only
exists in [Task 2](../Task2/)'s `infra-api-gateway`.
