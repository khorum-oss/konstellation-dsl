package org.khorum.oss.konstellation.generatetest

import org.khorum.oss.konstellation.crossmoduletest.CrossModuleNested
import org.khorum.oss.konstellation.metaDsl.annotation.GeneratedDsl

/**
 * Regression scenario for cross-module nested DSL detection.
 *
 * [CrossModuleNested] lives in the sibling `cross-module-test` Gradle module and is
 * annotated with `@GeneratedDsl` at source. Because `@GeneratedDsl` has `SOURCE`
 * retention, the annotation is stripped from the compiled `.class` file, so KSP in
 * this module cannot see it on the imported type. Without a fallback the `nested`
 * property would fall through to `DefaultPropSchema` and no builder accessor would
 * be generated, which matches a user-reported bug where nested DSL builder methods
 * silently disappear when the nested type lives in another module.
 *
 * `DefaultPropertySchemaFactoryAdapter.isGeneratedDslType` falls back to looking up
 * `${qualifiedName}DslBuilder` on the resolver's classpath via
 * [org.khorum.oss.konstellation.dsl.process.ResolverContext], so the generator
 * should still produce `fun nested(block: CrossModuleNestedDslBuilder.() -> Unit)`
 * on the parent builder.
 */
@GeneratedDsl
data class CrossModuleParent(
    val nested: CrossModuleNested? = null,
)

/**
 * Regression scenario for cross-module list/map DSL detection. The list/map
 * element type is defined in another Gradle module, so the same
 * source-retention visibility issue applies. The generator must still detect the
 * nested DSL type and emit group/map-group accessors (`fun nestedList(block)` and
 * `fun nestedMap(key, block)`) rather than falling back to plain `List`/`Map`
 * schemas.
 */
@GeneratedDsl
data class CrossModuleCollectionParent(
    val nestedList: List<CrossModuleNested>? = null,
    val nestedMap: Map<String, CrossModuleNested>? = null,
)
