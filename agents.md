# AGENTS.md — AI-Assisted Development Workflow

## Overview

This project was developed using an AI-assisted workflow where Claude (Anthropic's AI assistant) served as a pair-programming partner throughout the entire software development lifecycle. The AI was used for requirements analysis, system design, code generation, testing, and documentation — following a structured, design-first approach with incremental implementation.

The goal was not to blindly generate code, but to use AI as a collaborative tool: discussing trade-offs, validating architectural decisions, and iterating on implementations with meaningful, atomic commits.

## AI Tools Used

| Tool | Purpose |
|------|---------|
| Claude (Anthropic) | Primary AI assistant for code generation, architecture, and documentation |

## Development Workflow

The project followed a structured, phased approach:

```
Requirements Analysis → System Design → Implementation → Testing → Documentation
        ↑                                                              |
        └──────────────── Iterative Feedback Loop ─────────────────────┘
```

### Phase 1: Requirements Analysis
- Parsed the assignment PDF to extract functional and non-functional requirements
- Structured requirements into clear categories (user management, booking flow, admin features)
- Identified constraints and assumptions
- Output: `requirements.md`

### Phase 2: System Design
- Created High-Level Design (HLD) with component diagrams
- Developed Low-Level Design (LLD) with sequence diagrams
- Designed database schema with ER diagrams (Mermaid)
- Defined API contracts and endpoint specifications
- Output: `design.md`

### Phase 3: Implementation
- Generated project scaffold (Spring Boot + Maven)
- Implemented features incrementally with focused commits
- Each commit represents a logical unit of work
- AI suggested patterns (Repository, Service Layer, DTO) and best practices

### Phase 4: Testing
- Unit tests for service layer logic
- Integration tests for API endpoints
- Concurrency and edge case testing
- Output: Test classes under `src/test/`

### Phase 5: Documentation
- README with setup instructions and API usage
- This AGENTS.md documenting the AI workflow
- Inline code documentation

---

## Prompt Log

### Prompt 1: Initial Project Setup
**User:**
> Open the downloaded Movie Ticket Booking System pdf. I have a project which I need to work and upload it to my Github as a repository with multiple commits

**AI Contribution:**
- Read and extracted full content from the PDF (2 pages)
- Identified all functional requirements, constraints, and submission criteria
- Proposed a 10-commit strategy with architecture breakdown
- Created initial task plan

---

### Prompt 2: Requirements Restructuring
**User:**
> The requirements.md file shouldn't have the design. Infact, we should first add the actual requirements with function/non-functional requirements there. Read the pdf and then update the file. Move the design aspects to another file with hld/lld/schema designs separately. Since, we are going to make this repo with mermaid diagrams as well, make sure it's compliant. We also need to create Agents.md and Claude.md file where we need to add our prompts which we are doing each time. This is an assessment so we need to follow the process

**AI Contribution:**
- Restructured into 4 separate files:
  - `requirements.md` — Pure FR/NFR requirements (13 functional groups, 6 non-functional)
  - `design.md` — HLD, LLD with Mermaid diagrams, ER schema, API overview
  - `agents.md` — AI workflow documentation with prompt log
  - `claude.md` — Project conventions and coding standards for AI context
- All Mermaid diagrams use GitHub-compatible syntax

---

### Prompt 3: Design Review & Cleanup
**User:**
> Design looks good, just that Admin Endpoints are bloated. Please correct it.

**AI Contribution:**
- Replaced verbose 32-row admin endpoint table with a compact 8-row grouped summary
- Kept all information but presented as "Standard CRUD per resource" pattern

---

### Prompt 4: Prompt Orchestration Skill
**User:**
> There should also be a skill for agents to follow. Each time we are adding new prompts, this skill should be invoked by the agents.md file — first sanitize the prompt, add it to the relevant phase, update other skills as per the prompt details, then find what needs to be updated and invoke the particular skill.

**AI Contribution:**
- Created `skills/prompt-orchestration.md` — an orchestration workflow that ensures:
  1. Every prompt is sanitized (secrets/PII removed)
  2. Logged to agents.md under the correct phase
  3. Affected skills are identified and updated
  4. Work is routed to the correct implementation skill
- Established the rule: "always log first, implement second"

---

### Prompt 5: Project Scaffold & Domain Models
**User:**
> Let's start with all the next set of tasks and follow the process I told

**AI Contribution:**
- Created full Spring Boot 3.2.5 project scaffold in the repo root
- pom.xml with all dependencies (Web, JPA, Security, Validation, H2, PostgreSQL, JWT, Lombok, Test)
- Maven wrapper (3.9.6)
- 9 enum classes: Role, SeatType, ShowSeatStatus, BookingStatus, PaymentStatus, DiscountType, NotificationType, NotificationChannel, NotificationStatus
- 14 JPA entity classes with proper annotations, relationships, and @Version for optimistic locking
- application.yml (H2 default) + application-postgres.yml
- 12 empty package placeholders for future layers
- .gitignore for Java/Maven/IDE
- Build verified: `./mvnw clean compile` → SUCCESS

---

### Prompt 6: Service Layer & Business Logic
_(to be filled as implementation proceeds)_

---

### Prompt 5: Core Entity Implementation
_(to be filled as implementation proceeds)_

---

### Prompt 6: Service Layer & Business Logic
_(to be filled as implementation proceeds)_

---

### Prompt 7: API Controllers & Security
_(to be filled as implementation proceeds)_

---

### Prompt 8: Testing
_(to be filled as implementation proceeds)_

---

## Skills Demonstrated by AI

> **Note:** Every new prompt follows the workflow defined in [`skills/prompt-orchestration.md`](./skills/prompt-orchestration.md) — sanitize → log → analyze impact → update skills → route to implementation.

- **Code Generation** — Spring Boot REST APIs, JPA entities, service classes, DTOs
- **Architecture Design** — Layered architecture, separation of concerns, design patterns
- **Database Schema Design** — Normalized relational schema, indexing strategies, constraints
- **Test Generation** — JUnit 5, Mockito, MockMvc integration tests
- **Documentation** — Markdown docs, Mermaid diagrams, API specifications
- **Security Implementation** — JWT authentication, role-based access control, input validation
- **Error Handling** — Global exception handling, meaningful error responses

## Principles Followed

1. **Design-First Approach** — Complete system design before writing implementation code
2. **Incremental Development** — Small, meaningful commits; each representing a logical unit
3. **Test Coverage** — Unit and integration tests for core business flows
4. **Clean Code** — SOLID principles, meaningful naming, minimal duplication
5. **Separation of Concerns** — Controller → Service → Repository layering
6. **Security by Default** — Authentication, authorization, input validation from the start
7. **Documentation as Code** — Docs live alongside code and evolve with it

---

*Last updated: 2026-07-26*
