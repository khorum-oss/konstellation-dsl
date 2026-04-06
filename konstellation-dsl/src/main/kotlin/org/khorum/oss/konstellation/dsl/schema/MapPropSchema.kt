package org.khorum.oss.konstellation.dsl.schema

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import org.khorum.oss.konstellation.dsl.builder.kotlinPoet
import org.khorum.oss.konstellation.dsl.builder.kpMapOf
import org.khorum.oss.konstellation.dsl.builder.kpMutableMapOf
import org.khorum.oss.konstellation.dsl.domain.DefaultPropertyValue
import org.khorum.oss.konstellation.dsl.domain.PropertyAnnotationMetadata

/**
 * Schema for a property that represents a map of items in the DSL.
 *
 * @param propName The name of the property.
 * @param mapKeyType The type of keys in the map.
 * @param mapValueType The type of values in the map.
 * @param nullableAssignment Whether the property can be null.
 * @param withVararg Whether to generate a vararg function (default: true).
 * @param withProvider Whether to generate a provider function (default: true).
 */
@Suppress("LongParameterList")
class MapPropSchema(
    override val propName: String,
    val mapKeyType: TypeName = STRING,
    val mapValueType: TypeName = STRING,
    override val nullableAssignment: Boolean = true,
    val withVararg: Boolean = true,
    val withProvider: Boolean = true,
    override val defaultValue: DefaultPropertyValue? = null,
    override val annotationMetadata: PropertyAnnotationMetadata = PropertyAnnotationMetadata()
) : DslPropSchema {
    override val propTypeName: TypeName = kpMapOf(mapKeyType, mapValueType, nullable = true)
    override val iterableType: DslPropSchema.IterableType = DslPropSchema.IterableType.MAP

    override val verifyNotNull: Boolean = defaultValue != null
    override val verifyNotEmpty: Boolean = defaultValue == null

    override fun toPropertySpec(): PropertySpec = kotlinPoet {
        property {
            protected()
            variable()
            name = propName
            type(propTypeName)

            if (defaultValue != null) {
                initializer = defaultValue.codeBlock
            } else {
                initNullValue()
            }
        }
    }

    override fun accessors(): List<FunSpec> = kotlinPoet {
        val pairType = pairTypeOf(mapKeyType, mapValueType, nullable = false)
        val desc = annotationMetadata.effectiveDescription

        functions {
            // Vararg function: areaCodes(vararg items: Pair<String, String>)
            if (withVararg) {
                add {
                    funName = functionName
                    desc?.let { kdoc(it) }
                    varargParam {
                        type(pairType)
                    }
                    statements {
                        addLine("this.%N = items.toMap()", propName)
                    }
                }
            }

            // Provider function: areaCodes(block: MutableMap<String, String>.() -> Unit)
            if (withProvider) {
                add {
                    funName = functionName
                    desc?.let { kdoc(it) }
                    param {
                        name = "block"
                        lambdaType {
                            receiver = kpMutableMapOf(mapKeyType, mapValueType, nullable = false)
                        }
                    }
                    statements {
                        addLine(
                            "this.%N = mutableMapOf<%T, %T>().apply(block).toMap()",
                            propName,
                            mapKeyType,
                            mapValueType
                        )
                    }
                }
            }
        }
    }
}
