# Plan: DSL Module Test Coverage (41% -> ~100%)

## Progress
- **Starting coverage:** 41.1% class (30/73), ~30% line
- **Previous coverage (v1.0.1):** 91.3% class, 83.3% line, 51.9% branch, ~67.6% SonarQube estimate
- **Current coverage (v1.0.2):** 95%+ class, 90%+ line, 75%+ branch (target)
- **Tests:** 280+ test methods across 30+ test files
- **Completed:** Waves 1-6 + branch coverage improvements + AnnotationLookup refactor
- **Remaining:** Final coverage push for edge cases

## Context
DSL module coverage started at 41.1% (30/73 classes). Goal is to get as close to 100% as practical. The remaining gap accounts for KSP entry points and trivial one-liners excluded from coverage.

## v1.0.2 Migration Plan

### Annotation Refactoring
The meta-dsl 1.0.2 update requires migrating annotation lookup code to use the new `AnnotationLookup` utility:

1. **Replace manual annotation filtering** - All occurrences of `annotations.filter { it.shortName.asString() == X::class.simpleName }` should use `AnnotationLookup.findAnnotation()`
2. **Replace manual argument extraction** - All `annotation.arguments.firstOrNull { it.name?.asString() == X::prop.name }` should use `AnnotationLookup.findArgument()`
3. **Update DefaultPropertySchemaFactoryAdapter** - Remove `@ExcludeFromCoverage`, use `AnnotationLookup` for all annotation queries
4. **Update KSClassDeclarationExt** - Simplify extension functions using `AnnotationLookup`
5. **Update DslGenerator** - Simplify `isRootDsl()` and `isDebug()` using `AnnotationLookup`
6. **Update GroupGenerator** - Simplify `isGroup()` using `AnnotationLookup`

### New Tests Required
- `AnnotationLookupTest` - Full coverage of the new utility
- Expanded `DefaultPropertySchemaFactoryAdapterTest` - Now that `@ExcludeFromCoverage` is removed
- Expanded `DefaultBuilderGeneratorTest` - Group/MapGroup type alias generation branches
- Expanded `ParameterFactoryTest` - All property schema determination paths

## Exclusions from Coverage (DONE)
Annotate these with `@ExcludeFromCoverage` (too trivial or require KSP compilation infrastructure):
- `DslProcessor` (5 lines) - KSP entry point, delegates to DefaultDslGenerator
- `DslProvider` (2 lines) - KSP SymbolProcessorProvider factory
- `KonstellationException` (1 line) - simple exception subclass
- `RequiredDataException` (1 line) - simple exception subclass
- `PicardDSLMarker` - annotation class, no logic

## Shared Test Utility (create first)
**`dsl/src/test/kotlin/.../dsl/testutil/KspMockFactory.kt`**
- Reusable MockK helpers for KSP types: `mockKSClassDeclaration(name, pkg, annotations)`, `mockKSPropertyDeclaration(name, type, nullable)`, `mockKSAnnotation(shortName, args)`, `mockKSValueArgument(name, value)`
- Avoids duplicating 20+ lines of mock setup in every generator test

## Wave 1: Domain + Utility (DONE)

### 1. `domain/BuilderConfigTest.kt`
- `BuilderConfig(map, logger)` - Target: **0% -> 100%**
- Test construction with all keys present
- Test `isIgnored` defaults false, test `isIgnored=true`
- Test `RequiredDataException` when `projectRootClasspath` missing
- Test `RequiredDataException` when `dslBuilderClasspath` missing
- Test `rootDslFileClasspath()` returns override vs fallback
- Test `printDebug()` calls logger

### 2. `domain/DefaultPropertyValueTest.kt`
- `DefaultPropertyValue` - Target: **0% -> 100%**
- Test `importString()` concatenation
- Test data class equality

### 3. `domain/DefaultDomainPropertyTest.kt`
- `DefaultDomainProperty` - Target: **0% -> 100%**
- Test `simpleName()`, `continueBranch()` true/false, `singleEntryTransformString()` with/without match
- Requires MockK for `KSPropertyDeclaration`

### 4. `utils/ColorsTest.kt`
- `Colors` (internal) - Target: **0% -> 100%**
- Test each color function wraps with correct ANSI codes

### 5. `utils/LoggerTest.kt`
- `Logger` - Target: **16% -> ~90%**
- Test `enableDebug()`/`disableDebug()` toggle
- Test `debug()` suppressed when disabled, prints when enabled
- Test `info()` always prints
- Test `warn()` suppressed when disabled
- Test `error()` always prints
- Test `tierPrefix()` for tiers 0, 1, 2
- Test `infoMultiline()` with single and multi-line
- Test `formattedName` truncation for long names

### 6. `utils/VLoggableTest.kt`
- `VLoggable` + companion - Target: **0% -> 100%**
- Test `logger` property caches by `logId()`
- Test `setGlobalDebug(true/false)` toggles all cached loggers
- Test `resetGlobalDebug()`

## Wave 2: Builder Classes (DONE)

### 7. `builder/AnnotationDecoratorTest.kt`
- Target: **0% -> 100%**
- Test `createDslMarkerIfAvailable(null)` returns null
- Test with valid classpath returns ClassName

### 8. `builder/AnnotationGroupTest.kt`
- Target: **0% -> 100%**
- Test `annotation(pkg, name)`, `annotation(className)`, `annotation(provider)` null/non-null

