---
name: software-architecture
description: Software Architecture Design Methodology Guide. Covers Clean Architecture, DDD Bounded Contexts, Rich Domain Model Design, Hexagonal Architecture, Event-Driven Architecture, and Microservices Design Patterns.
---

# Software Architecture

## Quick Reference

- [Books](./references/books.md) - Essential reading for architecture mastery
- [Patterns](./references/patterns.md) - Architecture patterns with code examples
- [Anti-Patterns](./references/antipatterns.md) - Common anti-patterns and remedies
- [C4 Model](./references/c4-model.md) - Architecture documentation method
- [Online Resources](./references/online-resources.md) - Curated learning resources

## Architecture Design Principles

### SOLID Principles

| Principle | Description | Violation Symptoms |
|-----------|-------------|-------------------|
| **S**ingle Responsibility | A class should have only one reason to change | A class does too many things |
| **O**pen/Closed | Open for extension, closed for modification | Modifying existing code to add new features |
| **L**iskov Substitution | Subclasses can replace parent classes | instanceof checks, type casting |
| **I**nterface Segregation | Small, focused interfaces | Fat interfaces, forced implementation of unused methods |
| **D**ependency Inversion | Depend on abstractions, not concretions | Direct dependency on concrete classes |

### Signs of Architecture Decay

- **Circular dependencies**: Module A → B → C → A
- **Shotgun surgery**: Changing one feature requires modifying multiple classes
- **Feature envy**: A class spends more time accessing other class's data than its own
- **Duplicated code**: Repeated code scattered across the codebase
- **Premature abstraction**: Violating YAGNI, adding unnecessary indirection

## Clean Architecture

### Layer Model

```
┌─────────────────────────────────────────────────────────┐
│                    Frameworks & Drivers                  │
│         Web frameworks, ORM, UI frameworks, DB, external services │
├─────────────────────────────────────────────────────────┤
│                   Interface Adapters                     │
│      Controllers, Gateways, Presenters, Mappers         │
├─────────────────────────────────────────────────────────┤
│                    Application Layer                     │
│            Use Cases, Application Services               │
│              Commands, Queries, Handlers                  │
├─────────────────────────────────────────────────────────┤
│                      Domain Layer                        │
│    Entities, Value Objects, Aggregates, Domain Events    │
│           Domain Services, Repository Interfaces          │
│                    (No external dependencies)           │
└─────────────────────────────────────────────────────────┘
         ↑ Dependencies only point inward, outer layers depend on inner layers, inner layers know nothing about outer layers
```

### Dependency Rules

1. **Domain Layer is the Core**: No dependencies on external frameworks, libraries, or infrastructure
2. **Dependency Direction**: Outer layers can depend on inner layers, inner layers must never know about outer layers
3. **Interface Definition Location**: The consumer defines the interface (producer implements it)
4. **Data Format**: Each layer uses its own data format, outer layer formats must not be passed directly

### Project Structure (Java)

```
src/main/java/com/ai/
├── domain/                    # Domain layer (core, no external dependencies)
│   ├── model/               # Entities, Value Objects, Aggregates
│   ├── event/              # Domain Events
│   ├── service/            # Domain Services
│   └── repository/        # Repository interfaces
├── application/            # Application layer
│   ├── command/           # Command handling (CQRS)
│   ├── query/             # Query handling
│   └── service/           # Application Services
├── infrastructure/         # Infrastructure layer
│   ├── persistence/      # JPA entities, Repository implementations
│   ├── messaging/        # Event publishers
│   └── external/         # External service adapters
└── interface/            # Interface Adapters layer
    └── api/              # Controllers, DTOs
```

## DDD Domain-Driven Design

### Strategic Design

#### Bounded Context

A Bounded Context is an explicit boundary around a semantic area, each context has its own:

- **Ubiquitous Language**: Terms and meanings shared by the team
- **Domain Model**: Concepts that belong exclusively to this context
- **Boundary**: Clear definition of what's inside and what's outside

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Order Context  │    │  Inventory Context │    │  Payment Context │
│                  │    │                  │    │                  │
│  - Order        │◄──►│  - Inventory    │◄──►│  - Payment      │
│  - OrderItem    │    │  - Stock        │    │  - Transaction  │
│  - Pricing      │    │  - Warehouse    │    │  - Gateway     │
│                  │    │                  │    │                  │
│  Team: Order    │    │ Team: Warehouse │    │  Team: Payment  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

#### Core Domain Classification

| Type | Description | Investment |
|------|-------------|------------|
| **Core Domain** | Core competency, unique value proposition | Maximum investment, carefully crafted |
| **Supporting Domain** | Supports the core domain | Moderate investment |
| **Generic Domain** | Generic solutions, can be purchased | Minimal investment |

