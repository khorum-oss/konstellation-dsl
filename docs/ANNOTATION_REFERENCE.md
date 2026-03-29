# Konstellation Annotation Reference

Complete guide to all Konstellation annotations with usage examples.

---

## `@GeneratedDsl` (class-level)

The core entry point. Marks a data class for DSL builder generation.

**Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `name` | `String` | `""` | Override the builder class name |
| `debug` | `Boolean` | `false` | Emit debug logging during KSP processing |
| `isRoot` | `Boolean` | `false` | *Deprecated.* Use `@RootDsl` instead |
| `withListGroup` | `MapGroupType` | `NONE` | *Deprecated.* Use `@ListDsl` instead |
| `withMapGroup` | `MapGroupType` | `NONE` | *Deprecated.* Use `@MapDsl` instead |

**Generated output** for each annotated class `Foo`:
1. `FooDslBuilder` class implementing `CoreDslBuilder<Foo>`
2. `foo { }` accessor function (camelCase of class name)
3. Nested builder functions for properties whose types are also `@GeneratedDsl`

```kotlin
@GeneratedDsl
data class DatabaseConfig(
    val host: String,
    val port: Int,
    val database: String
)

// Usage:
val config = databaseConfig {
    host = "localhost"
    port = 5432
    database = "myapp"
}
```

---

## `@RootDsl` (property-level)

Generates a top-level DSL entry-point function for the annotated property's type, with custom naming and an optional alias.

> **Planned change:** A future meta-dsl release will change `@RootDsl` to a class-level annotation (`@Target(CLASS)`). The processor already supports this transition.

**Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `name` | `String` | `""` | Custom name for the root function (defaults to camelCase of the type name) |
| `alias` | `String` | `""` | Optional second entry-point function name |

```kotlin
@GeneratedDsl
data class MissionControl(
    val missionName: String,

    @RootDsl(name = "mission", alias = "missionControl")
    val command: FleetCommand
)

// Generated:
fun mission(block: FleetCommandDslBuilder.() -> Unit): FleetCommand = ...
fun missionControl(block: FleetCommandDslBuilder.() -> Unit): FleetCommand = ...
```

---

## `@DefaultValue` (property-level)

Sets the builder property's initial value instead of `null`.

**Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `value` | `String` | *required* | The default value as a string |
| `packageName` | `String` | `""` | Package name for non-primitive types |
| `className` | `String` | `""` | Class name for non-primitive types |
| `inferType` | `Boolean` | `true` | Auto-detect primitive types from the property declaration |

When `inferType = true` and no explicit `packageName`/`className`:
- **Primitives** (`Int`, `Long`, `Float`, `Double`, `Boolean`, `Short`, `Byte`, `Char`): emitted as literals
- **Other types**: emitted as string values

For enum or class-reference defaults, provide `packageName` and `className` explicitly.

```kotlin
// Primitive default
@DefaultValue("42")
val port: Int = 42

// String default
@DefaultValue("DEFAULT")
val region: String = "DEFAULT"

// Enum default
@DefaultValue("Version.V1", packageName = "com.example", className = "Version")
val version: Version = Version.V1
```

---

## `@DefaultState` (property-level)

Specifies a predefined default state for a property using the `DefaultStateType` enum. Preferred over `@DefaultValue` for common defaults (empty string, zero, empty collections, booleans) to avoid string-escaping issues.

**Mutual exclusivity:** A property must not have both `@DefaultState` and `@DefaultValue`. If both are present, `@DefaultState` takes precedence and a warning is emitted.

**Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `type` | `DefaultStateType` | *required* | The predefined default state to apply |

**`DefaultStateType` values:**

| Entry | Code Snippet | Category |
|---|---|---|
| `EMPTY_STRING` | `""` | String |
| `ZERO_INT` | `0` | Numeric |
| `ZERO_LONG` | `0L` | Numeric |
| `ZERO_DOUBLE` | `0.0` | Numeric |
| `ZERO_FLOAT` | `0.0f` | Numeric |
| `EMPTY_LIST` | `mutableListOf()` | Collection |
| `EMPTY_MAP` | `mutableMapOf()` | Collection |
| `TRUE` | `true` | Boolean |
| `FALSE` | `false` | Boolean |

