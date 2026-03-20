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

    @Test
    fun `isCollection returns true for map iterableType`() = test {
        given {
            val param = MapPropSchema("test", STRING, INT)
            expect { true }
            whenever { param.isCollection() }
        }
    }

    @Test
    fun `isMap returns false for map with COLLECTION iterableType`() = test {
        given {
            val param = MapPropSchema("test", STRING, INT)
            expect { false }
            whenever { param.isMap() }
        }
    }

    @Test
    fun `propertyValueReturn - nullable returns propName`() = test {
        given {
            val param = MapPropSchema("codes", STRING, INT, nullableAssignment = true)
            expect { "codes" }
            whenever { param.propertyValueReturn() }
        }
    }

    @Test
    fun `propertyValueReturn - non-nullable returns vRequireCollectionNotEmpty`() = test {
        given {
            val param = MapPropSchema("codes", STRING, INT, nullableAssignment = false)
            expect { "vRequireCollectionNotEmpty(::codes)" }
            whenever { param.propertyValueReturn() }
        }
    }

    @Test
    fun `accessors - both vararg and provider generates two functions`() = test {
        given {
            val param = MapPropSchema("test", STRING, INT, withVararg = true, withProvider = true)
            expect { 2 }
            whenever { param.accessors().size }
        }
    }

    @Test
    fun `accessors - neither vararg nor provider generates empty list`() = test {
        given {
            val param = MapPropSchema("test", STRING, INT, withVararg = false, withProvider = false)
            expect { 0 }
            whenever { param.accessors().size }
        }
    }

    @Test
    fun `provider accessor contains apply block`() = test {
        given {
            val param = MapPropSchema("test", STRING, INT, withVararg = false, withProvider = true)
            expect { true }
            whenever {
                val accessor = param.accessors().first().toString()
                accessor.contains("apply(block)")
            }
        }
    }
}
