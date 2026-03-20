# Konstellation

A robust Kotlin Symbol Processing (KSP) library for automatically generating type-safe Kotlin DSLs from annotated data classes.

> ⚠️ **This project is currently in active development and APIs may change.**

## Overview

Konstellation eliminates the boilerplate of creating Kotlin DSLs by automatically generating builder patterns, DSL markers, and type-safe configuration interfaces from your existing data classes. Simply annotate your classes and let KSP handle the rest.

## Features

- 🚀 **Zero Runtime Overhead** - Pure compile-time code generation
- 🛡️ **Type Safety** - Generated DSLs maintain full type safety
- 🎯 **Minimal Setup** - Just add annotations to existing classes
- 📊 **Rich Logging** - Tiered debug output for development insights
- 🔧 **Flexible Configuration** - Customizable through KSP arguments

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
@GenerateDSL
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

## Real-World Example

Here's a more complex example showing nested configuration:

```kotlin
@GenerateDSL
data class BloomBuildPlannerQueue(
    val maxQueuedTasksPerTenant: Int,
    val storeTasksOnDisk: Boolean,
    val tasksDiskDirectory: String,
    val cleanTasksDirectory: Boolean
)

@GenerateDSL
data class ServiceConfig(
    val name: String,
    val queue: BloomBuildPlannerQueue,
    val timeout: Duration = Duration.ofSeconds(30)
)
```

Generated usage:

```kotlin
val service = serviceConfig {
    name = "data-processor"
    queue {
        maxQueuedTasksPerTenant = 100
        storeTasksOnDisk = true
        tasksDiskDirectory = "/tmp/tasks"
        cleanTasksDirectory = true
    }
    timeout = Duration.ofMinutes(5)
}
```

## Configuration Options

| KSP Argument | Description | Example |
|--------------|-------------|---------|
| `projectRootClasspath` | Root package for your project | `com.yourcompany.project` |
| `dslBuilderClasspath` | Package where builders are generated | `com.yourcompany.project.dsl` |
| `dslMarkerClass` | Custom DSL marker annotation class | `com.yourcompany.project.MyDSL` |

## Development & Debugging

In order to run debug, you can use `./gradlew clean build -Ddebug=true`

Konstellation includes sophisticated logging to help you understand the generation process. 

```
konstellation DEBUG [····DSL_BUILDER] *>> +++ DOMAIN: BloomBuildPlannerQueue +++
konstellation DEBUG [····DSL_BUILDER] *>>   |__ package: io.violabs.konstellation.example
konstellation DEBUG [····DSL_BUILDER] *>>   |__ type: BloomBuildPlannerQueue
konstellation DEBUG [····DSL_BUILDER] *>>   |__ builder: BloomBuildPlannerQueueBuilder
konstellation DEBUG [····DSL_BUILDER] *>>   |__ Properties added
konstellation DEBUG [····DSL_BUILDER] *>>       |__ maxQueuedTasksPerTenant
konstellation DEBUG [····DSL_BUILDER] *>>       |   |__ type: kotlin.Int
```

To enable debug logging in your KSP processor, add:

```kotlin
ksp {
    arg("enableDebugLogging", "true")
}
```

## Project Structure

```
konstellation/
├── meta-dsl/          # Annotations for consumers
├── dsl/              # KSP processor implementation
├── examples/         # Usage examples
└── docs/            # Additional documentation
```

## Requirements

- Kotlin 1.9.0+
- KSP 1.9.0-1.0.13+
- Gradle 7.0+

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

## Roadmap

- [ ] Support for generic types
- [ ] Validation DSL generation
- [ ] Fix logging issues

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Support

- 🐛 [Issue Tracker](https://github.com/khorum-oss/konstellation-dsl/issues)

---

*Built with ❤️ by the Khorum OSS team*