### 9. `builder/KPFileSpecBuilderTest.kt`
- Target: **0% -> 100%**
- Test `build()` happy path, null className throws
- Test `addImport` variants, `addImportIf` true/false
- Test `types()`, `typeAliases()`, `functions()` in output

### 10. `builder/KPTypeSpecBuilderTest.kt`
- Target: **0% -> 100%** (includes `KPTypeSpecBuilder.Group`)
- Test `build()` minimal, null name throws
- Test `superInterface()`, `annotations()`, `typeVariables()`, `properties()`, `functions()`, `nested()`

### 11. `builder/KPTypeAliasSpecBuilderTest.kt`
- Target: **0% -> 100%**
- Test `build()` with name and type
- Test `typeVariables()` adds type params

### 12. Expand `builder/KotlinPoetBuilderTest.kt` (new file)
- Target: **65% -> ~95%**
- Test `kotlinPoet {}` returns result
- Test `kpMapOf`, `kpMutableMapOf`, `kpListOf`, `kpMutableListOf` with nullable flag
- Test `nestedClass()` extension

### 13. `builder/KPParameterSpecGroupTest.kt`
- Target `KPParameterSpecBuilder.Group`: **0% -> 100%**
- Test `param {}` builds ParameterSpec
- Test `varargParam {}` adds VARARG modifier

## Wave 3: KSP Utility Extension (DONE)
### 14. `utils/KSClassDeclarationExtTest.kt`
- Target: **0% -> 100%**
- Test `isGroupDsl()` true/false/null
- Test `mapGroupType()` returns enum/null
- Requires MockK for `KSClassDeclaration`, `KSAnnotation`, `KSValueArgument`

## Wave 4: Process Layer (PARTIAL — DomainConfig done, PropertySchemaFactory expanded)

### 15. `domain/DomainConfigTest.kt`
- Target: **0% -> 100%**
- Test derived properties (`packageName`, `typeName`, `builderName`, etc.)
- Requires MockK for `KSClassDeclaration`, `KSName`

### 16. `process/propSchema/DefaultPropertySchemaFactoryAdapterTest.kt`
- Target: **0% -> ~85%**
- Test `propName`, `actualPropTypeName`, `hasNullableAssignment`, `hasGeneratedDslAnnotation`
- Test `withVararg`/`withProvider` defaults
- Test `isGroupElement`, `mapDetails()` caching
- Heavy MockK for KSP type chains

### 17. `process/propSchema/DefaultPropertySchemaServiceTest.kt`
- Target: **0% -> ~90%**
- Test `getParamsFromDomain` with 0 and N properties
- Test `@DefaultValue` extraction (String vs non-String template)

### 18. Expand `process/ParameterFactoryTest.kt`
- Target: **36% -> ~85%**
- Add tests for: BOOLEAN type, MAP type, MAP with MapGroupType, LIST without group, `hasSingleEntryTransform` true, `hasGeneratedDslAnnotation` true producing BuilderPropSchema, fallback to DefaultPropSchema

### 19. Expand `props/DefaultParamTest.kt`
- Target: **60% -> ~95%**
- Add nullable assignment test, `defaultValue` set test, `verifyNotNull` path

### 20. Expand `props/SingleTransformParamTest.kt`
- Target: **73% -> ~95%**
- Add custom `transformTemplate` test, non-nullable assignment path

## Wave 5: Generator Classes (PARTIAL — DslGenerator, GroupGenerator, RootFunctionGenerator, RootDslAccessorGenerator done)

### 21. `process/generator/GroupGeneratorTest.kt`
- Targets `GroupGenerator`, `ListGroupGenerator`, `MapGroupGenerator`: **0% -> ~90%**
- Test list group generation when `isGroup=true/false`
- Test map group generation with active/inactive `MapGroupType`
- Verify generated nested type structure

### 22. `process/root/RootFunctionGeneratorTest.kt`
- Target: **0% -> 100%**
- Test generated FunSpec has correct name, lambda param, return type, body

### 23. `process/root/RootDslAccessorGeneratorTest.kt`
- Target: **0% -> ~90%**
- Test file generation with N domain classes, verify `writeTo` called

### 24. `process/generator/DefaultDslGeneratorTest.kt`
- Target: **0% -> ~85%**
- Test `isIgnored=true` returns early
- Test processes `@GeneratedDsl` classes
- Test collects `@SingleEntryTransformDsl` map
- Test calls rootDslAccessorGenerator when root classes exist

### 25. `process/generator/DefaultBuilderGeneratorTest.kt`
- Target: **0% -> ~75%**
- Test generates builder interface with correct name/superinterface
- Test includes property specs and accessor functions from schemas
- Test type alias generation (base, Group, MapGroup variants)
- Test conditional imports (`vRequireNotNull`, `vRequireCollectionNotEmpty`, `vRequireMapNotEmpty`)
- Test `debugLog` wrapping

## Verification
After each wave:
```bash
./gradlew :dsl:test :dsl:koverHtmlReport
open dsl/build/reports/kover/html/index.html
```

Final check:
```bash
./gradlew :dsl:koverXmlReport
# Verify coverage >= 88%
```

## File Summary
- **New test files:** ~18
- **Expanded existing tests:** 4
- **New utility file:** 1 (KspMockFactory)
- **Exclusion annotations:** 5 classes
- **Target coverage:** ~88-92%
