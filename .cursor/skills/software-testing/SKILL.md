---
name: software-testing
description: Software Testing Guide. Covers TDD, BDD, Testing Pyramid, and best practices.
---

# Software Testing

## Testing Pyramid

```
        ▲
       /E\      E2E Tests (10%) - Critical paths only
      /2E \
     /-----\
    / Inte \   Integration Tests (20%) - Component collaboration
   /gration\
  /-----------\
 /   Unit     \  Unit Tests (70%) - Single class, fast, reliable
/   Tests      \
────────────────
```

## TDD Cycle

```
RED → GREEN → REFACTOR

RED:    Write failing test
GREEN:  Minimal code to pass
REFACTOR: Clean while keeping tests green
```

### Naming Convention

```
should_expected_behavior_when_trigger_condition

Java:    shouldBeEligibleForFreeShippingWhenOrderExceeds100
```

## Test Structure (AAA)

```java
@Test
void shouldCalculateTotalCorrectly() {
    // Given: Prepare test data
    List<OrderLine> lines = List.of(
        new OrderLine(Money.of(100), 2),
        new OrderLine(Money.of(50), 3)
    );

    // When: Execute behavior
    Money total = new Order(lines).totalAmount();

    // Then: Verify outcome
    assertThat(total).isEqualTo(Money.of(350));
}
```

## Test Doubles

| Type | Purpose | Example |
|------|---------|---------|
| Dummy | Fill unused params | `new Order(null, dummy)` |
| Fake | Simplified impl | `InMemoryRepository` |
| Stub | Predefined responses | `when(repo.findById(1)).thenReturn(user)` |
| Mock | Verify interactions | `verify(repo).save(order)` |

## BDD (Gherkin)

```gherkin
Feature: Free Shipping

  Scenario: Order over $100 gets free shipping
    Given my cart has items totaling $120
    When I checkout
    Then shipping should be $0

  Scenario: Order under $100 has shipping fee
    Given my cart has items totaling $50
    When I checkout
    Then shipping should be $10
```

## Coverage Goals

| Layer | Target |
|-------|--------|
| Domain | > 90% |
| Application | > 80% |
| Interface | 100% happy path |

## Anti-Patterns

| Anti-Pattern | Solution |
|--------------|----------|
| Test implementation | Test behavior |
| Weak assertions | Precise assertions |
| Over-mocking | Use fakes or real objects |
| Testing private methods | Test public behavior |
| Slow unit tests | Mock dependencies |

## Quick Checklist

- [ ] Test names express intent clearly
- [ ] Tests are independent (no shared state)
- [ ] Boundary conditions covered (null, zero, max)
- [ ] Both happy and sad paths tested
- [ ] Test data builders used for complex objects
