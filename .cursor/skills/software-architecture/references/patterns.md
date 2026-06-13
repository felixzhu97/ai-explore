# Architecture Patterns

Detailed code examples for key architectural patterns.

## Entity Pattern

Objects with unique identity whose lifecycle can continue.

```java
// Rich Domain Model: Behavior inside the entity
public class Order extends AggregateRoot {
    private OrderId id;           // Unique identifier
    private CustomerId customerId;
    private List<OrderLine> lines;
    private OrderStatus status;
    private Money totalAmount;

    // Factory method to create order
    public static Order create(CustomerId customerId, List<OrderLine> lines) {
        Order order = new Order();
        order.id = OrderId.generate();
        order.customerId = customerId;
        order.lines = new ArrayList<>(lines);
        order.status = OrderStatus.DRAFT;
        order.totalAmount = calculateTotal(lines);
        return order;
    }

    // Business behavior: Place order
    public void place() {
        if (status != OrderStatus.DRAFT) {
            throw new OrderInvalidStateException("Only draft order can be placed");
        }
        if (lines.isEmpty()) {
            throw new OrderEmptyException("Order must have at least one line");
        }
        status = OrderStatus.PLACED;
        addDomainEvent(new OrderPlacedEvent(this));
    }

    // Business behavior: Cancel order
    public void cancel(String reason) {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            throw new OrderCannotBeCancelledException();
        }
        status = OrderStatus.CANCELLED;
        addDomainEvent(new OrderCancelledEvent(this, reason));
    }

    // Protected constructor (enforce factory method usage)
    protected Order() {}
}
```

## Value Object Pattern

No unique identity, immutable, equality based on attribute values.

```java
// Immutable value object
public record Money {
    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        this.amount = amount.stripTrailingZeros();
        this.currency = Objects.requireNonNull(currency, "Currency is required");
    }

    // Value object operations return new instances
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new CurrencyMismatchException(this.currency, other.currency);
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int factor) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), this.currency);
    }

    // equals/hashCode based on value
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.equals(money.amount) && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    // No setters, all fields final
    public BigDecimal amount() { return amount; }
    public Currency currency() { return currency; }
}
```

## Aggregate Pattern

Consistency boundary, accessed externally through the root entity.

```java
// Aggregate root: Order is the access point for OrderLine
public class Order extends AggregateRoot {
    private OrderId id;
    private List<OrderLine> lines;  // Internally managed, not directly exposed

    // External access only through aggregate root
    public void addLine(Product product, int quantity) {
        validateLine(product, quantity);
        OrderLine line = new OrderLine(product.getId(), product.getPrice(), quantity);
        lines.add(line);
        recalculateTotal();
    }

    // No direct access to internal lines
    public List<OrderLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    // Modifications only through aggregate root
    public void removeLine(OrderLineId lineId) {
        lines.removeIf(line -> line.getId().equals(lineId));
        recalculateTotal();
    }
}

// Incorrect: Exposing internal implementation
// public class BadOrder {
//     public List<OrderLine> lines;  // Directly exposed, can be modified externally
// }
```

## Repository Pattern

Collection abstraction for aggregates, CRUD operations only on aggregate roots.

```java
// Domain layer: Define repository interface (no implementation dependencies)
public interface OrderRepository {
    Optional<Order> findById(OrderId id);
    Optional<Order> findByIdWithLines(OrderId id);  // Aggregate loading strategy
    Page<Order> findByCustomer(CustomerId customerId, Pageable pageable);
    void save(Order order);
    void delete(Order order);
}

// Infrastructure layer: Implement repository
@Repository
public class JpaOrderRepository implements OrderRepository {
    private final SpringDataOrderRepository delegate;

    @Override
    public Optional<Order> findByIdWithLines(OrderId id) {
        return delegate.findWithLinesById(id.value())
            .map(jpaMapper::toDomain);
    }

    @Override
    @Transactional
    public void save(Order order) {
        JpaOrder entity = jpaMapper.toEntity(order);
        delegate.save(entity);
    }
}
```

## Domain Service Pattern

Business logic that cannot be attributed to a single entity.

```java
// Cross-entity business rules
public class PricingService {

    // Calculate order total, considering discount rules
    public Money calculateOrderPrice(List<OrderLine> lines, Customer customer, Promotion promotion) {
        Money subtotal = lines.stream()
            .map(line -> line.getPrice().multiply(line.getQuantity()))
            .reduce(new Money(BigDecimal.ZERO, Currency.USD), Money::add);

        Money discount = calculateDiscount(subtotal, customer, promotion);
        return subtotal.add(discount.negate());
    }

    private Money calculateDiscount(Money subtotal, Customer customer, Promotion promotion) {
        Money discount = Money.ZERO;

        // VIP customer discount
        if (customer.isVip()) {
            discount = discount.add(subtotal.multiply(0.1));
        }

        // Promotion discount
        if (promotion != null && promotion.isActive()) {
            discount = discount.add(promotion.applyTo(subtotal));
        }

        // Cannot exceed order amount
        return discount.isGreaterThan(subtotal) ? subtotal : discount;
    }
}
```

## Domain Event Pattern

Important events that occurred within the domain, used for decoupling.

```java
// Domain event definition
public record OrderPlacedEvent(
    OrderId orderId,
    CustomerId customerId,
    Money totalAmount,
    Instant occurredAt
) {}

// Aggregate root publishes events
public class Order extends AggregateRoot {
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public void place() {
        addDomainEvent(new OrderPlacedEvent(
            this.id,
            this.customerId,
            this.totalAmount,
            Instant.now()
        ));
    }

    @Override
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }
}

// Event handler
@EventHandler
public class OrderEventHandler {
    public void handle(OrderPlacedEvent event) {
        // Send emails, notify inventory, update reports, etc.
    }
}
```

