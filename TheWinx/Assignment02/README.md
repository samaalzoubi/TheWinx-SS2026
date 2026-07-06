DMSA Lab Assignment 02 | Instant Mobility Platform | Team: The Winx

# Lab Assignment 02 — Documentation & Requirements

**Full deliverable:** [`DMSA - Lab Assignment 02.pdf`](./DMSA%20-%20Lab%20Assignment%2002.pdf)
**Domain model diagram (rendered):** [`instant_mobility_domain_model_erd.html`](./instant_mobility_domain_model_erd.html)

## Task 1: Set Up Project Documentation in GitLab/GitHub

Project wiki structure created following the `03a - Template: Project Documentation` skeleton (Summary, Functionality, Architecture design, Implementation).

## Task 2: Requirements Analysis

### 1. Functional requirements

15 requirements (R01–R15) documented with ID, name, description, priority, and actors, split into **User side** (registration, login, search, filter, book, end booking, payment, rating, booking history) and **Provider side** (registration, login, fleet CRUD, pricing model, restrictions, fleet status). Full table in the PDF, §2.2.

Actors: **User** (customer), **Provider** (fleet owner), **System** (internal automation for status updates, cost computation, payment triggering).

### 2. Domain model

Core entities: `User`, `Provider`, `Vehicle`, `Booking`, `Payment`, `Rating`.

- Location and usage-restriction data are embedded as attributes on `Vehicle`/`Booking` rather than modeled as separate entities, to avoid unnecessary join complexity.
- Enumerations: `VehicleType` (E_SCOOTER, BICYCLE, E_BIKE, E_CAR), `VehicleStatus` (AVAILABLE, BOOKED), `BillingModel` (PER_HOUR, PER_KILOMETER), `BookingStatus` (ACTIVE, COMPLETED, CANCELLED), `PaymentStatus` (PENDING, PAID, FAILED).
- Relationships use Composition (`*--`) where the child cannot outlive the parent (Provider→Vehicle, User→Booking, Booking→Payment, Booking→Rating) and Association (`-->`) where entities exist independently (Vehicle→Booking, User→Rating, Rating→Vehicle/Provider).
- PlantUML source for the class diagram is included in the PDF (§3.5) and can be pasted into plantuml.com to regenerate the diagram.

## Status

Complete. This model is the basis for bounded-context identification in [Assignment 03](../Assignment03/) and tactical design in [Assignment 04](../Assignment04/).
