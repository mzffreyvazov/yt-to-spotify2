---
name: design-patterns
description: Modern Java design patterns with Spring and Java 17+ idioms (Factory with sealed classes, Strategy as lambdas, Execute Around, AOP Decorator, Reactive Observer, Records with Withers). Use when user asks "implement pattern", "use factory", "strategy pattern", "refactor this", or when designing extensible components. Also triggers for code reviews where patterns are suggested.
---

# Design Patterns Skill

Modern design patterns reference for Java 17+ and Spring. Prioritizes **composition over inheritance** — functional and AOP-based approaches are preferred over classic inheritance-heavy versions where applicable.

> **Core principle:** Favor composition-based patterns (Strategy, Execute Around, AOP Decorator) over inheritance-based ones (Template Method, abstract Decorator hierarchies). When a strategy has a single method, treat it as a functional interface.

## When to Use
- User asks to implement a specific pattern
- Designing extensible/flexible components
- Refactoring rigid or inheritance-heavy code structures
- Code review suggests pattern usage
- Reviewing or writing Spring services/components

---

## Quick Reference: Modern Pattern Selection Matrix

| Situation | Classic Pattern | Modern Java/Spring Tool |
|-----------|----------------|------------------------|
| Handle object creation by type | Factory | `sealed` interface + `switch` expression |
| Vary logic at runtime (single method) | Strategy | Lambda / `java.util.function` |
| Vary logic at runtime (stateful/complex) | Strategy | Custom `@FunctionalInterface` or interface |
| Add cross-cutting concerns (logging, timing, security) | Decorator | Spring AOP / `@Aspect` |
| Large algorithm skeleton with varying steps | Template Method | Execute Around (`Consumer<T>`) |
| Async/reactive event notification | Observer | Project Reactor (`Flux`) / Kafka |
| Sync in-process event notification | Observer | Spring `ApplicationEventPublisher` |
| Complex object construction | Builder | Builder pattern / Lombok `@Builder` |
| Small immutable data with one-field changes | Builder | Record + Wither methods |
| Single instance | Singleton | Spring `@Component` (default scope) |
| Incompatible interfaces | Adapter | Adapter (unchanged) |

---

## Creational Patterns

### Factory — Exhaustive with Sealed Interfaces (Java 17+)

**Use when:** Need to create objects by type, and the set of types is closed/known at compile time.

Classic factories throw a runtime exception on unknown types. With `sealed` interfaces, the compiler proves the factory is exhaustive — no `default` needed.

```java
// ✅ MODERN FACTORY: Sealed interface + exhaustive switch
public sealed interface Payment permits CreditCard, Paypal, Crypto {}

public record CreditCard(String cardNumber) implements Payment {}
public record Paypal(String email) implements Payment {}
public record Crypto(String walletAddress) implements Payment {}

public class PaymentFactory {
    public static Payment create(String type, String identifier) {
        return switch (type.toUpperCase()) {
            case "CC"     -> new CreditCard(identifier);
            case "PAYPAL" -> new Paypal(identifier);
            case "CRYPTO" -> new Crypto(identifier);
            // No default needed — compiler verifies all permits are covered
        };
    }
}

// Usage
Payment payment = PaymentFactory.create("CC", "4111-1111-1111-1111");
```

> **Why sealed?** Adding a new `permits` type (e.g., `BankTransfer`) without updating the switch causes a **compile error**, not a silent runtime bug.

**With Spring (preferred for bean-managed types):**
```java
public interface NotificationSender {
    void send(String message);
    String getType();
}

@Component
public class EmailSender implements NotificationSender {
    @Override public void send(String message) { /* ... */ }
    @Override public String getType() { return "EMAIL"; }
}

@Component
public class SmsSender implements NotificationSender {
    @Override public void send(String message) { /* ... */ }
    @Override public String getType() { return "SMS"; }
}

@Component
public class NotificationFactory {
    private final Map<String, NotificationSender> senders;

    public NotificationFactory(List<NotificationSender> senderList) {
        this.senders = senderList.stream()
            .collect(Collectors.toMap(
                NotificationSender::getType,
                Function.identity()
            ));
    }

    public NotificationSender getSender(String type) {
        return Optional.ofNullable(senders.get(type))
            .orElseThrow(() -> new IllegalArgumentException("Unknown: " + type));
    }
}
```

---

### Builder — Classic and Record Wither

**Use when:** Object has many parameters, some optional.
**Use Record + Wither instead when:** The object is a small immutable data holder and you only ever need to change one field at a time.

