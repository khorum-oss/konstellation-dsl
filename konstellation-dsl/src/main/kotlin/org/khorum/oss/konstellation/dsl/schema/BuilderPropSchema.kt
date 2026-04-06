package org.khorum.oss.konstellation.dsl.schema

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import org.khorum.oss.konstellation.dsl.builder.kotlinPoet
import org.khorum.oss.konstellation.dsl.domain.PropertyAnnotationMetadata

/**
 * Schema for a property that uses a builder pattern in the DSL.
 */
class BuilderPropSchema(
    override val propName: String,
    originalPropertyType: TypeName,
    private val nestedBuilderClassName: ClassName,
    override val nullableAssignment: Boolean = true,
    kdoc: String? = null,
    override val annotationMetadata: PropertyAnnotationMetadata = PropertyAnnotationMetadata()
) : DslPropSchema {
    override val propTypeName: TypeName = originalPropertyType
    private val _kdoc: String? = kdoc

    override fun accessors(): List<FunSpec> = kotlinPoet {
        functions {
            add {
                funName = functionName
                val propDesc = annotationMetadata.effectiveDescription
                val combinedKdoc = listOfNotNull(propDesc, _kdoc).joinToString("\n\n")
                if (combinedKdoc.isNotBlank()) kdoc(combinedKdoc)
                param {
                    lambdaType {
                        receiver = nestedBuilderClassName
                    }
                }

                statements {
                    addLine("val builder = %T()", nestedBuilderClassName)
                    addLine("builder.block()")
                    addLine("this.%N = builder.build()", propName)
                }
            }
        }
    }
}
