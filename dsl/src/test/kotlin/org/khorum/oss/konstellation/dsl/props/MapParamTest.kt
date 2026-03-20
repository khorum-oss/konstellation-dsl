package org.khorum.oss.konstellation.dsl.props

import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.STRING
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.schema.MapPropSchema
import org.junit.jupiter.api.Test

class MapParamTest : UnitSim() {

    @Test
    fun `toPropertySpec - happy path`() = test {
        given {
            val param = MapPropSchema("test", STRING, INT)

            expect { "protected var test: kotlin.collections.Map<kotlin.String, kotlin.Int>? = null" }

            whenever {
                val propSpec = param.toPropertySpec()

                propSpec.toString().trimIndent()
            }
        }
    }

    @Test
    fun `accessors - happy path`() = test {
        given {
            val param = MapPropSchema("test", STRING, INT, nullableAssignment = true)

            expect {
                """
                    |public fun test(vararg items: kotlin.Pair<kotlin.String, kotlin.Int>) {
                    |  this.test = items.toMap()
                    |}
                """.trimMargin()
            }

            whenever { param.accessors().first().toString().trimIndent() }
        }
    }

    @Test
    fun `accessors - withVararg false omits vararg function`() = test {
        given {
            val param = MapPropSchema("test", STRING, INT, withVararg = false)
            expect { true }
            whenever { param.accessors().none { it.toString().contains("vararg") } }
        }
    }

    @Test
    fun `accessors - withProvider false omits provider function`() = test {
        given {
            val param = MapPropSchema("test", STRING, INT, withProvider = false)
            expect { true }
            whenever { param.accessors().none { it.toString().contains("block") } }
        }
    }
}
