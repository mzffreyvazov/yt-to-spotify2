---
name: design-smell-detection
description: Identify and refactor architectural and domain-model design smells in Java and Spring applications, especially volatile derived state, primitive obsession, anemic domain models, feature envy, and temporal coupling. Use when reviewing code, refactoring legacy modules, designing entities/value objects, or when service classes feel too large and domain objects too passive.
---

# Design Smell Detection

Use this skill to review Java/Spring code through a senior domain-design lens. Favor immutability, single source of truth, and objects that protect their own invariants.

## Core Philosophy

- Prefer immutability by default. If a field does not need to change, remove the setter and make invalid transitions impossible.
- Prefer a single source of truth. Do not persist values that can be derived cheaply and deterministically from other state.
- Protect domain integrity inside the owning type. Keep validation and business rules near the data they govern.
- Prefer "tell, don't ask." Move calculations and state transitions onto the object that owns the data.
- Prefer explicit construction over temporal protocols. Ensure required setup happens in constructors, factories, builders, or execute-around APIs.

## Review Workflow

1. Identify the domain boundary. Separate entities, value objects, aggregates, DTOs, repositories, and orchestration services before judging the design.
2. Scan for stale derived state. Look for persisted or cached values that depend on time, collections, or other fields.
3. Scan for leaked invariants. Look for regex checks, null checks, or range checks living in services, controllers, or mappers.
4. Scan for behaviorless models. Look for classes with mostly fields, getters, and setters while services perform all calculations and transitions.
5. Scan for foreign-data logic. Look for methods that walk another object's internals more than they use their own fields.
6. Scan for call-order traps. Look for APIs that require `init()` before `parse()`, `load()` before `calculate()`, or `open()` before `use()` without enforcement.
7. Recommend the smallest refactor that strengthens correctness without unnecessary churn.

## Smell Catalog

| Smell | Detection trigger | Risk | Better path |
| --- | --- | --- | --- |
| Volatile derivative | Fields like `age`, `totalPrice`, `itemCount`, `isOverdue`, `status` derived from dates or child collections | Drift and desynchronization | Calculate from source-of-truth fields |
| Primitive obsession | `String`/`Integer` represent domain concepts such as email, phone, ZIP code, money | Validation leaks across layers | Introduce value objects with self-validation |
| Anemic domain model | Getters/setters dominate domain classes; services perform business rules | God services and weak invariants | Move behavior into domain types and expose business methods |
| Feature envy | A method uses another object's getters more than its own state | Wrong ownership and brittle coupling | Move calculation/decision to the data owner |
| Temporal coupling | Methods must be called in order but API does not enforce it | Invalid runtime states and hidden NPEs | Use constructors, builders, factories, or execute-around APIs |

## Refactoring Moves

### Volatile Derivative

Flag any stored field whose value can be recomputed from another field, the current date, or a child collection.

```java
// Smelly: age drifts every year
record User(LocalDate birthDate, int age) {}

// Better: derive on demand
record User(LocalDate birthDate) {
    int age() {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}
```

Ask:
- Can this field be recomputed quickly?
- What update path would keep this value synchronized?
- Would deleting the stored field remove an entire class of bugs?

Prefer derived methods, projections, or query-time calculations over duplicated persisted state.

### Primitive Obsession

Flag primitive fields when the domain concept has rules, formatting, or behavior.

```java
public record Email(String value) {
    public Email {
        if (value == null || !value.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
}
```

Prefer value objects for:
- validation-heavy strings
- identifiers with formatting rules
- money, percentages, and ranges
- dates or windows with domain semantics

When reviewing, ask whether the type communicates meaning and protects itself from invalid input.

### Anemic Domain Model

Flag classes that only expose state while services implement the real rules.

```java
// Prefer behavior-rich methods over raw status mutation
order.cancel();
subscription.renewUntil(nextBillingDate);
invoice.markPaid(paymentReference);
```

Move logic into the domain object when the rule depends mostly on that object's own fields. Remove broad setters in favor of meaningful business operations.

### Feature Envy

Flag methods that repeatedly traverse another object's data to calculate a result.

```java
// Smelly
BigDecimal total = order.getItems().stream()
    .map(item -> item.getPrice().multiply(item.getQuantity()))
    .reduce(BigDecimal.ZERO, BigDecimal::add);

// Better
BigDecimal total = order.calculateTotal();
```

Prefer moving calculations, state transitions, and invariant checks to the type that owns the data being inspected.

### Temporal Coupling

Flag APIs that can be misused by calling methods in the wrong order.

```java
// Smelly
parser.init();
parser.parse(input);
parser.cleanup();
```

Prefer:
- constructors or static factories that return fully valid objects
- builders when many fields are required before use
- execute-around APIs that own setup and cleanup internally

## Quick Heuristics

- If changing one field requires remembering to update another, suspect a volatile derivative.
- If validation appears in more than one class, suspect primitive obsession or misplaced invariants.
- If a service exceeds a few hundred lines and mostly manipulates one aggregate, suspect an anemic model.
- If a method reaches through two or more getter chains, suspect feature envy.
- If documentation says "call these methods in order," suspect temporal coupling.

## Recommended Patterns

- Use value objects for domain concepts such as `Email`, `Money`, and `DateRange`.
- Use strategy to replace large `if`/`switch` blocks that vary behavior by type or status.
- Use builders or factories to make invalid construction impossible.
- Use dependency injection instead of `new HeavyResource()` inside domain or service constructors.

## Review Output Shape

When reporting findings:

1. Prioritize correctness and design-risk issues over style.
2. Explain the concrete failure mode, not only the smell name.
3. Propose the smallest credible refactor.
4. Distinguish domain objects from DTOs or persistence-only shapes before recommending behavior moves.
5. Avoid forcing rich behavior into objects that are intentionally simple transport models.

Use concise findings in this form:

```text
Smell: Volatile derivative
Why it matters: totalPrice can drift from line items during partial updates.
Better path: remove the field, compute from items, or centralize recalculation behind one business method.
```

## Scope Boundaries

- Prefer `jpa-patterns` for fetch strategies, lazy loading, and query tuning.
- Prefer `architecture-review` for package boundaries, module direction, and layering.
- Prefer `solid-principles` or `clean-code` for broader OO refactoring beyond these domain smells.
