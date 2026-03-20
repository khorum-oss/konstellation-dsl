package org.khorum.oss.konstellation.dsl.props

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.schema.GroupPropSchema
import org.junit.jupiter.api.Test

class GroupParamTest : UnitSim() {
    val propTypeName = LIST.parameterizedBy(TestObj::class.asTypeName() as TypeName).copy(nullable = true)
    val groupBuilderName = ClassName("test", "TestObj")

    @Test
    fun `toPropertySpec - happy path`() = test {
        given {
            val param = GroupPropSchema("test", propTypeName, groupBuilderName)

            expect {
                "protected var test: kotlin.collections.List<test.TestObj>? = null"
            }

            whenever {
                val propSpec = param.toPropertySpec()

                propSpec.toString().trimIndent()
            }
        }
    }

    @Test
    fun `accessors - happy path`() = test {
        given {
            val param = GroupPropSchema("test", propTypeName, groupBuilderName)

            expect {
                """
                    |public fun test(block: test.TestObjDslBuilder.Group.() -> kotlin.Unit) {
                    |  this.test = test.TestObjDslBuilder.Group().apply(block).items()
                    |}
                """.trimMargin()
            }

            whenever { param.accessors().first().toString().trimIndent() }
        }
    }

    @Test
    fun `isCollection returns true`() = test {
        given {
            val param = GroupPropSchema("test", propTypeName, groupBuilderName)
            expect { true }
            whenever { param.isCollection() }
        }
    }

    @Test
    fun `isMap returns false`() = test {
        given {
            val param = GroupPropSchema("test", propTypeName, groupBuilderName)
            expect { false }
            whenever { param.isMap() }
        }
    }

    @Test
    fun `propertyValueReturn - nullable returns propName`() = test {
        given {
            val param = GroupPropSchema("test", propTypeName, groupBuilderName, nullableAssignment = true)
            expect { "test" }
            whenever { param.propertyValueReturn() }
        }
    }

    class TestObj
}
