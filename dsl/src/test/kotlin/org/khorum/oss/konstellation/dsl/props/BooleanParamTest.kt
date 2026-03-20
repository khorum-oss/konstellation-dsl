package org.khorum.oss.konstellation.dsl.props

import com.squareup.kotlinpoet.CodeBlock
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.schema.BooleanPropSchema
import org.junit.jupiter.api.Test
import org.khorum.oss.konstellation.dsl.domain.DefaultPropertyValue

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
            val param = BooleanPropSchema("enabled", defaultValue = DefaultPropertyValue(
                rawValue = "false",
                codeBlock = CodeBlock.of("%L", false),
                packageName = "kotlin",
                className = "Boolean"
            )
            )
            expect { true }
            whenever { param.accessors().first().toString().contains("false") }
        }
    }

    @Test
    fun `propertyValueReturn - nullable returns propName`() = test {
        given {
            val param = BooleanPropSchema("active", nullableAssignment = true)
            expect { "active" }
            whenever { param.propertyValueReturn() }
        }
    }

    @Test
    fun `propertyValueReturn - non-nullable returns vRequireNotNull`() = test {
        given {
            val param = BooleanPropSchema("active", nullableAssignment = false)
            expect { "vRequireNotNull(::active)" }
            whenever { param.propertyValueReturn() }
        }
    }

    @Test
    fun `isCollection returns false`() = test {
        given {
            val param = BooleanPropSchema("active")
            expect { false }
            whenever { param.isCollection() }
        }
    }

    @Test
    fun `isMap returns false`() = test {
        given {
            val param = BooleanPropSchema("active")
            expect { false }
            whenever { param.isMap() }
        }
    }

    @Test
    fun `toPropertySpec with defaultValue codeBlock`() = test {
        given {
            val defaultValue = DefaultPropertyValue(
                rawValue = "true",
                codeBlock = CodeBlock.of("%L", true),
                packageName = "kotlin",
                className = "Boolean"
            )
            val param = BooleanPropSchema("enabled", defaultValue = defaultValue)
            expect { true }
            whenever { param.toPropertySpec().toString().contains("true") }
        }
    }
}
