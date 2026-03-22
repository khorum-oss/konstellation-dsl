# Konstellation Meta DSL — KSP Processor Implementation Guide

This document describes how the `konstellation-meta-dsl` annotations and runtime utilities should be processed by the `konstellation-dsl` KSP processor to generate type-safe Kotlin DSL code.

## Overview

The meta-dsl library defines **annotations** (compile-time metadata) and **runtime utilities** (validation helpers, builder interface). The KSP processor in `konstellation-dsl` reads these annotations during compilation and generates builder classes, accessor functions, and validation logic.

```
┌─────────────────────────┐      reads       ┌───────────────────────────┐
│  User's annotated data  │ ───────────────── │  KSP Processor            │
│  classes using meta-dsl │                   │  (konstellation-dsl)      │
│  annotations            │                   │                           │
└─────────────────────────┘                   └────────┬──────────────────┘
                                                       │ generates
                                                       ▼
                                              ┌───────────────────────────┐
                                              │  Builder classes          │
                                              │  DSL accessor functions   │
                                              │  Validation in build()    │
                                              │  (uses DslValidation)     │
                                              └───────────────────────────┘
```

## Annotation Reference

### `@GeneratedDsl` (class-level) — Core Entry Point

**Input:** A data class annotated with `@GeneratedDsl`.

**Output:** For each annotated class `Foo`, generate:

1. **`FooBuilder`** class implementing `CoreDslBuilder<Foo>`
   - A mutable property for each constructor parameter of `Foo`
   - Nullable types for required properties (initially `null`), direct types for optional ones (using defaults)
   - A `build()` method that constructs `Foo` from builder state, with validation

2. **`foo { }` accessor function** (camelCase of class name)
   - Takes a lambda `FooBuilder.() -> Unit`, creates a builder, applies the lambda, calls `build()`

3. **Nested builder functions** — if a property's type is also `@GeneratedDsl`, generate a nested DSL function:
   ```kotlin
   // Inside ParentBuilder
   fun child(block: ChildBuilder.() -> Unit) {
       this.child = ChildBuilder().apply(block).build()
   }
   ```

**Parameters:**
- `name` - if provided should override the builder class name
- `debug = true` — emit debug logging during KSP processing
- `withListGroup`, `withMapGroup`, `isRoot` — **DEPRECATED**, see `@ListDsl`, `@MapDsl`, `@RootDsl`

---

### `@RootDsl` (property-level) — Root Entry Point

Replaces `@GeneratedDsl(isRoot = true)`. Marks a property as the root DSL entry point with more control.

**Parameters:**
- `name: String = ""` — custom name for the root DSL function (defaults to camelCase of class name)
- `alias: String = ""` — optional alias that creates a separate builder entry point

**Effect:** When present, the processor should generate a top-level DSL function at the root classpath. If `name` is provided, use it instead of the class name. If `alias` is provided, generate a second function under that name.

```kotlin
@GeneratedDsl
data class AppConfig(
    @RootDsl(name = "app", alias = "application")
    val config: DatabaseConfig
)

// Generated:
fun app(block: DatabaseConfigBuilder.() -> Unit): DatabaseConfig = ...
fun application(block: DatabaseConfigBuilder.() -> Unit): DatabaseConfig = ...
```

---

### `@PublicDslProperty` (property-level) — Public Property Accessor Control

Controls how generated accessors behave for **public** properties. By default, public properties get a direct setter in the builder.

**Parameters:**
- `restrictSetter: Boolean = false` — when `true`, the direct setter (`builder.prop = value`) is not generated
- `wrapInFunction: Boolean = false` — when `true`, a function-based accessor (`prop(value)`) is also generated

**Combinations:**

| `restrictSetter` | `wrapInFunction` | Generated Accessors |
|-------------------|-------------------|---------------------|
| `false` (default) | `false` (default) | setter only |
| `false` | `true` | setter + function |
| `true` | `false` | nothing (no standard accessor) |
| `true` | `true` | function only |

```kotlin
// restrictSetter=false, wrapInFunction=false (default for unannotated public props):
var name: String? = null

// wrapInFunction=true:
var amount: Int? = null
fun amount(value: Int) { this.amount = value }

// restrictSetter=true, wrapInFunction=true:
private var address: String? = null
fun address(value: String) { this.address = value }
```

