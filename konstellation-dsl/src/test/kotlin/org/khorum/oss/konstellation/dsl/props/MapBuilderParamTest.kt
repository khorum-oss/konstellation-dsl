package org.khorum.oss.konstellation.dsl.props

import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.domain.PropertyAnnotationMetadata
import org.khorum.oss.konstellation.dsl.schema.MapGroupPropSchema
import org.junit.jupiter.api.Test

class MapBuilderParamTest : UnitSim() {

    @Test
    fun `toPropertySpec - happy path`() = test {
        given {
            val param = MapGroupPropSchema("test", STRING, TestObj::class.asTypeName() as TypeName)

            expect { "protected var test: kotlin.collections.Map<kotlin.String, " +
                "org.khorum.oss.konstellation.dsl.props.MapBuilderParamTest.TestObj>? = null" }

            whenever {
                val propSpec = param.toPropertySpec()

                propSpec.toString().trimIndent()
            }
        }
    }

    @Test
    fun `accessors - happy path`() = test {
        given {
            val param = MapGroupPropSchema(
                "test",
                STRING, TestObj::class.asTypeName() as TypeName,
                false
            )

            expect {
                """
                    |public fun test(block: org.khorum.oss.konstellation.dsl.props.TestObjDslBuilder.MapGroup<kotlin.String>.() -> kotlin.Unit) {
                    |  this.test = org.khorum.oss.konstellation.dsl.props.TestObjDslBuilder.MapGroup<kotlin.String>().apply(block).items().toMap()
                    |}
                """.trimMargin()
            }

            whenever { param.accessors().first().toString().trimIndent() }
        }
    }

    @Test
    fun `isMap returns true`() = test {
        given {
            val param = MapGroupPropSchema("test", STRING, TestObj::class.asTypeName() as TypeName)
            expect { true }
            whenever { param.isMap() }
        }
    }

    @Test
    fun `isCollection returns false`() = test {
        given {
            val param = MapGroupPropSchema("test", STRING, TestObj::class.asTypeName() as TypeName)
            expect { false }
            whenever { param.isCollection() }
        }
    }

    @Test
    fun `verifyNotEmpty is true`() = test {
        given {
            val param = MapGroupPropSchema("test", STRING, TestObj::class.asTypeName() as TypeName)
            expect { true }
            whenever { param.verifyNotEmpty }
        }
    }

    @Test
    fun `verifyNotNull is false`() = test {
        given {
            val param = MapGroupPropSchema("test", STRING, TestObj::class.asTypeName() as TypeName)
            expect { false }
            whenever { param.verifyNotNull }
        }
    }

    @Test
    fun `propertyValueReturn - nullable returns propName`() = test {
        given {
            val param = MapGroupPropSchema(
                "test", STRING, TestObj::class.asTypeName() as TypeName, nullableAssignment = true
            )
            expect { "test" }
            whenever { param.propertyValueReturn() }
        }
    }

    @Test
    fun `propertyValueReturn - non-nullable returns requireMapNotEmpty`() = test {
        given {
            val param = MapGroupPropSchema(
                "test", STRING, TestObj::class.asTypeName() as TypeName, nullableAssignment = false
            )
            expect { "DslValidation.requireMapNotEmpty(::test)" }
            whenever { param.propertyValueReturn() }
        }
    }

    @Test
    fun `accessors - includes KDoc from docString`() = test {
        given {
            val metadata = PropertyAnnotationMetadata(docString = "Mapped builder items")
            val param = MapGroupPropSchema(
                "items", STRING, TestObj::class.asTypeName() as TypeName,
                annotationMetadata = metadata
            )
            expect { true }
            whenever { param.accessors().first().toString().contains("Mapped builder items") }
        }
    }

    @Test
    fun `accessors - no KDoc when no description`() = test {
        given {
            val param = MapGroupPropSchema("items", STRING, TestObj::class.asTypeName() as TypeName)
            expect { false }
            whenever { param.accessors().first().toString().contains("/**") }
        }
    }

    class TestObj
}
