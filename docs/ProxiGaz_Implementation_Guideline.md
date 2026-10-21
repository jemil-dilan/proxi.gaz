# ProxiGaz — Implementation Guideline

**Read this file in full before starting work on any task, sprint item, or use case.** It applies to every task in `ProxiGaz_Sprint_Plan.md`, with no exceptions. Skipping a step here is not a shortcut — it is the fastest way to reintroduce the exact problems the specification was written to prevent (fake orders, incorrect stock, lost orders on fallback, un-auditable status changes, unreliable notifications).

This guideline has four mandatory phases, in order: **Status Analysis → Plan & Approval → Git Setup → TDD Implementation.** Do not begin writing implementation code before Phase 4, and do not begin Phase 4 before the plan in Phase 2 has been explicitly approved.

---

## Phase 1 — Analyze Current Project Status

Before proposing any plan, you must understand the actual, current state of the codebase — not the state you assume it is in from memory of a previous session.

1. Analyze the project as it currently exists: which modules exist, which use cases from the spec are actually implemented (not just scaffolded), what tests exist and pass, what is incomplete or stubbed.
2. Check whether `../status.md` already exists at the project root.
   - **If it does not exist:** create it from your analysis (template below).
   - **If it exists:** read it fully, then compare it against your own fresh analysis of the codebase. `../status.md` is a snapshot written after a previous task — it may be stale, incomplete, or simply wrong if it was not updated after the last change. Do not trust it blindly. Flag every discrepancy you find between what it claims and what the code actually shows.
3. Update `../status.md` to reflect reality, and explicitly note any discrepancies you just resolved.

### `../status.md` template

```markdown
# ProxiGaz — Project Status

Last updated: <date> by <task reference, e.g. working/PXG-014>

## Sprint progress
| Sprint                              | Status                           | Notes |
|-------------------------------------|----------------------------------|-------|
| 0 — Foundations & Core Infrastructure | Done / In progress / Not started |       |
| 1 — Vendor & Catalog Management     | ...                              |       |
| 2 — Order Flow (Vertical Slice)     | ...                              |       |
| 3 — Fallback, Delivery & Reviews    | ...                              |       |
| 4 — Hardening, Admin & Pilot Ready  | ...                              |       |

## Use case status
| ID      | Implemented | Unit tested | E2E tested | Notes |
|---------|-------------|-------------|------------|-------|
| AUTH-01 | Yes/No      | Yes/No      | Yes/No     |       |
| VEN-01  | ...         | ...         | ...        |       |
| ORD-01  | ...         | ...         | ...        |       |
| SYS-01  | ...         | ...         | ...        |       |

## Known gaps / discrepancies found this session
- <anything status.md previously claimed that did not match reality>

## Architecture notes relevant to next task
- <module boundaries, ports/adapters in place, anything the next task must respect>
```

---

## Phase 2 — Propose a Plan and Get Approval

**Do not start implementing until this phase is explicitly approved.** This is a hard stop, not a formality.

1. Based on the status analysis and the relevant sprint/use-case entry in `ProxiGaz_Sprint_Plan.md`, propose a detailed, precise implementation plan for the specific task at hand.
2. The plan must explicitly follow the architecture already in place (clean / hexagonal architecture, modular boundaries defined in the Sprint Plan). If the task seems to require deviating from this architecture, say so explicitly and explain why, rather than quietly doing something different.
3. The plan should state:
   - which use case(s) it implements
   - which files/modules it will touch
   - what new states / transitions / aggregates it introduces
   - what the acceptance tests (unit + e2e) will cover
   - what could break in adjacent modules
4. Present the plan and **wait for explicit approval before proceeding.** Do not interpret silence or an unrelated follow-up message as approval.

---

## Phase 3 — Git & Task Tracking Setup

Only begin this phase after the plan is approved.

1. Ensure you are on the main branch. If not, switch to it.
2. Update main (pull latest changes).
3. Create a new branch from main named exactly: `working/PXG-<task-number>`
4. Create a task file at the project root (e.g. `TASK_PXG-<task-number>.md`) that states clearly, as a checklist, everything the approved plan says will be implemented — one line per concrete step.
5. As each step is completed during implementation, mark it done in this file. This file is the running, visible record of progress on this specific task — keep it accurate in real time.
6. Once the task is fully complete (implementation done, tests passing — see Phase 4), **delete this task file** before considering the task finished. It is a working artifact for the duration of the task only.
7. **Commits:** every commit on the task branch must be created with Commitizen via `cz c` (alias for `cz commit`). Do not use plain `git commit -m "..."` for project commits. Interactive `cz c` is required.
   - **Choose the suitable type** for what actually changed — do not default to `fix`. Examples: documentation / guideline / README → `docs`; new capability → `feat`; bug correction → `fix`; tooling/chore → `chore` / `build` / `ci` as appropriate.
   - **Scope = task id** from the branch name `working/PXG-<task-number>` (e.g. `PXG-002`), not the full branch string.
   - **Required message shape:** `<type>(PXG-<task-number>): <short imperative summary>`  
     Example: `docs(PXG-002): document runtime and env template`  
     (Commitizen’s documentation type is `docs`, not `doc`.)
8. **README.md:** keep the root `README.md` accurate as work lands. After a completed task, update the README **only when the change is legitimate** — i.e. the project’s runnable state, documented capabilities, setup steps, or sprint progress actually changed. Do not edit the README for cosmetic rewrites, speculative future work, or filler. If nothing user-facing or setup-related changed, leave the README untouched.

