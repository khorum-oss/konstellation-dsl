package org.khorum.oss.konstellation.dsl.schema

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import org.khorum.oss.konstellation.dsl.builder.kotlinPoet
import org.khorum.oss.konstellation.dsl.domain.DefaultPropertyValue
import org.khorum.oss.konstellation.dsl.domain.PropertyAnnotationMetadata

/**
 * Schema for a boolean property in the DSL.
 */
class BooleanPropSchema(
    override val propName: String,
    override val nullableAssignment: Boolean = true,
    override val defaultValue: DefaultPropertyValue? = null,
    override val annotationMetadata: PropertyAnnotationMetadata = PropertyAnnotationMetadata()
) : DslPropSchema {
    override val propTypeName: TypeName = BOOLEAN.copy(nullable = nullableAssignment) // Correctly use constructor arg

    override fun accessors(): List<FunSpec> = kotlinPoet {
        functions {
            val config = defaultValue?.booleanAccessorConfig

            if (config == null) {
                // Backward compatibility: single function with default from annotation
                add {
                    funName = propName
                    val param = param {
                        booleanType()
                        defaultValue(defaultValue?.rawValue?.toBoolean() ?: true)
                    }
                    statements {
                        addLine("this.%N = %N", propName, param)
                    }
                }
                return@functions
            }

            // Valid function: sets this.prop = check
            val validName = config.resolveValidFunctionName(propName)
            if (validName != null) {
                add {
                    funName = validName
                    val param = param {
                        booleanType()
                        defaultValue(true)
                    }
                    statements {
                        addLine("this.%N = %N", propName, param)
                    }
                }
            }

            // Negation function: sets this.prop = !check
            val negationName = config.resolveNegationFunctionName(propName)
            if (negationName != null) {
                add {
                    funName = negationName
                    val param = param {
                        booleanType()
                        defaultValue(true)
                    }
                    statements {
                        addLine("this.%N = !%N", propName, param)
                    }
                }
            }
        }
    }
}
