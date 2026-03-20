# Konstellation DSL v1.0.2 - Implementation Details

## Overview

Version 1.0.2 updates the `konstellation-meta-dsl` dependency from 1.0.1 to 1.0.2 and introduces
refactoring improvements to enhance code maintainability, testability, and coverage.

## Changes

### 1. Meta-DSL Dependency Bump (1.0.1 -> 1.0.2)

- Updated `meta-dsl` version in `gradle/libs.versions.toml` to `1.0.2`
- Updated `VERSION` file to `1.0.2`
- Annotations consumed from meta-dsl remain compatible:
  - `@GeneratedDsl` - marks domain classes for DSL generation (isRoot, debug, withListGroup, withMapGroup)
  - `@SingleEntryTransformDsl` - marks classes for single-entry transformation (inputType, transformTemplate)
  - `@DefaultValue` - specifies default values for properties (value, packageName, className)
  - `@DslProperty` - controls accessor generation (withVararg, withProvider)
  - `MapGroupType` - enum for map group variants (SINGLE, ACTIVE_TYPES)

### 2. Annotation Lookup Refactoring

The codebase had repeated patterns for KSP annotation lookup scattered across multiple files:
- `DslGenerator.kt` (isRootDsl, isDebug, getGeneratedDslAnnotation)
- `DefaultPropertySchemaFactoryAdapter.kt` (dslPropertyAnnotation, singleEntryTransformAnnotation, hasGeneratedDslAnnotation, isGroupElement, mapGroupType)
- `KSClassDeclarationExt.kt` (isGroupDsl, mapGroupType)
- `GroupGenerator.kt` (isGroup)
- `PropertySchemaService.kt` (extractDefaultPropertyValue)

#### Refactored: `AnnotationLookup` utility

Extracted a centralized `AnnotationLookup` utility to reduce duplicated annotation-querying boilerplate:

```kotlin
object AnnotationLookup {
    fun findAnnotation(annotations: Sequence<KSAnnotation>, annotationClass: KClass<*>): KSAnnotation?
    fun <T> findArgument(annotation: KSAnnotation?, argumentName: String): T?
    fun hasAnnotation(annotations: Sequence<KSAnnotation>, annotationClass: KClass<*>): Boolean
}
```

This consolidates the pattern of:
1. Filtering annotations by `shortName`
2. Finding arguments by name
3. Casting argument values

Used across `DefaultPropertySchemaFactoryAdapter`, `KSClassDeclarationExt`, `DslGenerator`, and `GroupGenerator`.

### 3. `DefaultPropertySchemaFactoryAdapter` Refactoring

- Removed `@ExcludeFromCoverage` annotation to enable proper test coverage
- Extracted annotation lookup logic to use `AnnotationLookup` utility
- Made the class more testable by reducing deeply-chained KSP calls

### 4. Test Coverage Improvements

Expanded test coverage targeting remaining gaps identified in the planning doc:

#### New/Expanded Tests:
- `DefaultPropertySchemaFactoryAdapterTest` - Expanded branch coverage for annotation lookups, map details caching, and edge cases
- `DefaultBuilderGeneratorTest` - Expanded coverage for builder generation with group/map group type aliases, conditional imports, and debug logging
- `DefaultPropertySchemaServiceTest` - Expanded coverage for `@DefaultValue` extraction edge cases
- `ParameterFactoryTest` - Expanded coverage for all property schema determination branches
- `DslPropSchemaBranchTest` - Expanded branch coverage for `propertyValueReturn()` across schema types
- `GroupGeneratorTest` - Expanded coverage for ListGroupGenerator and MapGroupGenerator edge cases
- `DefaultDslGeneratorTest` - Expanded coverage for root class filtering and debug mode

#### Coverage Targets:
- **Previous:** 91.3% class, 83.3% line, 51.9% branch
- **Target:** 95%+ class, 90%+ line, 75%+ branch

### 5. Code Quality

- Consistent use of `AnnotationLookup` eliminates repeated annotation-querying code
- Reduced cyclomatic complexity in `DefaultPropertySchemaFactoryAdapter`
- Better separation of concerns between annotation parsing and schema creation

## Migration Notes

### For consumers of the DSL processor:
- No breaking changes to the annotation API
- The `@GeneratedDsl`, `@SingleEntryTransformDsl`, `@DefaultValue`, and `@DslProperty` annotations remain unchanged
- Generated DSL code output is identical

### For developers of the DSL processor:
- `AnnotationLookup` utility is the preferred way to query KSP annotations
- `DefaultPropertySchemaFactoryAdapter` is no longer excluded from coverage; add tests for new code paths
- Use `AnnotationLookup.findAnnotation()` instead of manual `annotations.filter { ... }.firstOrNull { ... }` chains

## Files Modified

| File | Change Type |
|------|-------------|
| `VERSION` | Version bump to 1.0.2 |
| `gradle/libs.versions.toml` | meta-dsl version bump to 1.0.2 |
| `dsl/src/main/kotlin/.../utils/AnnotationLookup.kt` | New utility |
| `dsl/src/main/kotlin/.../utils/KSClassDeclarationExt.kt` | Refactored to use AnnotationLookup |
| `dsl/src/main/kotlin/.../process/generator/DslGenerator.kt` | Refactored to use AnnotationLookup |
| `dsl/src/main/kotlin/.../process/generator/GroupGenerator.kt` | Refactored to use AnnotationLookup |
| `dsl/src/main/kotlin/.../process/propSchema/DefaultPropertySchemaFactoryAdapter.kt` | Refactored, coverage enabled |
| `dsl/src/test/kotlin/.../utils/AnnotationLookupTest.kt` | New test |
| Various test files | Expanded coverage |
