# Requirements: Movie Ticket Booking System

## Project Overview

This is an SDE-2 level take-home assignment to build a movie ticket booking system that operates at scale. The system supports multiple cities, each containing multiple theaters with multiple screens and configurable seat layouts. It handles concurrent seat bookings, time-bound seat holds, multiple pricing tiers, discount codes, payments, refunds, and asynchronous notifications.

- **Time Constraint:** 48 hours
- **Stack Requirement:** Spring Boot

---

## Functional Requirements

### FR1: City & Theater Management (Admin)

- **FR1.1:** The system shall allow an admin to create a city with a unique name.
- **FR1.2:** The system shall allow an admin to list all cities.
- **FR1.3:** The system shall allow an admin to update a city's details.
- **FR1.4:** The system shall allow an admin to create a theater within a city, specifying the theater name and location.
- **FR1.5:** The system shall allow an admin to list all theaters in a given city.
- **FR1.6:** The system shall allow an admin to update a theater's details.
- **FR1.7:** The system shall enforce that theater names are unique within a city.

### FR2: Screen & Seat Layout Management (Admin)

- **FR2.1:** The system shall allow an admin to create a screen within a theater, specifying a screen identifier.
- **FR2.2:** The system shall allow an admin to define a seat layout for a screen, specifying rows, columns, and seat categories.
- **FR2.3:** The system shall allow an admin to list all screens in a given theater.
- **FR2.4:** The system shall support multiple seat categories per screen (e.g., regular, premium, VIP).
- **FR2.5:** The system shall allow an admin to update the seat layout of a screen.
- **FR2.6:** The system shall enforce that screen identifiers are unique within a theater.

### FR3: Show Management (Admin)

- **FR3.1:** The system shall allow an admin to schedule a show on a specific screen, specifying movie name, start time, and end time.
- **FR3.2:** The system shall prevent scheduling overlapping shows on the same screen.
- **FR3.3:** The system shall allow an admin to list all shows for a given screen.
- **FR3.4:** The system shall allow an admin to cancel a scheduled show.
- **FR3.5:** The system shall associate a pricing tier with each show.

### FR4: Pricing Tier Management (Admin)

- **FR4.1:** The system shall allow an admin to create pricing tiers with a name and price per seat category (e.g., regular, premium, weekend).
- **FR4.2:** The system shall allow an admin to list all pricing tiers.
- **FR4.3:** The system shall allow an admin to update a pricing tier's prices.
- **FR4.4:** The system shall allow an admin to delete a pricing tier that is not associated with any active show.
- **FR4.5:** The system shall support different prices for different seat categories within the same pricing tier.

### FR5: Discount Code Management (Admin)

- **FR5.1:** The system shall allow an admin to create discount codes with a code string, discount type (percentage or flat amount), discount value, and validity period.
- **FR5.2:** The system shall allow an admin to list all discount codes.
- **FR5.3:** The system shall allow an admin to deactivate a discount code.
- **FR5.4:** The system shall allow an admin to set a maximum usage limit on a discount code.
- **FR5.5:** The system shall enforce uniqueness of discount code strings.

### FR6: Refund Policy Management (Admin)

- **FR6.1:** The system shall allow an admin to create configurable refund policies specifying refund percentage and a cancellation deadline (time before show start).
- **FR6.2:** The system shall allow an admin to list all refund policies.
- **FR6.3:** The system shall allow an admin to update a refund policy.
- **FR6.4:** The system shall allow an admin to associate a refund policy with a show or theater.

### FR7: User Registration & Authentication

- **FR7.1:** The system shall allow a user to register with an email, password, and name.
- **FR7.2:** The system shall allow a registered user to log in and receive an authentication token.
- **FR7.3:** The system shall support two roles: Admin and Customer.
- **FR7.4:** The system shall restrict admin-only operations to users with the Admin role.
- **FR7.5:** The system shall validate that email addresses are unique during registration.
- **FR7.6:** The system shall reject requests to protected resources from unauthenticated users.

### FR8: Browse Shows (Customer)

- **FR8.1:** The system shall allow a customer to browse shows by city.
- **FR8.2:** The system shall allow a customer to browse shows by theater.
- **FR8.3:** The system shall allow a customer to browse shows by movie name.
- **FR8.4:** The system shall allow a customer to view available seats for a specific show, including seat category and pricing.
- **FR8.5:** The system shall indicate which seats are available, held, or booked for a given show.

### FR9: Seat Selection & Hold (Customer)

- **FR9.1:** The system shall allow a customer to select one or more available seats for a show and place a time-bound hold on them.
- **FR9.2:** The system shall prevent other customers from selecting seats that are currently held.
- **FR9.3:** The system shall automatically release held seats when the hold duration expires.
- **FR9.4:** The system shall reject hold requests for seats that are already held or booked.
- **FR9.5:** The system shall allow a customer to release their held seats before the hold expires.
- **FR9.6:** The system shall enforce a configurable hold duration.

### FR10: Booking & Payment (Customer)