```kotlin
@GeneratedDsl
data class SensorConfig(
    @DefaultState(DefaultStateType.EMPTY_STRING)
    val sensorName: String,

    @DefaultState(DefaultStateType.ZERO_INT)
    val retryCount: Int,

    @DefaultState(DefaultStateType.EMPTY_LIST)
    val tags: MutableList<String>,

    @DefaultState(DefaultStateType.FALSE)
    val enabled: Boolean
)

// Generated builder properties:
// var sensorName: String? = ""
// var retryCount: Int? = 0
// var tags: MutableList<String>? = mutableListOf()
// var enabled: Boolean? = false
```

---

## `@ListDsl` (property-level)

Configures list property behavior: size constraints, element transformations, and accessor generation.

**Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `minSize` | `Int` | `-1` | Minimum elements required (`-1` = no minimum) |
| `maxSize` | `Int` | `-1` | Maximum elements allowed (`-1` = no maximum) |
| `uniqueElements` | `Boolean` | `false` | Apply `.distinct()` before building |
| `sorted` | `Boolean` | `false` | Apply `.sorted()` before building |
| `withVararg` | `Boolean` | `true` | Generate a vararg function: `tags("a", "b")` |
| `withProvider` | `Boolean` | `true` | Generate a provider function: `tags { add("a") }` |

**Processing order in `build()`:**
1. `.distinct()` if `uniqueElements = true`
2. `.sorted()` if `sorted = true`
3. `DslValidation.requireMinSize(...)` if `minSize >= 0`
4. `DslValidation.requireMaxSize(...)` if `maxSize >= 0`

```kotlin
@ListDsl(minSize = 1, maxSize = 10, uniqueElements = true, sorted = true)
val shipNames: List<String>

// Generated accessors:
fun shipNames(vararg items: String) { ... }          // withVararg = true
fun shipNames(block: MutableList<String>.() -> Unit)  // withProvider = true
```

**Controlling accessors:**

```kotlin
@ListDsl(withProvider = false)     // vararg only
val priorities: List<Int>? = null

@ListDsl(withVararg = false)       // provider only
val objectives: List<String>? = null

@ListDsl(withVararg = false, withProvider = false)  // no accessors (direct assignment only)
val raw: List<Int>? = null
```

---

## `@MapDsl` (property-level)

Configures map property behavior: size constraints and accessor generation.

**Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `minSize` | `Int` | `-1` | Minimum entries required (`-1` = no minimum) |
| `maxSize` | `Int` | `-1` | Maximum entries allowed (`-1` = no maximum) |
| `withVararg` | `Boolean` | `true` | Generate a vararg function |
| `withProvider` | `Boolean` | `true` | Generate a provider function |

```kotlin
@MapDsl(minSize = 1, maxSize = 5)
val shipAssignments: Map<String, String>

// Generated accessors:
fun shipAssignments(vararg pairs: Pair<String, String>) { ... }
fun shipAssignments(block: MutableMap<String, String>.() -> Unit) { ... }
```

---

## `@DslDescription` (property-level)

Adds KDoc comments to generated builder properties and accessor functions.

```kotlin
@DslDescription("Maximum warp speed the ship can achieve")
val maxWarpSpeed: Float? = null

// Generated:
/** Maximum warp speed the ship can achieve */
var maxWarpSpeed: Float? = null
```

---

## `@DslAlias` (property-level)

Creates additional accessor functions with alternative names, all pointing to the same property.

**Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `names` | `String[]` | *required* | One or more alias names |

```kotlin
@DslAlias(names = ["active"])
val activated: Boolean? = null

// Generated (in addition to the normal `activated` accessor):
fun active(value: Boolean) { this.activated = value }
```

For list/map properties, alias functions mirror the same vararg/provider pattern as the primary property.

---

## `@DeprecatedDsl` (property-level)

Adds `@Deprecated` annotation to all generated accessor functions for this property.

**Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `message` | `String` | *required* | Deprecation message |
| `replaceWith` | `String` | `""` | Suggested replacement (generates `ReplaceWith`) |

```kotlin
@DeprecatedDsl(message = "Use 'activated' instead", replaceWith = "activated")
val docked: Boolean? = null

// Generated:
@Deprecated("Use 'activated' instead", replaceWith = ReplaceWith("activated"))
fun docked(value: Boolean) { this.docked = value }
```

When combined with `@DslAlias`, the `@Deprecated` annotation is applied to both the primary and alias accessors.

---

## `@ValidateDsl` (property-level)

Generates a `require()` call in the builder's `build()` method.

**Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `expression` | `String` | *required* | Boolean expression (use `it` for the property name) |
| `message` | `String` | `""` | Error message (defaults to `"Validation failed for property 'name'"`) |