#### Classic Builder
```java
// ✅ Builder pattern — good for complex objects with many optional fields
public class User {
    private final String name;      // required
    private final String email;     // required
    private final int age;          // optional
    private final String phone;     // optional

    private User(Builder builder) {
        this.name    = builder.name;
        this.email   = builder.email;
        this.age     = builder.age;
        this.phone   = builder.phone;
    }

    public static Builder builder(String name, String email) {
        return new Builder(name, email);
    }

    public static class Builder {
        private final String name;
        private final String email;
        private int age = 0;
        private String phone = "";

        private Builder(String name, String email) {
            this.name  = name;
            this.email = email;
        }

        public Builder age(int age)       { this.age = age; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public User build()               { return new User(this); }
    }
}

// Usage
User user = User.builder("John", "john@example.com")
    .age(30)
    .phone("+1234567890")
    .build();
```

**With Lombok:**
```java
@Builder
@Getter
public class User {
    private final String name;
    private final String email;
    @Builder.Default private int age = 0;
    private String phone;
}
```

#### Record + Wither Pattern (Java 14+)

**Use when:** Small, immutable configuration/data objects where you need one-field variations.

```java
// ✅ MODERN DATA PATTERN: Records with "Withers"
public record Config(String url, int timeout, boolean retry) {
    public Config withTimeout(int newTimeout) {
        return new Config(this.url, newTimeout, this.retry);
    }

    public Config withRetry(boolean newRetry) {
        return new Config(this.url, this.timeout, newRetry);
    }
}

// Usage — clean, immutable, no builder boilerplate
Config base       = new Config("https://api.example.com", 3000, false);
Config production = base.withTimeout(5000);
Config resilient  = production.withRetry(true);
```

> **When to choose Wither over Builder:** If the object has ≤5 fields, is always immutable, and you rarely set all fields at construction — use Record + Wither. Use Builder for objects where many optional fields need defaults.

---

### Singleton

**Use when:** Exactly one instance is needed (use sparingly!).

```java
// ✅ Modern singleton (enum-based, thread-safe, serialization-safe)
public enum DatabaseConnection {
    INSTANCE;

    private final Connection connection;

    DatabaseConnection() {
        // Initialize connection
    }

    public Connection getConnection() { return connection; }
}
```

**With Spring (preferred — almost always the right choice):**
```java
@Component  // Default scope is singleton; Spring manages the lifecycle
public class DatabaseConnection {
    // No Singleton boilerplate needed
}
```

> **Warning:** Manual singletons are hard to test (global state) and create hidden dependencies. In Spring applications, always prefer `@Component` with dependency injection.

---

## Behavioral Patterns

### Strategy — As Functional Interfaces (Java 8+)

**Use when:** Multiple algorithms for the same operation, swapped at runtime.

If the strategy has **one method**, treat it as a functional interface — no custom interface needed. Use standard types from `java.util.function`.

```java
// ✅ MODERN STRATEGY: Use standard Functional Interfaces
@Service
public class DiscountService {
    // UnaryOperator<Double> IS the strategy — no custom interface needed
    public double applyDiscount(double total, UnaryOperator<Double> strategy) {
        return strategy.apply(total);
    }
}

// Usage — strategies as lambdas, no class boilerplate
service.applyDiscount(100.0, price -> price * 0.9);         // VIP: 10% off
service.applyDiscount(100.0, price -> price - 10.0);        // Flat: $10 off
service.applyDiscount(100.0, price -> Math.max(price, 50.0)); // Floor: minimum $50

// Named strategies (reusable)
UnaryOperator<Double> vipDiscount      = price -> price * 0.9;
UnaryOperator<Double> seasonalDiscount = price -> price * 0.8;
UnaryOperator<Double> noDiscount       = UnaryOperator.identity();
```

**When a custom interface is still right:** The strategy is stateful, has multiple methods, or carries context that makes a lambda awkward:

```java
// ✅ Custom interface for stateful/multi-method strategies
public interface PaymentStrategy {
    void pay(BigDecimal amount);
    boolean supports(String currency);  // second method → can't be a simple lambda
}

public class CreditCardPayment implements PaymentStrategy {
    private final String cardNumber;
    public CreditCardPayment(String cardNumber) { this.cardNumber = cardNumber; }

    @Override
    public void pay(BigDecimal amount) {
        System.out.println("Paid " + amount + " with card " + cardNumber);
    }

    @Override
    public boolean supports(String currency) { return !currency.equals("CRYPTO"); }
}
```

---

### Execute Around — Modern Replacement for Template Method

**Use when:** You have a fixed "skeleton" (open resource → do work → close resource) but the middle step varies. This is the **compositional alternative** to Template Method.

