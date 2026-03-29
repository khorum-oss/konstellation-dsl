package org.khorum.oss.konstellation.dsl.props

import com.squareup.kotlinpoet.STRING
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.schema.ListPropSchema
import org.junit.jupiter.api.Test

class ListParamTest : UnitSim() {

    @Test
    fun `toPropertySpec - happy path`() = test {
        given {
            val param = ListPropSchema("test", STRING)

            expect { "protected var test: kotlin.collections.List<kotlin.String>? = null" }

            whenever {
                val propSpec = param.toPropertySpec()

                propSpec.toString().trimIndent()
            }
        }
    }

    @Test
    fun `accessors - happy path`() = test {
        given {
            val param = ListPropSchema("test", nullableAssignment = true)

            expect {
                """
                    |public fun test(vararg items: kotlin.String) {
                    |  this.test = items.toList()
                    |}
                """.trimMargin()
            }

            whenever { param.accessors().first().toString().trimIndent() }
        }
    }

    @Test
    fun `isCollection returns true`() = test {
        given {
            val param = ListPropSchema("test", STRING)
            expect { true }
            whenever { param.isCollection() }
        }
    }

    @Test
    fun `isMap returns false`() = test {
        given {
            val param = ListPropSchema("test", STRING)
            expect { false }
            whenever { param.isMap() }
        }
    }

    @Test
    fun `propertyValueReturn - nullable returns propName`() = test {
        given {
            val param = ListPropSchema("items", STRING, nullableAssignment = true)
            expect { "items" }
            whenever { param.propertyValueReturn() }
        }
    }

    @Test
    fun `propertyValueReturn - non-nullable collection returns vRequireCollectionNotEmpty`() = test {
        given {
            val param = ListPropSchema("items", STRING, nullableAssignment = false)
            expect { "vRequireCollectionNotEmpty(::items)" }
            whenever { param.propertyValueReturn() }
        }
    }

    @Test
    fun `accessors - withVararg false omits vararg function`() = test {
        given {
            val param = ListPropSchema("test", STRING, withVararg = false)
            expect { true }
            whenever { param.accessors().none { it.toString().contains("vararg") } }
        }
    }

    @Test
    fun `accessors - withProvider false omits provider function`() = test {
        given {
            val param = ListPropSchema("test", STRING, withProvider = false)
            expect { true }
            whenever { param.accessors().none { it.toString().contains("block") } }
        }
    }

    @Test
    fun `accessors - both vararg and provider generates two functions`() = test {
        given {
            val param = ListPropSchema("test", STRING, withVararg = true, withProvider = true)
            expect { 2 }
            whenever { param.accessors().size }
        }
    }

    @Test
    fun `accessors - neither vararg nor provider generates empty list`() = test {
        given {
            val param = ListPropSchema("test", STRING, withVararg = false, withProvider = false)
            expect { 0 }
            whenever { param.accessors().size }
        }
    }

    @Test
    fun `provider accessor contains mutableListOf apply`() = test {
        given {
            val param = ListPropSchema("test", STRING, withVararg = false, withProvider = true)
            expect { true }
            whenever {
                val accessor = param.accessors().first().toString()
                accessor.contains("mutableListOf")
            }
        }
    }

    @Test
    fun `verifyNotEmpty is true`() = test {
        given {
            val param = ListPropSchema("test", STRING)
            expect { true }
            whenever { param.verifyNotEmpty }
        }
    }

    @Test
    fun `verifyNotNull is false`() = test {
        given {
            val param = ListPropSchema("test", STRING)
            expect { false }
            whenever { param.verifyNotNull }
        }
    }
}