- **FR10.1:** The system shall allow a customer to confirm a booking for their currently held seats by completing payment.
- **FR10.2:** The system shall calculate the total price based on the show's pricing tier, seat categories, and any applied discount code.
- **FR10.3:** The system shall allow a customer to apply a valid discount code during booking.
- **FR10.4:** The system shall validate that the discount code is active, within its validity period, and has not exceeded its usage limit.
- **FR10.5:** The system shall mark seats as booked upon successful payment.
- **FR10.6:** The system shall reject booking confirmation if the hold has expired.
- **FR10.7:** The system shall generate a unique booking confirmation identifier upon successful booking.
- **FR10.8:** The system shall increment the usage count of a discount code upon successful booking.

### FR11: Booking Cancellation & Refund (Customer)

- **FR11.1:** The system shall allow a customer to cancel a confirmed booking.
- **FR11.2:** The system shall calculate the refund amount based on the applicable refund policy and the time remaining before the show starts.
- **FR11.3:** The system shall release the booked seats upon successful cancellation, making them available for future bookings.
- **FR11.4:** The system shall process the refund and record the refund amount.
- **FR11.5:** The system shall reject cancellation requests if the cancellation deadline defined in the refund policy has passed.

### FR12: Booking History (Customer)

- **FR12.1:** The system shall allow a customer to view their past and upcoming bookings.
- **FR12.2:** The system shall display booking details including show information, seats, amount paid, discount applied, and booking status.
- **FR12.3:** The system shall display cancellation and refund details for cancelled bookings.

### FR13: Notifications

- **FR13.1:** The system shall send a booking confirmation notification to the customer upon successful booking.
- **FR13.2:** The system shall send a reminder notification to the customer before the show starts.
- **FR13.3:** The system shall send notifications asynchronously without blocking the booking flow.
- **FR13.4:** The system shall send a cancellation confirmation notification upon successful booking cancellation.

---

## Non-Functional Requirements

### NFR1: Concurrency & Data Consistency

- **NFR1.1:** The system shall prevent double-allocation of the same seat when multiple users attempt to book it simultaneously.
- **NFR1.2:** The system shall ensure that seat state transitions (available → held → booked) are atomic.
- **NFR1.3:** The system shall handle concurrent hold requests for the same seat gracefully, allowing only one to succeed.
- **NFR1.4:** The system shall maintain data consistency across all booking operations under concurrent load.

### NFR2: Performance

- **NFR2.1:** The system shall handle seat hold expiration without requiring manual intervention.
- **NFR2.2:** The system shall process booking operations without being blocked by notification delivery.
- **NFR2.3:** The system shall respond to browse and search queries efficiently even with a large number of shows and theaters.

### NFR3: Security

- **NFR3.1:** The system shall implement role-based access control (RBAC) to restrict operations by user role.
- **NFR3.2:** The system shall validate all input data and reject malformed or invalid requests with appropriate error messages.
- **NFR3.3:** The system shall not expose sensitive user data (e.g., passwords) in API responses.
- **NFR3.4:** The system shall authenticate users before allowing access to protected resources.

### NFR4: Reliability

- **NFR4.1:** The system shall persist all data to a database to survive application restarts.
- **NFR4.2:** The system shall handle errors gracefully and return meaningful error responses.
- **NFR4.3:** The system shall not lose booking data in the event of a notification delivery failure.
- **NFR4.4:** The system shall ensure expired holds are released even if the system was temporarily unavailable during the expiration window.

### NFR5: Maintainability

- **NFR5.1:** The system shall expose REST APIs for all core flows.
- **NFR5.2:** The system shall follow consistent error response formats across all endpoints.
- **NFR5.3:** The system shall be structured to allow independent modification of business rules (pricing, refund policies, hold duration) without code changes to unrelated modules.

### NFR6: Testability

- **NFR6.1:** The system shall include unit tests covering core business logic.
- **NFR6.2:** The system shall include integration tests covering end-to-end booking flows.
- **NFR6.3:** The system shall include tests verifying concurrent seat booking behavior.
- **NFR6.4:** The system shall include tests verifying seat hold expiration behavior.

---

## Scope

### In Scope

- Multi-city, multi-theater, multi-screen movie ticket booking
- Seat layout configuration with multiple seat categories
- Show scheduling with conflict detection
- Configurable pricing tiers per show and seat category
- Discount code creation, validation, and application
- Time-bound seat holds with automatic expiration
- Booking confirmation with payment processing
- Booking cancellation with configurable refund policies
- Asynchronous notification delivery (confirmation and reminder)
- User registration, authentication, and role-based access control
- Booking history for customers
- Concurrency handling for seat allocation
- REST API for all operations
- Database persistence
- Input validation and error handling
- Unit and integration tests

### Out of Scope

- User interface (frontend/UI)
- Deployment and CI/CD pipelines
- Microservices architecture
- OAuth/SSO integration
- Production observability (monitoring, alerting, distributed tracing)

---

## Submission Requirements

- GitHub repository with multiple meaningful commits showing incremental progress
- README.md documenting setup and usage instructions
- AGENTS.md or Claude.md file describing AI agent usage
- Skills files used during development
- Raw files (prompt logs, conversation exports)
- Video recording (maximum 10 minutes) demonstrating the solution
