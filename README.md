# Movie Ticket Booking System

A backend system for booking movie tickets at scale, built with Spring Boot. Supports multiple cities, theaters, screens, and shows with seat-level booking, time-bound holds, configurable pricing, discount codes, refund policies, and async notifications.

## Tech Stack

| Technology | Purpose |
|-----------|---------|
| Java 17 | Language |
| Spring Boot 3.2.5 | Application framework |
| Spring Data JPA + Hibernate | ORM / Data access |
| Spring Security + JWT | Authentication & RBAC |
| H2 Database | Default (in-memory, zero-config) |
| PostgreSQL | Optional production-like profile |
| Lombok | Boilerplate reduction |
| JUnit 5 + Mockito | Testing |

## Project Structure

```
movie-ticket-booking/
├── README.md                    # This file
├── requirements.md              # Functional & non-functional requirements
├── design.md                    # HLD, LLD, ER diagrams (Mermaid)
├── agents.md                    # AI-assisted development workflow & prompt log
├── skills/                      # AI skill files used during development
│   ├── system-design.md
│   ├── spring-boot.md
│   ├── testing.md
│   ├── api-design.md
│   └── prompt-orchestration.md
├── pom.xml                      # Maven build configuration
├── mvnw / mvnw.cmd             # Maven wrapper
└── src/
    ├── main/
    │   ├── java/com/moviebooking/
    │   │   ├── MovieTicketBookingApplication.java   # Entry point
    │   │   ├── config/          # Security, Async, Scheduling configs
    │   │   ├── controller/
    │   │   │   ├── admin/       # Admin REST endpoints (CRUD)
    │   │   │   └── customer/    # Customer REST endpoints (browse, book, cancel)
    │   │   ├── dto/
    │   │   │   ├── request/     # Incoming request payloads
    │   │   │   └── response/    # Outgoing response payloads
    │   │   ├── entity/          # 14 JPA entities
    │   │   ├── enums/           # 9 enum types (Role, SeatType, Status, etc.)
    │   │   ├── event/           # Spring application events
    │   │   ├── exception/       # Custom exceptions + GlobalExceptionHandler
    │   │   ├── listener/        # @Async event listeners (notifications)
    │   │   ├── repository/      # 14 Spring Data JPA repositories
    │   │   ├── scheduler/       # @Scheduled tasks (hold expiry, reminders)
    │   │   ├── security/        # JWT filter, util, UserDetailsService
    │   │   └── service/         # 10 service classes (business logic)
    │   └── resources/
    │       ├── application.yml          # Default config (H2)
    │       └── application-postgres.yml # PostgreSQL profile
    └── test/
        └── java/com/moviebooking/      # Unit & integration tests
```

## Prerequisites

- **Java 17+** (Amazon Corretto 17, OpenJDK 17, or similar)
- **Maven 3.9+** (included via wrapper — no separate install needed)
- **Git**

Verify Java version:
```bash
java -version
# Should show: openjdk version "17.x.x" or similar
```

If you have multiple Java versions, set JAVA_HOME:
```bash
export JAVA_HOME=/path/to/jdk-17
```

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/sswatisuman04-pixel/movie-ticket-booking.git
cd movie-ticket-booking
```

### 2. Build the project

```bash
./mvnw clean package
```

This will:
- Download dependencies
- Compile source code
- Run all tests
- Package into a JAR

To skip tests during build:
```bash
./mvnw clean package -DskipTests
```

### 3. Run the application

```bash
./mvnw spring-boot:run
```

The server starts at: **http://localhost:8080**

### 4. Verify it's running

- **H2 Console:** http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:moviedb`
  - Username: `sa`
  - Password: _(empty)_

- **API Health check:** Try any GET endpoint after adding data

## Running with PostgreSQL (Optional)

If you prefer PostgreSQL over H2:

1. Create a database:
```sql
CREATE DATABASE moviedb;
```

2. Run with the postgres profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

3. Default connection:
   - Host: localhost:5432
   - Database: moviedb
   - Username: postgres
   - Password: postgres

Edit `src/main/resources/application-postgres.yml` to change credentials.

## Running Tests

```bash
# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=BookingServiceTest

# Run with verbose output
./mvnw test -Dsurefire.useFile=false
```

## API Overview

Once the server is running, the following API groups are available:

| Group | Base Path | Auth Required |
|-------|-----------|---------------|
| Auth | `/api/auth/*` | No |
| Admin | `/api/admin/*` | Yes (ADMIN role) |
| Customer | `/api/*` | Yes (any authenticated user) |

### Quick Start Flow

1. **Register:** `POST /api/auth/register`
2. **Login:** `POST /api/auth/login` → get JWT token
3. **Browse shows:** `GET /api/shows?cityId=1`
4. **View seats:** `GET /api/shows/{showId}/seats`
5. **Hold seats:** `POST /api/bookings/hold`
6. **Confirm booking:** `POST /api/bookings/confirm`

> Full API documentation is in [`design.md`](./design.md#14-api-overview).

## Key Design Decisions

- **Optimistic locking** on seat bookings to prevent double-allocation
- **5-minute seat holds** with automatic expiry via scheduled cleanup
- **Event-driven notifications** (non-blocking, async)
- **Configurable refund policies** per theater with sliding-scale percentages
- **Pricing tiers** computed at show-creation time (immutable after that)

> See [`design.md`](./design.md#4-design-decisions) for full rationale.

## Assumptions

1. Payment is simulated (no real gateway integration)
2. Notifications are logged (no real email/SMS provider)
3. Single-instance deployment (no distributed locking needed)
4. Admin user is pre-seeded on startup
5. One discount code per booking maximum

> See [`design.md`](./design.md#5-assumptions) for the complete list.

## Documentation

| Document | Description |
|----------|-------------|
| [requirements.md](./requirements.md) | Functional & non-functional requirements |
| [design.md](./design.md) | System design with Mermaid diagrams |
| [agents.md](./agents.md) | AI workflow & prompt log |
| [skills/](./skills/) | AI skill files used during development |
