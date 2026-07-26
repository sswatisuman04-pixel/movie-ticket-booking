# Testing Skill

## Strategy
- Unit tests: Service layer with Mockito mocks
- Integration tests: @SpringBootTest with H2 in-memory
- Concurrency tests: Simulate parallel seat booking attempts
- Test coverage targets: Core booking flows, edge cases, error paths

## Test Naming
Pattern: `methodName_scenario_expectedBehavior()`

```java
void holdSeats_whenSeatsAvailable_shouldReturnHoldWithExpiry()
void holdSeats_whenSeatAlreadyHeld_shouldThrowConflictException()
void confirmBooking_whenHoldExpired_shouldThrowHoldExpiredException()
void cancelBooking_within24Hours_shouldRefund50Percent()
void applyDiscount_whenCodeExpired_shouldThrowInvalidCodeException()
```

## Test Structure
- Arrange-Act-Assert pattern
- One assertion concept per test (multiple asserts OK if testing same thing)
- Mirror main source structure under src/test/java/

## Unit Test Conventions
```java
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock private ShowSeatRepository showSeatRepository;
    @Mock private BookingRepository bookingRepository;
    @InjectMocks private BookingService bookingService;

    @Test
    void holdSeats_whenSeatsAvailable_shouldSetStatusToHeld() {
        // Arrange
        // Act
        // Assert
    }
}
```

## Integration Test Conventions
```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingControllerIT {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void fullBookingFlow_shouldSucceed() { ... }
}
```

## Key Test Scenarios
- Happy path: hold → confirm → booking created
- Concurrent booking: two users, same seat, one fails with 409
- Hold expiry: hold seats, wait, verify released
- Discount code: valid, expired, over-limit, min amount not met
- Refund: within policy, outside policy, no policy
- Auth: unauthenticated, wrong role, correct role

## Running Tests
```bash
./mvnw test                    # All tests
./mvnw test -Dtest=BookingServiceTest  # Single class
./mvnw verify                  # Full build + tests
```
