package org.khorum.oss.konstellation.dsl.schema

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import org.khorum.oss.konstellation.dsl.builder.kotlinPoet
import org.khorum.oss.konstellation.dsl.domain.PropertyAnnotationMetadata
import org.khorum.oss.konstellation.dsl.process.propSchema.PropertySchemaFactoryAdapter

/**
 * Schema for a property that applies a transformation to the input value in the DSL.
 * This is typically used for properties that require some form of processing or conversion.
 */
class SingleTransformPropSchema(
    override val propName: String,
    val inputTypeName: TypeName,
    actualPropTypeName: TypeName,
    val transformTemplate: String? = null,
    override val nullableAssignment: Boolean = true,
    override val annotationMetadata: PropertyAnnotationMetadata = PropertyAnnotationMetadata()
) : DslPropSchema {
    override val propTypeName: TypeName = actualPropTypeName.copy(nullable = nullableAssignment)

    constructor(adapter: PropertySchemaFactoryAdapter) : this(
        propName = adapter.propName,
        transformTemplate = adapter.transformTemplate,
        actualPropTypeName = adapter.actualPropTypeName,
        inputTypeName = adapter.transformType!!,
        nullableAssignment = adapter.hasNullableAssignment,
        annotationMetadata = adapter.annotationMetadata
    )

    override fun accessors(): List<FunSpec> = kotlinPoet {
        functions {
            add {
                funName = functionName
                val param = param {
                    name = propName
                    type(inputTypeName)
                }
                statements {
                    val finalTransformTemplate = transformTemplate ?: "${propTypeName.copy(nullable = false)}(%N)"
                    addLine("this.%N = $finalTransformTemplate", propName, param)
                }
            }
        }
    }
}
