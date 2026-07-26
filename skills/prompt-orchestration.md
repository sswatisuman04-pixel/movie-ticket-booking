# Prompt Orchestration Skill

## Purpose

This skill is the entry point for every new user prompt. It ensures consistent documentation, skill updates, and correct routing for implementation.

## Trigger

Invoked on **every new user prompt** after the documentation phase is complete.

## Workflow

```
New Prompt → Sanitize → Log to agents.md → Analyze Impact → Update Skills → Route to Implementation
```

### Step 1: Sanitize Prompt
- Remove any sensitive information (tokens, passwords, personal emails)
- Clean up formatting (remove excessive whitespace, fix grammar if needed)
- Extract the **intent** (what the user wants) and **context** (what files/features are involved)

### Step 2: Log to agents.md
- Determine which **phase** the prompt belongs to:
  - Phase 1: Requirements Analysis
  - Phase 2: System Design
  - Phase 3: Implementation
  - Phase 4: Testing
  - Phase 5: Documentation
- Append a new prompt entry under the correct phase in the Prompt Log section:
  ```markdown
  ### Prompt N: <Short Title>
  **User:**
  > <sanitized prompt>

  **AI Contribution:**
  - <what was done>
  - <files created/modified>
  ```
- Increment the prompt counter

### Step 3: Analyze Impact
Determine which skills are affected by this prompt:

| Prompt About | Skills to Update |
|-------------|-----------------|
| Architecture/design changes | `skills/system-design.md` |
| New API endpoints or changes | `skills/api-design.md` |
| Coding convention changes | `skills/spring-boot.md` |
| Test strategy changes | `skills/testing.md` |
| New patterns or decisions | `skills/system-design.md` |
| Schema changes | Update `design.md` ER diagram |

### Step 4: Update Affected Skills
- If the prompt introduces a new pattern → add to `skills/system-design.md`
- If the prompt changes API conventions → update `skills/api-design.md`
- If the prompt changes project structure → update `skills/spring-boot.md`
- If the prompt adds test requirements → update `skills/testing.md`
- If the prompt changes nothing in skills → skip this step

### Step 5: Route to Implementation
Based on the prompt intent, determine the action:

| Intent | Action |
|--------|--------|
| Create/modify code | Invoke `skills/spring-boot.md` for conventions, then implement |
| Add tests | Invoke `skills/testing.md` for conventions, then implement |
| Design change | Update `design.md`, then propagate to affected code |
| Add API endpoint | Invoke `skills/api-design.md` for conventions, then implement |
| Documentation only | Update relevant .md files |
| Refactor | Read existing code, apply changes following conventions |

## Example Flow

**User prompt:** "Add pagination to the browse shows endpoint"

1. **Sanitize:** Clean prompt, intent = "add pagination", context = "browse shows API"
2. **Log:** Append to agents.md under Phase 3 (Implementation)
3. **Analyze:** Affects `skills/api-design.md` (pagination pattern)
4. **Update skills:** Verify pagination conventions are documented in api-design.md
5. **Route:** Implement pagination in CustomerShowController following api-design.md conventions

## Rules

- **Always log first, implement second** — no code changes without updating agents.md
- **Never skip sanitization** — secrets, tokens, and personal data must be stripped
- **Skills are living documents** — update them whenever conventions evolve
- **One source of truth** — if a convention exists in a skill file, follow it; don't invent new patterns
- **Prompt numbering is sequential** — never reuse or skip numbers
