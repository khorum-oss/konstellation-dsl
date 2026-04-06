package org.khorum.oss.konstellation.dsl.props

import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.domain.PropertyAnnotationMetadata
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

    @Test
    fun `accessors - combines property docString with builder kdoc`() = test {
        given {
            val metadata = PropertyAnnotationMetadata(docString = "The engine configuration")
            val param = BuilderPropSchema(
                "engine", typeName, buildClassName, true,
                kdoc = "Available builder functions:\n* [TestBuilder.power]",
                annotationMetadata = metadata
            )

            expect { true }
            whenever {
                val accessor = param.accessors().first().toString()
                accessor.contains("The engine configuration") && accessor.contains("Available builder functions")
            }
        }
    }

    @Test
    fun `accessors - DslDescription takes precedence over docString in combined kdoc`() = test {
        given {
            val metadata = PropertyAnnotationMetadata(
                description = "From annotation",
                docString = "From source"
            )
            val param = BuilderPropSchema(
                "engine", typeName, buildClassName, true,
                kdoc = "Builder list",
                annotationMetadata = metadata
            )

            expect { true }
            whenever {
                val accessor = param.accessors().first().toString()
                accessor.contains("From annotation") && !accessor.contains("From source")
            }
        }
    }

    @Test
    fun `accessors - propDesc only when no builder kdoc`() = test {
        given {
            val metadata = PropertyAnnotationMetadata(docString = "The engine config")
            val param = BuilderPropSchema(
                "engine", typeName, buildClassName, true,
                kdoc = null,
                annotationMetadata = metadata
            )

            expect { true }
            whenever {
                val accessor = param.accessors().first().toString()
                accessor.contains("The engine config") && !accessor.contains("Available builder")
            }
        }
    }

    @Test
    fun `accessors - no KDoc when neither propDesc nor builder kdoc`() = test {
        given {
            val param = BuilderPropSchema(
                "engine", typeName, buildClassName, true,
                kdoc = null
            )

            expect { false }
            whenever { param.accessors().first().toString().contains("/**") }
        }
    }

    class TestResponse
    class TestBuilder
}
