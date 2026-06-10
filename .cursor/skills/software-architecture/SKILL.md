---
name: software-architecture
description: Software Architecture Design Guide. Covers Clean Architecture, DDD, and key design patterns.
---

# Software Architecture

## SOLID Principles

| Principle | Description |
|-----------|-------------|
| **S**ingle Responsibility | One reason to change |
| **O**pen/Closed | Open for extension, closed for modification |
| **L**iskov Substitution | Subclasses replace parent classes |
| **I**nterface Segregation | Small, focused interfaces |
| **D**ependency Inversion | Depend on abstractions |

## Clean Architecture

```
┌─────────────────────────────────────┐
│           Interface/UI              │  ← Controllers, Web
├─────────────────────────────────────┤
│           Application               │  ← Use Cases, Commands
├─────────────────────────────────────┤
│              Domain                 │  ← Entities, VO, Services
├─────────────────────────────────────┤
│           Infrastructure           │  ← DB, External Services
└─────────────────────────────────────┘
    Dependencies point inward only
```

**Key Rules:**
1. Domain layer: NO framework dependencies
2. Inner layers know nothing about outer layers
3. Consumer defines interfaces
4. Transform data when crossing layers

## Project Structure (Java)

```
src/main/java/com/ai/
├── domain/
│   ├── model/entity/         # Entities with behavior
│   ├── model/vo/            # Value Objects
│   └── repository/          # Interfaces only
├── application/
│   ├── service/             # Use cases
│   └── command/             # Command handlers
├── infrastructure/
│   └── persistence/         # JPA implementations
└── interface/
    └── api/                # Controllers, DTOs
```

## DDD Building Blocks

| Component | Rules |
|-----------|-------|
| **Entity** | Has identity, lifecycle, rich behavior |
| **Value Object** | Immutable, equality by value |
| **Aggregate** | Consistency boundary, root access only |
| **Repository** | Collection abstraction, root only |
| **Domain Service** | Cross-entity logic only |
| **Domain Event** | Important state changes |

## Rich vs Anemic Domain Model

```java
// ❌ Anemic - Wrong
public class Order {
    private OrderStatus status;
    public void setStatus(OrderStatus s) { this.status = s; }
}

// ✅ Rich - Correct
public class Order {
    private OrderStatus status;
    public void place() {
        if (this.status != DRAFT) throw new InvalidStateException();
        this.status = PLACED;
    }
}
```

## Clean Architecture Checklist

- [ ] Domain has NO Spring/Jakarta/Hibernate imports
- [ ] Repository interfaces in Domain, implementations in Infrastructure
- [ ] Data transforms when crossing layers (DTO → Domain)
- [ ] Business logic in Domain/Application, not Controllers
- [ ] Value objects are immutable
