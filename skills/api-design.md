# API Design Skill

## URL Conventions
- Base: `/api`
- Admin: `/api/admin/{resource}` | Customer: `/api/{resource}` | Auth: `/api/auth/{action}`
- Use plural nouns: `/cities`, `/theaters`, `/bookings`
- Nest resources where natural: `/cities/{cityId}/theaters`
- Path params for identity: `/{id}` | Query params for filtering: `?cityId=1&date=2026-07-26`

## HTTP Methods
| Method | Purpose | Response Code |
|--------|---------|---------------|
| GET | Read/List | 200 OK |
| POST | Create | 201 Created |
| PUT | Full update | 200 OK |
| DELETE | Remove | 204 No Content |

## Request/Response Format
- Content-Type: `application/json`
- Request DTOs: validated with `@Valid` + Jakarta annotations
- Response DTOs: separate from entities

## Error Response Format
```json
{"timestamp":"2026-07-26T12:00:00Z","status":409,"error":"Conflict","message":"One or more seats are no longer available","path":"/api/bookings/hold"}
```

## Standard Error Codes
| Scenario | HTTP Status | Error |
|----------|-------------|-------|
| Seat already held/booked | 409 Conflict | SeatUnavailableException |
| Hold expired | 410 Gone | HoldExpiredException |
| Invalid discount code | 400 Bad Request | InvalidDiscountCodeException |
| Refund not allowed | 422 Unprocessable | RefundNotAllowedException |
| Resource not found | 404 Not Found | ResourceNotFoundException |
| Unauthorized | 401 Unauthorized | — |
| Forbidden (wrong role) | 403 Forbidden | AccessDeniedException |
| Validation failure | 400 Bad Request | MethodArgumentNotValidException |
| Optimistic lock conflict | 409 Conflict | OptimisticLockingFailureException |

## Authentication
- JWT Bearer token in `Authorization` header
- Public: `/api/auth/register`, `/api/auth/login`
- Admin endpoints: require `ROLE_ADMIN` | Customer endpoints: require authentication

## Pagination
```
GET /api/shows?page=0&size=20&sort=date,asc
```
Response includes pagination metadata when applicable.

## Validation Annotations Used
- `@NotNull`, `@NotBlank`, `@Email` — field presence
- `@Size(min, max)` — string length
- `@Positive`, `@Min`, `@Max` — numeric bounds
- `@Future` — date must be in future (show scheduling)
- Custom validators where needed