Template Method requires inheritance (abstract classes). Execute Around passes the varying step as a lambda — used heavily in Spring's `JdbcTemplate`, `TransactionTemplate`, etc.

```java
// ❌ CLASSIC (inheritance-based): Abstract class forces subclassing
public abstract class DataProcessor {
    public final void process() {
        readData();
        processData(); // varies
        writeData();
    }
    protected abstract void readData();
    protected abstract void processData();
    protected abstract void writeData();
}

// ✅ MODERN (composition-based): Execute Around with Consumer<T>
@Service
public class ResourceProcessor {
    public void process(Consumer<Resource> worker) {
        Resource res = open();          // Skeleton step 1: always runs
        try {
            worker.accept(res);         // The varying step — injected as a lambda
        } finally {
            close(res);                 // Skeleton step 2: always runs
        }
    }

    private Resource open()              { /* acquire connection, open file, etc. */ return new Resource(); }
    private void close(Resource res)     { /* release */ }
}

// Usage — no subclassing required
processor.process(res -> res.write(csvData));
processor.process(res -> res.write(apiResponse));
processor.process(res -> {
    var transformed = transform(res.read());
    res.write(transformed);
});
```

**JdbcTemplate is the canonical example of Execute Around:**
```java
// Spring's JdbcTemplate uses Execute Around internally
jdbcTemplate.execute(conn -> {
    // Your varying logic here; open/close handled by the template
    return conn.prepareStatement("SELECT ...").executeQuery();
});
```

> **Template Method still applies** when you genuinely want subclasses to extend behavior and inheritance is intentional (e.g., framework hooks). Execute Around is the first choice in application code.

---

### Observer — Spring Events and Reactive Streams

**Use when:** Objects need to be notified of changes in another object.

#### Synchronous: Spring ApplicationEventPublisher (preferred in-process)
```java
// Event (use a record — immutable, no boilerplate)
public record OrderPlacedEvent(Order order) {}

// Publisher
@Service
public class OrderService {
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void placeOrder(Order order) {
        saveOrder(order);
        eventPublisher.publishEvent(new OrderPlacedEvent(order));
    }
}

// Listeners (observers) — decoupled, no manual registration
@Component
public class InventoryListener {
    @EventListener
    public void handleOrderPlaced(OrderPlacedEvent event) {
        // Reduce inventory
    }
}

@Component
public class EmailListener {
    @EventListener
    @Async  // Non-blocking
    public void handleOrderPlacedAsync(OrderPlacedEvent event) {
        // Send confirmation email
    }
}
```

#### Reactive: Project Reactor Flux (cloud-native, non-blocking)

**Use when:** The classic Observer is synchronous and can block threads. For cloud-native Java with Spring WebFlux, Observer has evolved into Reactive Streams.

```java
// ✅ MODERN OBSERVER: Flux/Mono (Project Reactor)
public Flux<Order> observeOrders() {
    return orderRepo.findAll()
        .doOnNext(order -> log.info("Processing: {}", order.getId())) // Observer 1
        .filter(order -> order.getAmount().compareTo(BigDecimal.valueOf(100)) > 0)
        .map(this::enrichData);                                        // Observer 2
}

// Subscribers (observers) attach at runtime
orderService.observeOrders()
    .subscribe(
        order -> inventoryService.reduce(order),   // onNext
        err   -> log.error("Error: {}", err),       // onError
        ()    -> log.info("Stream completed")       // onComplete
    );
```

> **Choosing between them:**
> - **Spring Events** → in-process, simpler, synchronous or `@Async`
> - **Flux/Reactor** → non-blocking pipelines, backpressure, streaming data
> - **Kafka/RabbitMQ** → cross-service, durable, distributed observers

---

## Structural Patterns

### Decorator — Spring AOP (preferred) and Classic Chain

**Use when:** Add behavior (logging, security, caching, timing) without modifying existing classes.

#### Spring AOP Decorator (preferred for cross-cutting concerns)

In Spring applications, manual decorator chains are rarely written. AOP applies "decoration" to methods via aspects — cleaner and non-invasive.

```java
// 1. Define a marker annotation
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {}

// 2. Write the Aspect (the "decorator")
@Aspect
@Component
public class LoggingDecorator {

    @Around("@annotation(LogExecutionTime)")
    public Object logTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        log.info("{} executed in {} ms",
            joinPoint.getSignature().getName(),
            System.currentTimeMillis() - start);
        return result;
    }
}

// 3. Apply the annotation — zero decorator boilerplate at the call site
@Service
public class OrderService {

    @LogExecutionTime
    public Order processOrder(OrderRequest request) {
        // business logic unchanged
    }
}
```