## Hexagonal Architecture (Ports and Adapters)

```
                    ┌─────────────────────┐
                    │    Primary Adapters  │
                    │  (Driving Actors)     │
                    │                       │
                    │  ┌─────────────────┐  │
                    │  │   Controllers   │  │
                    │  │   REST, GraphQL │  │
                    │  │   CLI, Events   │  │
                    │  └────────┬────────┘  │
                    └───────────┼───────────┘
                                │
                                ▼
                    ┌─────────────────────┐
                    │                     │
                    │      PORTS           │
                    │  (Inbound Interfaces)│
                    │                     │
                    │  ┌─────────────────┐  │
                    │  │  Use Cases /    │  │
                    │  │  Commands       │  │
                    │  │                 │  │
                    │  └────────┬────────┘  │
                    │           │            │
                    │           ▼            │
                    │  ┌─────────────────┐  │
                    │  │     DOMAIN       │  │
                    │  │   (Core Logic)   │  │
                    │  │                  │  │
                    │  │  Entities       │  │
                    │  │  Value Objects  │  │
                    │  │  Domain Services │  │
                    │  └────────┬────────┘  │
                    │           │            │
                    │      PORTS           │
                    │  (Outbound Interfaces)│
                    │           │            │
                    │  ┌────────┴────────┐  │
                    │  │    Secondary    │  │
                    │  │    Adapters     │  │
                    │  │                 │  │
                    │  │  Repositories   │  │
                    │  │  External APIs  │  │
                    │  │  Message Queues │  │
                    └──┴─────────────────┴──┘
```

## Event Sourcing Pattern

Store events instead of state.

```java
// Event store instead of state store
public class BankAccount {
    private AccountId id;
    private List<DomainEvent> events = new ArrayList();

    // Reconstruct state from event replay
    public void replay(Iterable<DomainEvent> events) {
        events.forEach(this::mutate);
    }

    private void mutate(DomainEvent event) {
        switch (event) {
            case DepositedEvent e -> apply(e);
            case WithdrawnEvent e -> apply(e);
        }
    }

    private void apply(DepositedEvent e) {
        this.balance = this.balance.add(e.amount());
    }

    // Commands produce events
    public void deposit(Money amount) {
        if (amount.isNegative()) throw new InvalidAmountException();
        events.add(new DepositedEvent(id, amount, Instant.now()));
        mutate(events.get(events.size() - 1));
    }
}
```

## CQRS Pattern (Command Query Responsibility Segregation)

Separate read/write models.

```
┌─────────────────┐         ┌─────────────────┐
│   Commands      │         │     Queries     │
│  (Write Model)  │         │  (Read Model)   │
│                 │         │                 │
│  CreateOrder    │────────►│  OrderSummary   │
│  UpdateOrder    │  sync   │  OrderDetails   │
│  CancelOrder    │  async  │  OrderHistory   │
│                 │         │                 │
└────────┬────────┘         └────────▲────────┘
         │                            │
         │        ┌──────────────────┘
         │        │
         ▼        ▼
    ┌─────────────────────────────────┐
    │       Event Store / Bus         │
    │   (Kafka, EventStore, etc.)     │
    └─────────────────────────────────┘
```

## Saga Pattern

Distributed transaction management for microservices.

```java
// Orchestration-based Saga
public class OrderSagaOrchestrator {
    private final List<SagaStep> steps = List.of(
        new ReserveInventoryStep(),
        new ProcessPaymentStep(),
        new ConfirmShipmentStep()
    );

    public void execute(OrderCreatedEvent event) {
        SagaContext context = new SagaContext(event.orderId());

        for (SagaStep step : steps) {
            try {
                step.execute(context);
            } catch (Exception e) {
                // Compensating transactions
                for (int i = steps.indexOf(step) - 1; i >= 0; i--) {
                    steps.get(i).compensate(context);
                }
                break;
            }
        }
    }
}

// Compensating transaction example
public class ReserveInventoryStep {
    public void execute(SagaContext context) {
        InventoryReservation reservation = inventoryService.reserve(
            context.orderId(),
            context.items()
        );
        context.setInventoryReservation(reservation);
    }

    public void compensate(SagaContext context) {
        inventoryService.release(context.getInventoryReservation());
    }
}
```

## Anemic vs Rich Domain Model Comparison

| Characteristic          | Anemic Domain Model (Anti-Pattern) | Rich Domain Model (Recommended)           |
| ----------------------- | ---------------------------------- | ----------------------------------------- |
| Entity content          | Only fields + getter/setter        | Fields + business behavior                |
| Business logic location | Service layer                      | Inside domain objects                     |
| State changes           | Service directly modifies fields   | Domain object methods encapsulate changes |
| Validation logic        | Service or utility classes         | Domain object self-validation             |
| Testability             | Test Service                       | Test domain objects                       |

```java
// Anemic Domain Model (Incorrect)
public class AnemicOrder {
    private UUID id;
    private List<OrderLine> lines;
    private OrderStatus status;

    // Only getter/setter
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
}

// Rich Domain Model (Correct)
public class Order {
    public void place() {
        if (this.lines.isEmpty()) {
            throw new OrderEmptyException();
        }
        this.status = OrderStatus.PLACED;
    }
}
```
