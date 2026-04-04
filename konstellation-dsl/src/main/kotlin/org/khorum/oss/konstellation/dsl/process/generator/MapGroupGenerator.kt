package org.khorum.oss.konstellation.dsl.process.generator

import org.khorum.oss.konstellation.dsl.builder.AnnotationDecorator
import org.khorum.oss.konstellation.dsl.builder.kpMapOf
import org.khorum.oss.konstellation.dsl.builder.kpMutableMapOf

/**
 * Generator config for a DSL group that represents a map of items.
 */
private val MAP_GROUP_GENERATOR_CONFIG = GroupGenerator.Config(
    namespace = GroupGenerator.Namespace(
        checkName = "isMapGroup", typeName = "MapGroup", typeVariable = "T"
    ),
    annotationName = "GeneratedDsl",
    templates = GroupGenerator.Templates(
        prop = "mutableMapOf()",
        itemsReturn = "return items.toMap()",
        builderAdd = "items[key] = %T().apply(block).build()"
    ),
    propertyTypeAssigner = { typeVar, className ->
        val typeVariable = requireNotNull(typeVar) { "Parameterized Type required for MapGroup" }
        kpMutableMapOf(typeVariable, className, nullable = false)
    },
    builtTypeAssigner = { typeVar, className ->
        val typeVariable = requireNotNull(typeVar) { "Parameterized Type required for MapGroup" }
        kpMapOf(typeVariable, className, nullable = false)
    }
)

/**
 * A generator for a DSL group that represents a map of items.
 * This generator is used to create a mutable map of items in the DSL.
 *
 * @property annotationDecorator An optional decorator for annotations.
 */
class MapGroupGenerator(
    annotationDecorator: AnnotationDecorator = AnnotationDecorator()
) : GroupGenerator(MAP_GROUP_GENERATOR_CONFIG, annotationDecorator) {
    override fun logId(): String? = this::class.simpleName
}