The `it` placeholder is replaced with the actual property name at generation time.

```kotlin
@ValidateDsl(expression = "it?.let { v -> v > 0 } ?: true", message = "must be positive")
val hullIntegrity: Int? = null

// Generated in build():
require(hullIntegrity?.let { v -> v > 0 } ?: true) { "must be positive" }
```

---

## `@TransientDsl` (property-level)

Completely excludes a property from the generated DSL builder. The `build()` method uses the data class constructor's default value.

**Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `reason` | `String` | `""` | Documentation reason (not emitted in generated code) |

```kotlin
@GeneratedDsl
data class Config(
    val name: String,

    @TransientDsl(reason = "Internal tracking field")
    val internalId: String? = null
)

// Generated builder has NO `internalId` property or accessor.
```

---

## `@DslProperty` (property-level)

Controls vararg/provider accessor generation for list and map properties. **Superseded by `@ListDsl` and `@MapDsl`** which include the same `withVararg`/`withProvider` parameters plus additional features.

**Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `withVararg` | `Boolean` | `true` | Generate a vararg accessor function |
| `withProvider` | `Boolean` | `true` | Generate a provider accessor function |

When `@ListDsl` or `@MapDsl` is present on the same property, their `withVararg`/`withProvider` values take precedence over `@DslProperty`.

---

## `@PublicDslProperty` (property-level)

Controls accessor generation for public properties.

**Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `restrictSetter` | `Boolean` | `false` | When `true`, no direct setter is generated |
| `wrapInFunction` | `Boolean` | `false` | When `true`, a function-based accessor is generated |

| `restrictSetter` | `wrapInFunction` | Generated |
|---|---|---|
| `false` | `false` | setter only (default) |
| `false` | `true` | setter + function |
| `true` | `false` | nothing |
| `true` | `true` | function only |

```kotlin
@PublicDslProperty(wrapInFunction = true, restrictSetter = true)
val endpoint: String

// Generated: function only (no direct setter)
private var endpoint: String? = null
fun endpoint(value: String) { this.endpoint = value }
```

---

## `@PrivateDslProperty` (property-level)

Controls accessor generation for private properties. Key difference from `@PublicDslProperty`: `wrapInFunction` defaults to `true`.

**Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `restrictSetter` | `Boolean` | `false` | When `true`, no direct setter is generated |
| `wrapInFunction` | `Boolean` | `true` | When `true`, a function-based accessor is generated |

---

## `@SingleEntryTransformDsl` (class-level)

Applied to a class to mark it as a single-entry transform type. Generates a convenience function that accepts a single value and transforms it.

**Parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `inputType` | `KClass<*>` | *required* | The input type for the transform function |
| `transformTemplate` | `String` | `""` | Template string (`%N` = parameter name) |

```kotlin
@SingleEntryTransformDsl(inputType = Long::class, transformTemplate = "Duration.ofMillis(%N)")
data class Timeout(val value: Duration)

// When a property uses this type:
val timeout: Timeout

// Generated:
fun timeout(value: Long) { this.timeout = Duration.ofMillis(value) }
```

---

## Annotation Precedence

When multiple annotations affect the same property:

1. `@TransientDsl` — if present, the property is skipped entirely
2. `@DefaultState` / `@DefaultValue` — sets the builder property initial value (`@DefaultState` takes precedence if both present)
3. `@ListDsl` / `@MapDsl` — controls `withVararg`/`withProvider` (takes precedence over `@DslProperty`)
4. `@DslProperty` — fallback for `withVararg`/`withProvider`
5. `@DeprecatedDsl` — applied to all generated accessors (including aliases)
6. `@DslAlias` — generates additional accessor functions mirroring the primary pattern
7. `@ValidateDsl` — emits validation in `build()`
8. `@DslDescription` — adds KDoc

---

## Migration from Deprecated Annotations

| Old | New | Notes |
|---|---|---|
| `@Alias` | `@DslAlias` | Same semantics |
| `@Description` | `@DslDescription` | Same semantics |
| `@Validate` | `@ValidateDsl` | Same semantics |
| `@ListConfig` | `@ListDsl` | Now includes `withVararg`/`withProvider` |
| `@MapConfig` | `@MapDsl` | Now includes `withVararg`/`withProvider` |
| `@DslProperty` | `@ListDsl` / `@MapDsl` | `withVararg`/`withProvider` moved to type-specific annotations |
| `@GeneratedDsl(isRoot=true)` | `@RootDsl` | More control via `name` and `alias` |
