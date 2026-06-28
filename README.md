# TheWinx-SS2026 / bc-01-identity-access 🔐
## Project Overview 🥑

The <mark> **BC01-Identity-Access** </mark> microservice is a foundational component of the Software Intensive Solution project. It manages user authentication, authorization, and profile management. This service ensures that only authorized users can access specific fleet resources and provides a secure gateway for the entire system.

## Architecture 🔨

This project follows **Domain-Driven Design (DDD)** principles to ensure a clear separation of concerns:


**API**: Contains REST Controllers and Data Transfer Objects (DTOs) for external communication.

**Application**: Implements the business logic and service layer (e.g., IdentityAccessService, FleetService).

**Domain**: The core of the system, containing entities like UserAccount, Role, and AccountStatus.

**Infrastructure**: Handles data persistence (Repositories) and external service integrations (FleetClient).

**Web**: Manages the UI controllers for the frontend templates.

## Key Features 🗝

- User Authentication: Secure login and registration system.

- Role-Based Access Control (RBAC): Manage user permissions and roles.

- Fleet Integration: Connected fleet management with login capabilities.

- User Dashboard: A dedicated interface for users to manage their profiles and bookings.

- Global Exception Handling: Centralized error management for a robust API.

## Tech Stack 👌🏻

- Backend: Java, Spring Boot

- Frontend: Thymeleaf (HTML/CSS/JS)

- Database: Spring Data JPA


## API Endpoints (/api/v1/identity) 📡

### User Management Endpoints 👤
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :---: |
| `POST` | `/api/v1/identity/users` | Register a new user account | No |
| `GET` | `/api/v1/identity/users` | List all registered users | Yes |
| `GET` | `/api/v1/identity/users/{userId}` | Get profile details for a specific user | Yes |
| `PUT` | `/api/v1/identity/users/{userId}` | Update user profile (username/email) | Yes |
| `DELETE` | `/api/v1/identity/users/{userId}` | Deactivate a user account (Sets status to `INACTIVE`) | Yes |

### Authentication & Authorization 🔐
| Method | Endpoint | Description | Return Type |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/identity/auth/login` | Authenticate user via SHA-256 hash | `AuthResponse` |
| `GET` | `/api/v1/identity/permissions/check` | Verify if a user has a specific permission | `Boolean` |
| `POST` | `/api/v1/identity/users/{userId}/roles` | Assign a security role (e.g., `ADMIN`) | `UserResponse` |
| `DELETE` | `/api/v1/identity/users/{userId}/roles` | Revoke a security role from a user | `UserResponse` |

### Fleet Integration (Proxy Endpoints) 🚗
| Method | Endpoint | Description | Service Source |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/identity/fleet/vehicles` | List all available vehicles | `FleetClient` |
| `GET` | `/api/v1/identity/fleet/bookings` | List bookings (Filter by `username` optional) | `FleetService` |
| `POST` | `/api/v1/identity/fleet/bookings` | Create a new vehicle booking | `FleetService` |
| `POST` | `/api/v1/identity/fleet/bookings/{id}/cancel` | Cancel an active booking | `FleetService` |


## Configuration & Infrastructure ⚙️

The service relies on a centralized configuration server. Key parameters in application.yml include:

  **Eureka Discovery**: Registered at `http://localhost:8761/eureka/`

  **Circuit Breaker**:

  `failureRateThreshold`: <mark>50%</mark>

  `waitDurationInOpenState`: <mark>10s</mark>

**Persistence**: Managed via UserAccount entity with EAGER role fetching for performance.

## Getting Started 🧠


  #### 1️⃣Clone the repository:
    bash
    git clone <https://github.com/samaalzoubi/TheWinx-SS2026.git>


  #### 2️⃣Configure Environment: 
  Check `src/main/resources/application.yml` for database and server configurations.

  #### 3️⃣Run the Application:
    bash
    ./mvnw spring-boot:run


  #### 4️⃣Access the UI: 
Open your browser and navigate to http://localhost:8080 

  #### 5️⃣Communication: 
Client (for internal microservice communication) 




