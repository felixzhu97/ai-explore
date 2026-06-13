# Anti-Patterns

Common architectural anti-patterns and their remedies.

## Code-Level Anti-Patterns

| Anti-Pattern | Symptoms | Consequences | Remedy |
|--------------|----------|--------------|--------|
| **Anemic Domain Model** | Entities only have getters/setters, no behavior | Business logic scattered in services, hard to test | Move business logic into domain objects |
| **God Class/Object** | Single class with too many responsibilities | Hard to understand, test, or reuse | Split into smaller, focused classes |
| **Shotgun Surgery** | One change requires modifying many classes | High coupling, fragile code | Use Extract Class, move behavior closer to data |
| **Feature Envy** | Class accesses other class's data more than its own | Tight coupling, poor cohesion | Move method to the class it envies |
| **Data Clump** | Same group of data fields appear together repeatedly | Duplication, inconsistency | Extract into a class/record |
| **Primitive Obsession** | Using primitives instead of domain types | Loss of type safety, validation scattered | Create value objects for domain concepts |
| **Long Method** | Methods that are too long | Hard to read, test, reuse | Extract smaller methods |
| **Switch Statements** | Repeated switch/case or if-else chains | Adding cases requires changing many places | Use polymorphism or strategy pattern |

## Architecture-Level Anti-Patterns

| Anti-Pattern | Symptoms | Consequences | Remedy |
|--------------|----------|--------------|--------|
| **Circular Dependency** | A → B → C → A | Changes cascade unpredictably, hard to test | Introduce abstractions, extract common interface |
| **Premature Abstraction** | Unnecessary interfaces, indirection | Over-engineering, harder to understand | Follow YAGNI, add abstraction when needed |
| **Golden Hammer** | Applying one solution to all problems | Wrong tool for the job | Understand trade-offs of different approaches |
| **Architecture Astronaut** | Overly complex architecture for simple needs | Excessive complexity, slow development | Start simple, evolve as needed |
| **Lava Flow** | Old, unmaintainable code still in production | Risk when changing, hard to remove | Incrementally refactor, document unknowns |
| **Big Ball of Mud** | No clear architecture, everything mixed | Chaos, high maintenance cost | Define bounded contexts, isolate changes |

## Layering Anti-Patterns

| Anti-Pattern | Symptoms | Consequences | Remedy |
|--------------|----------|--------------|--------|
| **Anemic Domain Model** | Domain layer has no logic, all in services | Violates Clean Architecture | Move behavior into domain entities |
| **Smart UI** | Business logic in controllers/presenters | Hard to test, tied to UI framework | Move logic to domain layer |
| **Gateway Agglomeration** | One gateway wrapping multiple services | Tight coupling, single point of change | Split into focused adapters |
| **DTO Overdose** | Excessive DTOs, no domain objects | All logic in transformation | Use domain objects internally |
| **Leaking Abstractions** | Domain depends on infrastructure | Violates dependency rules | Define interfaces in domain |

## Database Anti-Patterns

| Anti-Pattern | Symptoms | Consequences | Remedy |
|--------------|----------|--------------|--------|
| **Single Table Inheritance** | One table for entire class hierarchy | Null columns, complex queries | Separate tables per class |
| **Surrounding the Transaction** | Business logic tied to transaction scope | Hard to test, inflexible | Separate transaction boundaries |
| **Query in Loop** | N+1 queries | Performance degradation | Use JOIN or batch queries |
| **Database as Queue** | Using tables for message queuing | Lost updates, race conditions | Use proper message broker |

## Microservices Anti-Patterns

| Anti-Pattern | Symptoms | Consequences | Remedy |
|--------------|----------|--------------|--------|
| **Microservice Premium** | Breaking monolith into too many services | Complexity explosion | Start with bounded contexts |
| **Shared Database** | Multiple services sharing same DB | Tight coupling, deployment issues | Extract to separate databases |
| **Chatty Services** | Excessive service-to-service calls | Latency, cascading failures | Batch operations, use events |
| **Nanoservices** | Over-granular service decomposition | Too many moving parts | Consolidate related functionality |
| **Ignore Failure** | No circuit breaker, retry logic | Cascading failures | Implement resilience patterns |

## Testing Anti-Patterns

| Anti-Pattern | Symptoms | Consequences | Remedy |
|--------------|----------|--------------|--------|
| **Test Envy** | Tests have more code than production | Tests become burden | Keep tests focused, use test utilities |
| **Mock Overload** | Excessive mocking of collaborators | Tests don't reflect reality | Use real objects when possible |
| **Assertion Roulette** | Multiple unrelated assertions | Hard to diagnose failures | One assertion per test |
| **Gentleman Test** | Tests not run or commented out | Degraded confidence | Delete or fix tests |
| **Secret Catcher** | Tests that don't verify anything | False confidence | Always assert expected behavior |

## Refactoring Anti-Patterns

| Anti-Pattern | Symptoms | Consequences | Remedy |
|--------------|----------|--------------|--------|
| **Parallel Replacement** | Keeping old and new code during refactor | Confusion, technical debt | Delete old code after validation |
| **Waterbed Effect** | Fixing one problem creates another | Complexity shifts | Use test coverage, review impact |
| **Cutting the Gordian Knot** | Big bang refactor instead of incremental | High risk, hard to rollback | Incremental changes with small PRs |
