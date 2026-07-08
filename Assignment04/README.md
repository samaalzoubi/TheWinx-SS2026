DMSA Lab Assignment 04 | Instant Mobility Platform | Team: The Winx

# Lab Assignment 04 — DDD: Tactical Design, Event Storming

**Full deliverable:** [`DMSA - Lab Assignment 04.pdf`](./DMSA%20-%20Lab%20Assignment%2004.pdf)
**Event storming board (rendered):** [`event_storming_instant_mobility.html`](./event_storming_instant_mobility.html)

## Task 1: Tactical Design

Each bounded context from Assignment 03 was assigned to one team member, who owns its tactical DDD design, UML class diagram, and later its implementation:

| BC | Context | Owner | Implementation |
|---|---|---|---|
| BC-01 | Identity & Access | Sama Alzoubi | Full |
| BC-02 | Fleet Management | Priyanka Gupta | Full |
| BC-03 | Booking | Rowena Pagayanan | Full |
| BC-04 | Payment | Marianne Nosseir | Full |
| BC-05 | Rating | Mae Eskandari Borujerdi | Full (remaining contexts mocked) |

UML stereotypes used throughout: `<<AggregateRoot>>`, `<<Entity>>`, `<<ValueObject>>`, `<<Service>>`, `<<Repository>>`, `<<DomainEvent>>` (Paper 01 Table 1 / Paper 02 Table 3 conventions).

### Per-context summary

- **BC-01 Identity & Access** — Aggregates `UserAccount`, `ProviderAccount`; VOs `PersonalInfo`, `CompanyInfo`, `Email`, `Password`, `AuthToken`; services `AuthenticationService`, `RegistrationService`; events `UserRegistered`, `ProviderRegistered`, `UserAuthenticated`.
- **BC-02 Fleet Management** — Aggregate `Vehicle` (VOs `VehicleLocation`, `PricingPolicy`, `UsageRestrictions`); services `VehicleRegistrationService`, `FleetStatusService`, `VehicleAvailabilityService`; events `VehicleCreated`, `VehicleStatusUpdated`, `VehicleLocationUpdated`, `VehicleDeleted`.
- **BC-03 Booking** — Aggregate `Booking` (VOs `RideLocation`, `TimeInterval`, `VehicleSnapshot`, `RideSummary`); services `VehicleSearchService`, `BookingService`, `CostCalculationService`, `RestrictionValidator`; events `BookingCreated`, `BookingCompleted`, `BookingCancelled`, `PaymentTriggered`.
- **BC-04 Payment** — Aggregate `Payment` (VOs `Money`, `PaymentMethod`, `PaymentResult`); services `PaymentProcessingService`, `PaymentGatewayAdapter`; events `PaymentInitiated`, `PaymentSucceeded`, `PaymentFailed`.
- **BC-05 Rating** — Aggregate `Rating` (VOs `Score`, `Review`, `RatingTarget`); services `RatingSubmissionService`, `RatingQueryService`; event `RatingSubmitted`. Key invariants: score 1–5, exactly one rating per booking, only after the booking is `COMPLETED`, immutable once submitted.

Full attribute tables, invariants, and PlantUML source for all five class diagrams are in the PDF §1.3.

## Task 2: Event Storming

The team chose **Event Storming** (over Domain Storytelling) to walk the domain end-to-end as a timeline of domain events, commands, aggregates, and policies across 5 phases: Registration → Vehicle Management → Search & Booking → Ride & Completion → Rating. Full event flow table, narrative, and board in PDF §2 / `event_storming_instant_mobility.html`.

## Status

Complete. This is the **authoritative domain specification** for [Assignment 05](../Assignment05/) — each owner implements their aggregates, value objects, services, repositories, and domain events as specified here.