### Task file template

```markdown
# Task PXG-<number>: <short title>

Plan approved: <date>
Use case(s): <AUTH-xx / VEN-xx / ORD-xx / SYS-xx>
Branch: working/PXG-<task-number>

## Steps
- [ ] Write acceptance / e2e test(s) for <scenario>
- [ ] Implement <component/module>
- [ ] Write unit tests for <method/class>
- [ ] Wire adapter/port for <external dependency, if any>
- [ ] Run full test suite
- [ ] Confirm all acceptance criteria from the spec are met
```

---

## Phase 4 — TDD Implementation Workflow

Follow this order strictly. Do not write implementation code before its corresponding acceptance/e2e test exists.

1. **Write the end-to-end / acceptance test(s) first**, directly from the use case’s acceptance criteria in the Sprint Plan and the project specification. The scenario must reflect the real environment as accurately as possible — realistic data shapes, realistic timing for time-dependent behavior (offer expiry 10–12 min, availability expiration 4 h, auto-validation after 24 h), and realistic failure modes of external dependencies (Web Push failure, future OtpProvider timeout). If the use case has a documented race condition or failure path (e.g. concurrent order creation for the same phone, simultaneous fallback offers), it must have its own scenario.
2. **Implement the code**, module by module, following the approved plan and existing architecture boundaries. Do not introduce a new module or bypass an existing port/adapter without flagging it and getting it approved first.
3. **Write unit tests alongside each method as it is written** — not in a batch afterward. Every piece of business logic (order state transitions, availability calculation `is_available AND available_until > now()`, max 2 active orders per phone, max 3 confirmation-code attempts, reliability score, fallback selection) needs a unit test proving it in isolation.
4. **Run the full test suite** once implementation and unit tests are in place. All scenarios — including failure-path and race-condition scenarios — must pass before the task is considered complete.
5. If a test fails, fix the implementation, not the test — unless the test itself was wrong, in which case say so explicitly and explain why before changing it.
6. Only after the full suite is green: mark the final steps done in the task file, then delete the task file per Phase 3, step 6.

### What “reflects the real environment” means in practice

- **Timing-sensitive scenarios** (offer expiry, availability expiry, 24 h auto-validation): **always use a controlled/fake Clock** injected via the `Clock` port. Never rely on real wall-clock time for 10-minute or 24-hour waits — tests would become impractical and flaky.
- Concurrency-sensitive scenarios (max 2 active orders per phone, concurrent fallback) must include genuinely simultaneous or near-simultaneous requests.
- Notification and OTP are behind interfaces (`NotificationChannel`, `OtpProvider`). Tests must cover both the success path and the failure / no-op path of the current (null or Web-Push-only) implementations.
- Stock availability is computed at query time (`is_available AND available_until > now() AND is_open_now`), not solely by a background job.
- Anti-abuse tests must cover rate limiting by IP and by device/fingerprint in addition to the per-phone limit.

---

## Core Domain Rules (never violate)

These rules come from the final specification (Cadrage v1.5 + MVP Spec v1.2). Any implementation that breaks them is incorrect.

1. **No paid SMS / WhatsApp recurring cost in the pilot.** All messaging goes through `NotificationChannel` (Web Push first) and `OtpProvider` (null / reduced in pilot). A trigger exists: if reported fake-order rate > 10 %, activate reverse-WhatsApp OTP later.
2. **Order statuses and transitions** must follow the defined state machine (En attente → Confirmée → En cours → Livrée → Livrée & Vérifiée / Expirée / Annulée / Échouée). Use `order_offers` for the multi-vendor fallback (max 3 attempts). Offer duration is **configurable and still an open decision** (proposed 10–12 min; may be shortened after manual test).
3. **Availability** is never a simple boolean stored forever. It is `is_available AND available_until > now() AND is_open_now`.
4. **Confirmation code**: 4 digits, maximum 3 attempts by the vendor. Successful entry → status Livrée.
5. **Client confirmation** only unlocks the review. After 24 h without client confirmation, stats may auto-validate but the review stays locked.
6. **Anti-abuse**: Max 2 active orders per phone number + rate limiting by IP and by device/fingerprint + phone block list + vendor fake-order reporting. The 10 % fake-order rate is the trigger to activate a stronger OTP later.
7. **Tracking** uses a random `tracking_token`, never the raw order ID.
8. **Vendor auth in pilot**: long-lived sessions created manually by Admin (no magic-link dependency on a messaging channel).
9. **Distance / proximity** queries must use PostGIS (`ST_DWithin`, `ST_Distance`). Ranking is strictly local.

---

## Summary checklist (pin this)

- [ ] Analyzed current project state; `../status.md` created or reconciled against reality
- [ ] Plan proposed, follows existing architecture, explicitly approved before coding started
- [ ] On main, updated, new `working/PXG-<task-number>` branch created
- [ ] Task file created and kept current throughout
- [ ] Acceptance / e2e test(s) written first, including failure and race-condition scenarios
- [ ] Implementation + unit tests written together, module by module
- [ ] Full test suite run and green
- [ ] Commits created with `cz c`, suitable type, scope `PXG-<n>` (e.g. `docs(PXG-002): …`)
- [ ] `README.md` updated only if the task changed setup, capabilities, or accurate project status
- [ ] Task file deleted after completion
- [ ] `../status.md` updated to reflect the newly completed work, ready for the next task’s Phase 1
