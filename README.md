# Konstellation

A robust Kotlin Symbol Processing (KSP) library for automatically generating type-safe Kotlin DSLs from annotated data classes.

> **This project is currently in active development and APIs may change.**

## Overview

Konstellation eliminates the boilerplate of creating Kotlin DSLs by automatically generating builder patterns, DSL markers, and type-safe configuration interfaces from your existing data classes. Simply annotate your classes and let KSP handle the rest.

## Features

- **Zero Runtime Overhead** - Pure compile-time code generation
- **Type Safety** - Generated DSLs maintain full type safety
- **Minimal Setup** - Just add annotations to existing classes
- **Rich Logging** - Tiered debug output for development insights
- **Flexible Configuration** - Customizable through KSP arguments

## Quick Start

### 1. Add Dependencies

In your `build.gradle.kts`:

```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
}

dependencies {
    implementation("io.violabs.konstellation:meta-dsl:x.x.x")
    ksp("io.violabs.konstellation:dsl:x.x.x")
}

// Configure source sets for generated code
kotlin.sourceSets["main"].kotlin {
    srcDir("build/generated/ksp/main/kotlin")
}
```

### 2. Configure KSP

```kotlin
ksp {
    arg("projectRootClasspath", "com.yourcompany.yourproject")
    arg("dslBuilderClasspath", "com.yourcompany.yourproject.builders")
    arg("dslMarkerClass", "com.yourcompany.yourproject.YourDSL")
}
```

### 3. Annotate Your Classes

```kotlin
@GeneratedDsl
data class DatabaseConfig(
    val host: String,
    val port: Int,
    val database: String,
    val ssl: Boolean = false
)
```

### 4. Use Your Generated DSL

```kotlin
val config = databaseConfig {
    host = "localhost"
    port = 5432
    database = "myapp"
    ssl = true
}
```

## Annotations

Konstellation provides a comprehensive set of annotations to control DSL generation. Here is a quick overview; for full details and examples see [docs/ANNOTATION_REFERENCE.md](docs/ANNOTATION_REFERENCE.md).

### Class-Level

| Annotation | Purpose |
|---|---|
| `@GeneratedDsl` | Core entry point — marks a data class for DSL builder generation |

### Property-Level

| Annotation | Purpose |
|---|---|
| `@RootDsl` | Generates a top-level DSL entry-point function for the property's type, with optional custom `name` and `alias` |
| `@DefaultValue` | Sets the builder property's initial value instead of `null` |
| `@ListDsl` | Configures list properties: size constraints, `uniqueElements`, `sorted`, accessor control |
| `@MapDsl` | Configures map properties: size constraints, accessor control |
| `@DslDescription` | Adds KDoc comments to generated builder properties and accessors |
| `@DslAlias` | Creates alternative accessor function names for a property |
| `@DeprecatedDsl` | Marks generated accessors as `@Deprecated` with optional `replaceWith` |
| `@ValidateDsl` | Generates a `require()` check in the `build()` method |
| `@TransientDsl` | Excludes a property from the generated builder entirely |
| `@DslProperty` | Controls vararg/provider accessor generation (superseded by `@ListDsl`/`@MapDsl`) |
| `@PublicDslProperty` | Controls accessor visibility for public properties |
| `@PrivateDslProperty` | Controls accessor visibility for private properties |
| `@SingleEntryTransformDsl` | Generates a convenience transform function |

## Real-World Example

```kotlin
@GeneratedDsl
data class ServiceConfig(
    val name: String,

    @DslDescription("Maximum warp speed")
    val maxWarpSpeed: Float? = null,

    @DslAlias(names = ["active"])
    val activated: Boolean? = null,

    @DeprecatedDsl(message = "Use 'activated' instead", replaceWith = "activated")
    val docked: Boolean? = null,

    @ValidateDsl(expression = "it?.let { v -> v > 0 } ?: true", message = "must be positive")
    val capacity: Int? = null,

    @ListDsl(minSize = 1, maxSize = 10, uniqueElements = true, sorted = true)
    val tags: List<String>,

    @MapDsl(minSize = 1, maxSize = 5)
    val metadata: Map<String, String>,

    @DefaultValue("DEFAULT")
    val region: String = "DEFAULT",

    @TransientDsl(reason = "Internal tracking only")
    val internalId: String? = null,

    val nested: DatabaseConfig? = null
)

@GeneratedDsl
data class DatabaseConfig(
    val host: String,
    @DefaultValue("5432")
    val port: Int = 5432,
    val database: String
)
```

Generated usage:

```kotlin
val service = serviceConfig {
    name = "data-processor"
    maxWarpSpeed = 9.5f           // KDoc: "Maximum warp speed"
    activated = true
    active(true)                   // alias for 'activated'
    capacity = 100                 // validated: must be positive
    tags("alpha", "beta")          // vararg; distinct + sorted + min 1, max 10
    metadata("key" to "value")     // vararg; min 1, max 5

    nested {                       // nested DSL block
        host = "localhost"
        database = "myapp"
    }
}
```

## Configuration Options

| KSP Argument | Description | Example |
|---|---|---|
| `projectRootClasspath` | Root package for your project | `com.yourcompany.project` |
| `dslBuilderClasspath` | Package where builders are generated | `com.yourcompany.project.dsl` |
| `dslMarkerClass` | Custom DSL marker annotation class | `com.yourcompany.project.MyDSL` |
| `enableDebugLogging` | Enable debug logging during generation | `true` |

## Development & Debugging

Run with debug logging: `./gradlew clean build -Ddebug=true`

```
konstellation DEBUG [····DSL_BUILDER] *>> +++ DOMAIN: DatabaseConfig +++
konstellation DEBUG [····DSL_BUILDER] *>>   |__ package: com.yourcompany.project
konstellation DEBUG [····DSL_BUILDER] *>>   |__ type: DatabaseConfig
konstellation DEBUG [····DSL_BUILDER] *>>   |__ builder: DatabaseConfigBuilder
konstellation DEBUG [····DSL_BUILDER] *>>   |__ Properties added
konstellation DEBUG [····DSL_BUILDER] *>>       |__ host
konstellation DEBUG [····DSL_BUILDER] *>>       |   |__ type: kotlin.String
```

Or configure in `build.gradle.kts`:

```kotlin
ksp {
    arg("enableDebugLogging", "true")
}
```

## Project Structure

```
konstellation/
├── meta-dsl/          # Annotations for consumers
├── dsl/               # KSP processor implementation
├── core-test/         # Shared testing framework (Geordi)
├── generate-test/     # Integration tests with generated DSL
└── docs/              # Additional documentation
```

## Documentation

- [Annotation Reference](docs/ANNOTATION_REFERENCE.md) - Complete guide to all annotations with examples
- [Implementation Guide](docs/IMPLEMENTATION_GUIDE.md) - KSP processor implementation details

## Requirements

- Kotlin 2.1+
- KSP 2.1+
- Gradle 8.0+
- JDK 21+

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

## Roadmap

- [ ] Change `@RootDsl` target from `PROPERTY` to `CLASS` in meta-dsl
- [ ] Support for generic types
- [ ] Fix logging issues

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Support

- [Issue Tracker](https://github.com/khorum-oss/konstellation-dsl/issues)

---

*Built by the Khorum OSS team*
