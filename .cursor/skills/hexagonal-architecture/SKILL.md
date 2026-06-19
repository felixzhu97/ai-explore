---
name: hexagonal-architecture
description: Hexagonal Architecture refactoring guide. Refactor existing code to Ports-and-Adapters architecture while preserving DDD rich domain model.
version: "1.0"
lastUpdated: "2026-06-20"
---

> **Prerequisite**: This skill is based on the rules defined in `.cursor/rules/hexagonal-architecture.mdc`

---

# Hexagonal Architecture Refactoring Guide

## Refactoring Flow

```
1. Identify domain concepts → Extract Domain layer (core, no dependencies)
2. Identify Driving adapters → Create adapter/in (primary port)
3. Identify Driven adapters → Create adapter/out (secondary port)
4. Integrate configuration → Create Config layer
```

---

## Refactoring Phases

### Phase 1: Extract Domain Layer

**Goal**: Create core business model with no external dependencies

```java
// ✅ Rich domain model
public class Order extends AggregateRoot {
    private final UUID id;
    private OrderStatus status;
    private final List<OrderLine> lines;

    private Order(UUID id) { /* ... */ }

    public static Order create() {
        return new Order(UUID.randomUUID());
    }

    public void place() {
        validateCanPlace();
        this.status = OrderStatus.PLACED;
    }
}
```

### Phase 2: Create adapter/in (Driving)

**Goal**: Primary adapter receives external input

```java
// Controller
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final PlaceOrderUseCase useCase;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody Request req) {
        return ResponseEntity.ok(useCase.execute(req.toCommand()));
    }
}
```

### Phase 3: Create adapter/out (Driven)

**Goal**: Secondary adapter connects to external systems

```java
// Repository implementation
@Repository
public class JpaOrderRepository implements OrderRepository {
    @Override
    public Optional<Order> findById(UUID id) { /* ... */ }
}
```

### Phase 4: Config Layer Integration

**Goal**: Spring configuration and dependency injection

```java
@SpringBootApplication
@ComponentScan(basePackages = "com.ai")
public class AiApplication { }
```

---

## Frequently Asked Questions

### Q: How to distinguish adapter/in and adapter/out?

- **adapter/in**: Input points that drive the system (Controllers, REST APIs)
- **adapter/out**: External dependencies that the system calls (Repositories, External Services)

### Q: What can the Domain layer depend on?

- ✅ Pure Java classes
- ✅ Value Objects
- ✅ Port interfaces (Port Interfaces)
- ❌ Spring/Jakarta annotations
- ❌ Database drivers

### Q: Where to place UseCases?

- Small projects: Place in `adapter/in/controller` as private classes
- Medium projects: Place in `domain/service/` as domain services
- Large projects: Create separate `application/usecase/` package

---

## Refactoring Checklist

### Before Starting

- [ ] Backup existing code
- [ ] Ensure test coverage exists
- [ ] Define refactoring scope

### During Process

- [ ] Refactor one small step at a time
- [ ] Run tests after each refactoring
- [ ] Verify architecture boundaries

### After Completion

- [ ] All tests pass
- [ ] Architecture checks pass
- [ ] Documentation updated