---

### `@PrivateDslProperty` (property-level) — Private Property Accessor Control

Controls how generated accessors behave for **private** properties. Key difference from `@PublicDslProperty`: `wrapInFunction` defaults to `true`.

**Parameters:**
- `restrictSetter: Boolean = false` — when `true`, the direct setter is not generated
- `wrapInFunction: Boolean = true` — when `true`, a function-based accessor is generated

**Combinations:**

| `restrictSetter` | `wrapInFunction` | Generated Accessors |
|-------------------|-------------------|---------------------|
| `false` (default) | `true` (default) | setter + function |
| `false` | `false` | setter only |
| `true` | `true` | function only |
| `true` | `false` | nothing |

---

### `@DefaultValue` (property-level) — Builder Default Values

**Effect:** Set the builder property's initial value instead of `null`.

**Processing logic:**
1. If `inferType = true` (default): determine the type from the property declaration
   - Primitive types (`String`, `Int`, `Long`, `Boolean`, `Float`, `Double`, `Short`, `Byte`, `Char`) — emit the `value` string directly as a literal
   - Enum types — emit `EnumClass.VALUE` (resolve from the property's declared type)
   - Other types — use `packageName` and `className` if inference isn't possible
2. If `inferType = false`: use `packageName` and `className` to qualify the value

```kotlin
// Annotation:
@DefaultValue("42")
val port: Int

// Generated builder property:
var port: Int = 42
```

---

### `@DslDescription` (class or property-level) — KDoc Generation

**Effect:** Emit KDoc comments on generated code.

- On a class → KDoc on the generated builder class
- On a property → KDoc on the generated builder property and its accessor functions

```kotlin
// Annotation:
@DslDescription("The server hostname")
val host: String

// Generated:
/** The server hostname */
var host: String? = null
```

---

### `@DslAlias` (property-level) — Alternative Accessor Names

**Effect:** Generate additional accessor functions with alternative names, all pointing to the same property.

```kotlin
// Annotation:
@DslAlias("desc")
val description: String? = null

// Generated (in addition to the normal `description` accessor):
fun desc(value: String) { this.description = value }
```

For list/map properties, alias functions should mirror the same vararg/provider pattern as the primary property.

---

### `@DeprecatedDsl` (property-level) — Deprecation Markers

**Effect:** Add `@Deprecated` annotation to the generated accessor functions.

```kotlin
// Annotation:
@DeprecatedDsl(message = "Use 'endpoint' instead.", replaceWith = "endpoint")
val url: String? = null

// Generated:
@Deprecated("Use 'endpoint' instead.", replaceWith = ReplaceWith("endpoint"))
fun url(value: String) { this.url = value }
```

- If `replaceWith` is empty, omit the `ReplaceWith` argument.

---

### `@ValidateDsl` (property-level) — Custom Validation

**Effect:** Emit a `require()` call in the generated `build()` method.

```kotlin
// Annotation:
@ValidateDsl(expression = "it > 0", message = "capacity must be positive")
val capacity: Int

// Generated in build():
require(capacity > 0) { "capacity must be positive" }
```

- `it` in the expression should be replaced with the actual property name.
- If `message` is empty, use a default message like `"Validation failed for property 'capacity'"`.
- The expression is emitted as-is into Kotlin source, so it's validated by the Kotlin compiler.

---

### `@ListDsl` (property-level) — List Property Configuration

Replaces the old `@ListConfig` + `@DslProperty` combination. Controls both constraints/transformations and accessor generation for `List` properties.

**Parameters:**
- `minSize: Int = -1` — minimum elements required (`-1` = no minimum)
- `maxSize: Int = -1` — maximum elements allowed (`-1` = no maximum)
- `uniqueElements: Boolean = false` — apply `.distinct()` before building
- `sorted: Boolean = false` — apply `.sorted()` before building
- `withVararg: Boolean = true` — generate a vararg function: `tags("a", "b")`
- `withProvider: Boolean = true` — generate a provider function: `tags { listOf("a") }`

**Processing order in `build()`:**
1. Apply `.distinct()` if `uniqueElements = true`
2. Apply `.sorted()` if `sorted = true`
3. Emit `DslValidation.requireMinSize(list, minSize, "propertyName")` if `minSize >= 0`
4. Emit `DslValidation.requireMaxSize(list, maxSize, "propertyName")` if `maxSize >= 0`

**Generated accessors (based on `withVararg`/`withProvider`):**
```kotlin
// withVararg = true:
fun tags(vararg items: String) { this.tags = items.toList() }

// withProvider = true:
fun tags(provider: () -> List<String>) { this.tags = provider() }
```

---

### `@MapDsl` (property-level) — Map Property Configuration

Replaces the old `@MapConfig` + `@DslProperty` combination. Controls constraints and accessor generation for `Map` properties.

**Parameters:**
- `minSize: Int = -1` — minimum entries required (`-1` = no minimum)
- `maxSize: Int = -1` — maximum entries allowed (`-1` = no maximum)
- `withVararg: Boolean = true` — generate a vararg function
- `withProvider: Boolean = true` — generate a provider function

**Processing in `build()`:**
- Emit `DslValidation.requireMinSize(map, minSize, "propertyName")` if `minSize >= 0`
- Emit `DslValidation.requireMaxSize(map, maxSize, "propertyName")` if `maxSize >= 0`

---

### `@TransientDsl` (property-level) — Exclude from Builder

**Effect:** The property is completely excluded from the generated DSL builder. The processor should skip it during code generation.

**Parameters:**
- `reason: String = ""` — optional documentation reason (not emitted in generated code)

```kotlin
@GeneratedDsl
data class Config(
    val name: String,

    @TransientDsl(reason = "Internal tracking field")
    val internalId: String = ""
)

// Generated builder has NO `internalId` property or accessor.
// The build() method should use the default value from the data class constructor.
```

---

### `@InjectDslMethod` (property-level) — Method Injection

**Effect:** Marks a property whose value should be injected as a method into the generated builder rather than being a simple setter property.

The processor should generate a method in the builder that allows the user to define the property's value through a function call pattern rather than direct assignment.

---

### `@SingleEntryTransformDsl` (property-level) — Transform Functions

**Effect:** Generate a convenience function that accepts a single value of `inputType` and transforms it.

```kotlin
// Annotation:
@SingleEntryTransformDsl(inputType = Long::class, transformTemplate = "Duration.ofMillis(%N)")
val timeout: Duration

// Generated:
fun timeout(value: Long) { this.timeout = Duration.ofMillis(value) }
```

- `%N` in the template is replaced with the parameter name.

---

## Runtime Utilities — What Generated Code Should Call

### `CoreDslBuilder<T>`

Every generated builder must implement this interface:

```kotlin
class FooBuilder : CoreDslBuilder<Foo> {
    // mutable properties...
    override fun build(): Foo { /* validation + construction */ }
}
```

### `DslValidation` (object)

Generated `build()` methods should call `DslValidation` for all validation:

| Scenario | Call |
|----------|------|
| Required (non-nullable, no default) property | `DslValidation.requireNotNull(::propertyName)` |
| Required non-empty collection | `DslValidation.requireCollectionNotEmpty(::propertyName)` |
| Required non-empty map | `DslValidation.requireMapNotEmpty(::propertyName)` |
| `@ListDsl(minSize=N)` | `DslValidation.requireMinSize(collection, N, "name")` |
| `@ListDsl(maxSize=N)` | `DslValidation.requireMaxSize(collection, N, "name")` |
| `@MapDsl(minSize=N)` | `DslValidation.requireMinSize(map, N, "name")` |
| `@MapDsl(maxSize=N)` | `DslValidation.requireMaxSize(map, N, "name")` |

**Important:** The old top-level `vRequire*` functions in `RequiredValidation.kt` are deprecated. New generated code should use `DslValidation.*` directly.

### `MapGroupType` (enum)

Used at generation time to determine which map-group accessor patterns to emit. The `ACTIVE_TYPES` companion list (`SINGLE`, `LIST`, `ALL`) is useful for iterating over non-NONE types.

---

## Generated `build()` Method Structure

The generated `build()` method should follow this order:

```kotlin
override fun build(): Foo {
    // 1. Validate required properties
    val host = DslValidation.requireNotNull(::host)

    // 2. Apply list transformations (unique, sorted) from @ListDsl
    val processedTags = tags?.distinct()?.sorted()

    // 3. Validate size constraints (@ListDsl, @MapDsl)
    processedTags?.let { DslValidation.requireMinSize(it, 1, "tags") }
    processedTags?.let { DslValidation.requireMaxSize(it, 10, "tags") }

    // 4. Validate custom expressions (@ValidateDsl)
    require(capacity > 0) { "capacity must be positive" }

    // 5. Construct the data class (skip @TransientDsl properties, use defaults)
    return Foo(
        host = host,
        tags = processedTags ?: emptyList(),
        capacity = capacity
    )
}
```

---

## Property Visibility Decision Tree

When generating accessors for a property, the processor should follow this logic:

```
Is property annotated with @TransientDsl?
  └─ YES → skip entirely, use default value in build()

Is property a List type?
  └─ YES → use @ListDsl if present (withVararg, withProvider, constraints)
           otherwise generate default list accessors

Is property a Map type?
  └─ YES → use @MapDsl if present (withVararg, withProvider, constraints)
           otherwise generate default map accessors

Is property private?
  └─ YES → check for @PrivateDslProperty (defaults: restrictSetter=false, wrapInFunction=true)
  └─ NO  → check for @PublicDslProperty (defaults: restrictSetter=false, wrapInFunction=false)
           if not annotated, generate setter only

Has @DslAlias?
  └─ YES → generate additional accessors mirroring the primary accessor pattern

Has @DeprecatedDsl?
  └─ YES → add @Deprecated to all generated accessors for this property

Has @InjectDslMethod?
  └─ YES → generate method injection pattern instead of standard accessor
```

---

## Deprecated Annotation Migration

The following old annotations have been replaced. The processor should support both during the transition period:

| Old Annotation | New Annotation | Notes |
|----------------|----------------|-------|
| `@Alias` | `@DslAlias` | Same semantics |
| `@Description` | `@DslDescription` | Same semantics |
| `@Validate` | `@ValidateDsl` | Same semantics |
| `@ListConfig` | `@ListDsl` | Now includes `withVararg`/`withProvider` (previously on `@DslProperty`) |
| `@MapConfig` | `@MapDsl` | Now includes `withVararg`/`withProvider` (previously on `@DslProperty`) |
| `@DslProperty` | `@ListDsl`/`@MapDsl`/`@PublicDslProperty`/`@PrivateDslProperty` | Functionality split across new annotations |
| `@GeneratedDsl(isRoot=true)` | `@RootDsl` | More control via `name` and `alias` parameters |

---

## Complete Example

```kotlin
@GeneratedDsl
@DslDescription("Application configuration")
data class AppConfig(
    @DslDescription("The application name")
    val name: String,

    @DefaultValue("8080")
    val port: Int,

    @DslAlias("desc")
    @DslDescription("Optional description")
    val description: String? = null,

    @ValidateDsl(expression = "it > 0 && it <= 65535", message = "port must be 1-65535")
    val customPort: Int? = null,

    @ListDsl(minSize = 1, uniqueElements = true, withVararg = true, withProvider = true)
    val tags: List<String>,

    @MapDsl(maxSize = 50)
    val headers: Map<String, String> = emptyMap(),

    @DeprecatedDsl(message = "Use 'name' instead", replaceWith = "name")
    val label: String? = null,

    @TransientDsl(reason = "Internal tracking")
    val traceId: String = "",

    @PublicDslProperty(wrapInFunction = true, restrictSetter = true)
    val endpoint: String,

    val database: DatabaseConfig
)

@GeneratedDsl
data class DatabaseConfig(
    val host: String,
    @DefaultValue("5432")
    val port: Int,
    val database: String
)
```

This would generate builders with:
- Required validation for `name`, `tags`, `endpoint`, `database`
- Default value `8080` for `port`
- `desc(...)` alias function alongside `description(...)`
- `@Deprecated` on `label(...)` accessor
- `require(customPort > 0 && customPort <= 65535)` in `build()`
- `.distinct()` + `requireMinSize(tags, 1, "tags")` for tags
- `requireMaxSize(headers, 50, "headers")` for headers
- Nested `database { }` DSL block
- KDoc on builder class and relevant properties
- `traceId` excluded from builder, uses default `""` in `build()`
- `endpoint` accessible only via function, not direct setter
