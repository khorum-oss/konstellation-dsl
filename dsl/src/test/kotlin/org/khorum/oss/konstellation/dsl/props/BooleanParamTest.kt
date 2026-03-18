package org.khorum.oss.konstellation.dsl.props

import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.schema.BooleanPropSchema
import org.junit.jupiter.api.Test

class BooleanParamTest : UnitSim() {

    @Test
    fun `toPropertySpec - happy path - #scenario`() = test {
        given {
            val param = BooleanPropSchema("test")

            expect { "protected var test: kotlin.Boolean? = null" }

            whenever {
                val propSpec = param.toPropertySpec()

                propSpec.toString().trimIndent()
            }
        }
    }

    @Test
    fun `accessors - happy path`() = test {
        given {
            val param = BooleanPropSchema("test", true)

            expect {
                """
                    |public fun test(on: kotlin.Boolean = true) {
                    |  this.test = on
                    |}
                """.trimMargin()
            }

            whenever { param.accessors().first().toString().trimIndent() }
        }
    }

    @Test
    fun `toPropertySpec - non-nullable boolean`() = test {
        given {
            val param = BooleanPropSchema("active", nullableAssignment = false)
            expect { true }
            whenever { param.toPropertySpec().toString().contains("Boolean") }
        }
    }

    @Test
    fun `accessors - with defaultValue false`() = test {
        given {
            val param = BooleanPropSchema("enabled", defaultValue = org.khorum.oss.konstellation.dsl.domain.DefaultPropertyValue(
                rawValue = "false",
                codeBlock = com.squareup.kotlinpoet.CodeBlock.of("%L", false),
                packageName = "kotlin",
                className = "Boolean"
            ))
            expect { true }
            whenever { param.accessors().first().toString().contains("false") }
        }
    }
}
