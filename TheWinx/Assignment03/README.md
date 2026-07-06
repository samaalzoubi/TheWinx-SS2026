DMSA Lab Assignment 03 | Instant Mobility Platform | Team: The Winx

# Lab Assignment 03 — DDD: Bounded Contexts & Context Mapping

**Full deliverable:** [`DMSA_Lab_Assignment_03.pdf`](./DMSA_Lab_Assignment_03.pdf)
**Context map diagram (source):** [`instant_mobility_context_map.svg`](./instant_mobility_context_map.svg)

## Task 1: Identify Bounded Contexts

Five bounded contexts were carved out of the Assignment 02 domain model, grouped by cohesive business capability:

| ID | Context | Responsibility | Key concepts |
|---|---|---|---|
| BC-01 | **Identity & Access** (Purple) | Auth & account management for Users and Providers | User (identity), Provider (identity) |
| BC-02 | **Fleet Management** (Teal) | Vehicle CRUD, pricing, restrictions, status tracking | Provider (owner), Vehicle, VehicleType, VehicleStatus, BillingModel |
| BC-03 | **Booking** (Blue) | Vehicle search, booking lifecycle, cost computation, payment trigger | User (customer), Vehicle (availability), Booking, BookingStatus |
| BC-04 | **Payment** (Amber) | Financial transaction processing for completed bookings | Booking (ref), Payment, PaymentStatus |
| BC-05 | **Rating** (Coral/Red) | User feedback on vehicles and providers post-booking | User (author), Vehicle (ref), Provider (ref), Booking (ref), Rating |

Domain concepts that recur across contexts (e.g. `User`, `Vehicle`, `Booking`) intentionally carry only the attributes relevant to that context's responsibility — full detail in PDF §2.1.

## Task 2: Context Mapping (Vernon's Notation)

- **Identity & Access** is the global upstream: every other context authenticates against it via Open Host Service / Published Language (OHS/PL), wrapped in an Anti-Corruption Layer (ACL) downstream.
- **Fleet Management** is upstream to Booking and Rating (OHS/PL → ACL) for vehicle/provider data.
- **Booking** is the central operational context — downstream consumer of Identity & Fleet, and upstream supplier to Payment and Rating via a negotiated Customer/Supplier (C/S) relationship, with Payment and Rating as Conformists (CF) since Booking's output model is stable.
- **Payment** and **Rating** are pure leaf contexts: they consume but produce nothing consumed elsewhere.

Full relationship table and narrative in PDF §3.2–§3.3; diagram in PDF §3 / `instant_mobility_context_map.svg`.

## Status

Complete. Each bounded context maps 1:1 to a microservice in [Assignment 04](../Assignment04/) tactical design and [Assignment 05](../Assignment05/) implementation.