#### Context Mapping Patterns

- **Shared Kernel**: Subset of shared domain model
- **Customer/Supplier**: Upstream/downstream relationship
- **Conformist**: Downstream follows upstream model
- **Anticorruption Layer**: Translation layer isolating different models

### Tactical Design

See [Patterns](./references/patterns.md) for detailed code examples:

| Pattern | Description |
|---------|-------------|
| **Entity** | Objects with unique identity whose lifecycle can continue |
| **Value Object** | Immutable, equality based on attribute values |
| **Aggregate** | Consistency boundary, accessed externally through the root |
| **Repository** | Collection abstraction for aggregates |
| **Domain Service** | Cross-entity business logic |
| **Domain Event** | Decoupling via important domain events |

## Hexagonal Architecture (Ports and Adapters)

```
                    ┌─────────────────────┐
                    │    Primary Adapters  │
                    │  (Driving Actors)     │
                    │  ┌─────────────────┐  │
                    │  │   Controllers   │  │
                    │  │   REST, GraphQL │  │
                    │  │   CLI, Events   │  │
                    │  └────────┬────────┘  │
                    └───────────┼───────────┘
                                │
                                ▼
                    ┌─────────────────────┐
                    │      PORTS           │
                    │  (Inbound Interfaces)│
                    │  ┌─────────────────┐  │
                    │  │  Use Cases /    │  │
                    │  │  Commands       │  │
                    │  └────────┬────────┘  │
                    │           │            │
                    │           ▼            │
                    │  ┌─────────────────┐  │
                    │  │     DOMAIN       │  │
                    │  │   (Core Logic)   │  │
                    │  │  Entities       │  │
                    │  │  Value Objects  │  │
                    │  │  Domain Services │  │
                    │  └────────┬────────┘  │
                    │           │            │
                    │      PORTS           │
                    │  (Outbound Interfaces)│
                    │  ┌────────┴────────┐  │
                    │  │    Secondary    │  │
                    │  │    Adapters     │  │
                    │  │  Repositories   │  │
                    │  │  External APIs  │  │
                    │  │  Message Queues │  │
                    └──┴─────────────────┴──┘
```

## Event-Driven Architecture

See [Patterns](./references/patterns.md) for detailed examples:

| Pattern | Description |
|---------|-------------|
| **Event Sourcing** | Store events instead of state, reconstruct from event replay |
| **CQRS** | Separate Command (write) and Query (read) models |
| **Saga** | Distributed transaction management via compensating transactions |

## Microservices Design Patterns

### Pattern Comparison

| Pattern | Characteristics | Applicable Scenarios |
|---------|----------------|----------------------|
| **Aggregates** | Monolithic architecture, bounded contexts | Small teams, moderate complexity |
| **Event-Driven** | Async collaboration via events | Independent deployment, high concurrency |
| **Saga** | Distributed transactions via compensating actions | Cross-service consistency |
| **CQRS** | Read/write model separation | Large read/write ratio differences |

### Communication Patterns

```
Sync communication:                        Async communication:
┌─────┐    REST/gRPC    ┌─────┐   ┌─────┐    Event    ┌─────┐
│  A  │ ───────────────► │  B  │   │  A  │ ─────────► │  B  │
└─────┘                  └─────┘   └─────┘            └─────┘
      Response                  Publish/Subscribe     Consume/Process
```

## Architecture Decision Records (ADR)

Document significant architecture decisions:

```markdown
# ADR-001: Designing Order Aggregate Using Rich Domain Model

## Status
Accepted

## Context
Order business logic is scattered across OrderService and various places.

## Decision
Adopt Rich Domain Model, encapsulate order state changes and business rules inside the Order entity.

## Consequences
- Order state machine fully encapsulated
- Business rules cohesive within domain objects
- Easy to unit test

## Drawbacks
- Team needs to learn DDD Rich Domain Model
- Aggregate design requires careful review
```

## Architecture Review Checklist

### Code-Level Review

- [ ] No circular dependencies (module/package level)
- [ ] Domain layer has no infrastructure dependencies
- [ ] Entities contain business behavior (not just fields)
- [ ] Value objects are immutable
- [ ] Aggregate boundaries are clear
- [ ] Repositories only operate on aggregate roots

### Design-Level Review

- [ ] Bounded Context division is reasonable
- [ ] Context mapping relationships are clear
- [ ] Core Domain receives sufficient investment
- [ ] Architecture layers follow dependency rules

### Change Impact Analysis

- [ ] What modules/layers need modification for a given change
- [ ] Where should new functionality be placed in bounded contexts
- [ ] Need to create new aggregates or services
