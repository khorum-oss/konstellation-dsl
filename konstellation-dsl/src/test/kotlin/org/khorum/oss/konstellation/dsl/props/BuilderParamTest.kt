package org.khorum.oss.konstellation.dsl.props

import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.schema.BuilderPropSchema
import org.junit.jupiter.api.Test

class BuilderParamTest : UnitSim() {
    val typeName = TestResponse::class.asTypeName()
    val buildClassName = TestBuilder::class.asClassName()
    private val testResponseClassName = TestResponse::class.asClassName()

    @Test
    fun `toPropertySpec - happy path`() = test {
        given {
            val param = BuilderPropSchema("test", typeName, buildClassName)

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
            val param = BuilderPropSchema("test", typeName, buildClassName, true)

            expect {
                """
                    |public fun test(block: org.khorum.oss.konstellation.dsl.props.BuilderParamTest.TestBuilder.() -> kotlin.Unit) {
                    |  val builder = org.khorum.oss.konstellation.dsl.props.BuilderParamTest.TestBuilder()
                    |  builder.block()
                    |  this.test = builder.build()
                    |}
                """.trimMargin()
            }

            whenever { param.accessors().first().toString().trimIndent() }
        }
    }

    @Test
    fun `accessors - with kdoc`() = test {
        given {
            val param = BuilderPropSchema("test", typeName, buildClassName, true, kdoc = "Builder docs")

            expect { true }

            whenever {
                val accessor = param.accessors().first().toString()
                accessor.contains("Builder docs")
            }
        }
    }

    @Test
    fun `accessors - without kdoc`() = test {
        given {
            val param = BuilderPropSchema("test", typeName, buildClassName, true, kdoc = null)

            expect { false }

            whenever {
                val accessor = param.accessors().first().toString()
                accessor.contains("/**")
            }
        }
    }

    @Test
    fun `propertyValueReturn - nullable returns propName`() = test {
        given {
            val param = BuilderPropSchema("test", typeName, buildClassName, nullableAssignment = true)
            expect { "test" }
            whenever { param.propertyValueReturn() }
        }
    }

    @Test
    fun `propertyValueReturn - non-nullable returns requireNotNull`() = test {
        given {
            val param = BuilderPropSchema("test", typeName, buildClassName, nullableAssignment = false)
            expect { "DslValidation.requireNotNull(::test)" }
            whenever { param.propertyValueReturn() }
        }
    }

    class TestResponse
    class TestBuilder
}