#### Classic Decorator Chain (for composable, runtime behavior)

Still applicable when decorators need to be composed programmatically at runtime (e.g., building a pipeline of transformations).

```java
// ✅ Classic Decorator — good for composable, data-transformation pipelines
public interface Coffee {
    String getDescription();
    BigDecimal getCost();
}

public class SimpleCoffee implements Coffee {
    @Override public String getDescription() { return "Coffee"; }
    @Override public BigDecimal getCost()    { return new BigDecimal("2.00"); }
}

// Base decorator
public abstract class CoffeeDecorator implements Coffee {
    protected final Coffee coffee;
    public CoffeeDecorator(Coffee coffee) { this.coffee = coffee; }
    @Override public String getDescription() { return coffee.getDescription(); }
    @Override public BigDecimal getCost()    { return coffee.getCost(); }
}

public class MilkDecorator extends CoffeeDecorator {
    public MilkDecorator(Coffee coffee) { super(coffee); }
    @Override public String getDescription() { return coffee.getDescription() + ", Milk"; }
    @Override public BigDecimal getCost()    { return coffee.getCost().add(new BigDecimal("0.50")); }
}

public class WhippedCreamDecorator extends CoffeeDecorator {
    public WhippedCreamDecorator(Coffee coffee) { super(coffee); }
    @Override public String getDescription() { return coffee.getDescription() + ", Whipped Cream"; }
    @Override public BigDecimal getCost()    { return coffee.getCost().add(new BigDecimal("0.70")); }
}

// Compose at runtime
Coffee coffee = new WhippedCreamDecorator(new MilkDecorator(new SimpleCoffee()));
System.out.println(coffee.getDescription()); // Coffee, Milk, Whipped Cream
System.out.println(coffee.getCost());        // 3.20
```

> **Choosing between them:**
> - **AOP** → cross-cutting concerns (logging, auth, metrics, transactions). Applies to any method via annotation.
> - **Classic chain** → domain-level composition where the set of decorators varies per object at runtime.

**Java I/O is the canonical classic Decorator example:**
```java
BufferedReader reader = new BufferedReader(
    new InputStreamReader(
        new FileInputStream("file.txt")
    )
);
```

---

### Adapter

**Use when:** Make incompatible interfaces work together (integrating legacy code or third-party libraries).

```java
// ✅ Adapter pattern
public interface MediaPlayer {
    void play(String filename);
}

// Legacy/third-party — can't modify
public class LegacyAudioPlayer {
    public void playMp3(String filename) {
        System.out.println("Playing MP3: " + filename);
    }
}

public class AdvancedVideoPlayer {
    public void playMp4(String filename) { System.out.println("Playing MP4: " + filename); }
    public void playAvi(String filename) { System.out.println("Playing AVI: " + filename); }
}

// Adapters bridge the gap
public class Mp3PlayerAdapter implements MediaPlayer {
    private final LegacyAudioPlayer legacyPlayer = new LegacyAudioPlayer();
    @Override
    public void play(String filename) { legacyPlayer.playMp3(filename); }
}

public class VideoPlayerAdapter implements MediaPlayer {
    private final AdvancedVideoPlayer videoPlayer = new AdvancedVideoPlayer();
    @Override
    public void play(String filename) {
        if (filename.endsWith(".mp4"))      videoPlayer.playMp4(filename);
        else if (filename.endsWith(".avi")) videoPlayer.playAvi(filename);
    }
}

// Usage — caller only knows MediaPlayer
MediaPlayer mp3   = new Mp3PlayerAdapter();
MediaPlayer video = new VideoPlayerAdapter();
mp3.play("song.mp3");
video.play("movie.mp4");
```

---

## Anti-Patterns to Avoid

| Anti-Pattern | Problem | Better Approach |
|---|---|---|
| Singleton abuse | Global state, hard to test, hidden deps | Spring `@Component` + DI |
| Factory with unchecked `default` | Silent runtime failures on new types | `sealed` interface + exhaustive `switch` |
| Deep decorator chains | Hard to debug, hard to trace | Keep chains ≤3; prefer AOP for cross-cutting |
| Observer with many sync listeners | Thread blocking, ordering surprises | `@Async` listeners or Reactor `Flux` |
| Template Method by default | Locks callers into inheritance | Execute Around with `Consumer<T>` first |
| Builder for tiny immutable objects | Unnecessary boilerplate | Record + Wither |

---

## Related Skills

- `solid-principles` — Design principles that patterns help implement
- `clean-code` — Code-level best practices
- `spring-boot-patterns` — Spring-specific implementations