# System Design Skill

## Architecture
- Layered architecture: Controller → Service → Repository → Database
- Event-driven async notifications via Spring ApplicationEventPublisher
- Scheduled tasks for background cleanup (hold expiry, reminders)

## Design Patterns Used
- **Optimistic Locking** — @Version on ShowSeat for concurrent booking protection
- **Event-Driven** — ApplicationEventPublisher + @Async listeners for notifications
- **Strategy Pattern** — Refund policy selection based on time-before-show
- **Builder Pattern** — Complex DTOs and entity construction via Lombok @Builder
- **Repository Pattern** — Spring Data JPA interfaces for data access
- **DTO Pattern** — Decouple API contracts from persistence model

## Mermaid Diagram Conventions
- Use `flowchart TD` for architecture/component diagrams
- Use `sequenceDiagram` for flow/interaction diagrams
- Use `erDiagram` for database schema
- All diagrams must render on GitHub (test with GitHub preview)
- Keep diagrams focused — one concept per diagram

## Concurrency Strategy
- Optimistic locking via @Version on ShowSeat entity
- On conflict: return 409 Conflict to client (no server-side retry)
- Hold mechanism: 5-min TTL, @Scheduled cleanup every 30s
- Atomic discount code usage: `UPDATE ... SET currentUses = currentUses + 1 WHERE currentUses < maxUses`

## Database Design Principles
- Normalize to 3NF, denormalize only with justification (e.g., totalSeats on Screen)
- Price stored on ShowSeat at show-creation time (immutable after creation)
- Composite primary key for join tables (BookingSeat)
- Strategic indexes for query-heavy paths (see design.md §3.3)
