# Spring Boot Skill

## Project Structure
```
src/main/java/com/moviebooking/
├── config/          # Security, Async, Scheduling configs
├── controller/
│   ├── admin/       # Admin endpoints (@PreAuthorize ADMIN)
│   └── customer/    # Customer endpoints (@PreAuthorize CUSTOMER)
├── dto/
│   ├── request/     # Incoming request DTOs
│   └── response/    # Outgoing response DTOs
├── entity/          # JPA entities
├── enums/           # BookingStatus, SeatType, Role, etc.
├── exception/       # Custom exceptions + @RestControllerAdvice handler
├── event/           # Spring application events
├── listener/        # @Async event listeners
├── repository/      # Spring Data JPA repositories
├── security/        # JWT filter, util, UserDetailsService
├── service/         # Business logic
└── scheduler/       # @Scheduled tasks
```

## Coding Conventions
- Constructor injection (no @Autowired on fields)
- Lombok: @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor
- DTOs separate from entities — never expose entities in responses
- Services are @Transactional where needed
- Controllers return ResponseEntity<>
- Validation via @Valid on request DTOs
- Use Optional returns from repositories — handle empty with exceptions

## Naming Conventions
- Entities: singular (Booking, Show, Theater)
- Tables: snake_case plural (bookings, shows, theaters)
- DTOs: {Action}{Entity}Request/Response (CreateShowRequest, BookingResponse)
- Services: {Entity}Service
- Repositories: {Entity}Repository
- Controllers: {Domain}Controller (AdminShowController, CustomerBookingController)
- Enums: PascalCase type, UPPER_SNAKE values

## Configuration
- Default: H2 in-memory (application.yml)
- PostgreSQL: application-postgres.yml profile
- JWT secret + expiry in application.yml
- Hold duration: configurable via app.booking.hold-duration-minutes

## Build & Run
```bash
./mvnw clean package          # Build
./mvnw spring-boot:run         # Run (H2)
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres  # Run (PG)
./mvnw test                    # Tests
```

## Do NOT
- Field injection (@Autowired on fields)
- Expose entities in API responses
- Business logic in controllers
- Raw SQL (prefer JPQL or derived queries)
- Catch generic Exception
- Hardcode config values
- Circular service dependencies
- Ignore Optional.empty()
