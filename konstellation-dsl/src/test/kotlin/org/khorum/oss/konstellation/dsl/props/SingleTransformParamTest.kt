package org.khorum.oss.konstellation.dsl.props

import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.schema.SingleTransformPropSchema
import org.junit.jupiter.api.Test

private class TestCase

class SingleTransformParamTest : UnitSim() {
    val propTypeName = TestCase::class.asTypeName()
    val inputTypeName = String::class.asTypeName()
    private val testResponseClassName = TestCase::class.asClassName()

    @Test
    fun `toPropertySpec - happy path`() = test {
        given {
            val param = SingleTransformPropSchema(
                "test",
                inputTypeName,
                propTypeName,
            )

            expect { "protected var test: $testResponseClassName? = null" }

            whenever {
                val propSpec = param.toPropertySpec()

                propSpec.toString().trimIndent()
            }
        }
    }

    @Test
    fun `accessors - happy path`() = test {
        given {
            val param = SingleTransformPropSchema(
                "test",
                inputTypeName,
                propTypeName,
                nullableAssignment = true
            )

            expect {
                """
                    |public fun test(test: kotlin.String) {
                    |  this.test = $testResponseClassName(test)
                    |}
                """.trimMargin()
            }

            whenever { param.accessors().first().toString().trimIndent() }
        }
    }

    @Test
    fun `accessors - with custom transformTemplate`() = test {
        given {
            val param = SingleTransformPropSchema(
                "test",
                inputTypeName,
                propTypeName,
                transformTemplate = "customTransform(%N)",
                nullableAssignment = true
            )

            expect {
                """
                    |public fun test(test: kotlin.String) {
                    |  this.test = customTransform(test)
                    |}
                """.trimMargin()
            }

            whenever { param.accessors().first().toString().trimIndent() }
        }
    }

    @Test
    fun `propertyValueReturn - nullable assignment returns propName`() = test {
        given {
            val param = SingleTransformPropSchema(
                "test",
                inputTypeName,
                propTypeName,
                nullableAssignment = true
            )

            expect { "test" }

            whenever { param.propertyValueReturn() }
        }
    }

    @Test
    fun `propertyValueReturn - non-nullable assignment returns requireNotNull`() = test {
        given {
            val param = SingleTransformPropSchema(
                "test",
                inputTypeName,
                propTypeName,
                nullableAssignment = false
            )

            expect { "DslValidation.requireNotNull(::test)" }

            whenever { param.propertyValueReturn() }
        }
    }

    @Test
    fun `accessors produces exactly one function`() = test {
        given {
            val param = SingleTransformPropSchema(
                "test",
                inputTypeName,
                propTypeName,
                nullableAssignment = true
            )

            expect { 1 }
            whenever { param.accessors().size }
        }
    }

    @Test
    fun `propTypeName copies nullable from nullableAssignment`() = test {
        given {
            val param = SingleTransformPropSchema(
                "test",
                inputTypeName,
                propTypeName,
                nullableAssignment = false
            )

            expect { false }
            whenever { param.propTypeName.isNullable }
        }
    }

    @Test
    fun `toPropertySpec - non-nullable assignment`() = test {
        given {
            val param = SingleTransformPropSchema(
                "test",
                inputTypeName,
                propTypeName,
                nullableAssignment = false
            )

            expect { true }
            whenever {
                val propSpec = param.toPropertySpec()
                propSpec.toString().contains("protected var test")
            }
        }
    }
}
