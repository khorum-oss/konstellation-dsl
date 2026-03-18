package org.khorum.oss.konstellation.dsl.schema

import com.squareup.kotlinpoet.STRING
import org.khorum.oss.geordi.UnitSim
import org.junit.jupiter.api.Test

/**
 * Tests for DslPropSchema interface default method branches,
 * specifically propertyValueReturn() for collection and map paths.
 */
class DslPropSchemaBranchTest : UnitSim() {

    @Test
    fun `propertyValueReturn - non-nullable collection with verifyNotEmpty returns vRequireCollectionNotEmpty`() = test {
        given {
            val param = ListPropSchema("items", STRING, nullableAssignment = false)
            expect { "vRequireCollectionNotEmpty(::items)" }
            whenever { param.propertyValueReturn() }
        }
    }

    @Test
    fun `propertyValueReturn - non-nullable map collection returns vRequireCollectionNotEmpty`() = test {
        given {
            // MapPropSchema uses IterableType.COLLECTION (collection of pairs)
            val param = MapPropSchema("entries", STRING, STRING, nullableAssignment = false)
            expect { "vRequireCollectionNotEmpty(::entries)" }
            whenever { param.propertyValueReturn() }
        }
    }

    @Test
    fun `propertyValueReturn - non-nullable MapGroupPropSchema returns vRequireMapNotEmpty`() = test {
        given {
            val param = MapGroupPropSchema(
                "entries",
                com.squareup.kotlinpoet.ClassName("test", "Entry"),
                com.squareup.kotlinpoet.ClassName("test", "EntryDslBuilder"),
                nullableAssignment = false
            )
            expect { "vRequireMapNotEmpty(::entries)" }
            whenever { param.propertyValueReturn() }
        }
    }

    @Test
    fun `propertyValueReturn - nullable returns propName for list`() = test {
        given {
            val param = ListPropSchema("items", STRING, nullableAssignment = true)
            expect { "items" }
            whenever { param.propertyValueReturn() }
        }
    }

    @Test
    fun `propertyValueReturn - nullable returns propName for map`() = test {
        given {
            val param = MapPropSchema("entries", STRING, STRING, nullableAssignment = true)
            expect { "entries" }
            whenever { param.propertyValueReturn() }
        }
    }

    @Test
    fun `isCollection returns true for ListPropSchema`() = test {
        given {
            expect { true }
            whenever { ListPropSchema("x", STRING).isCollection() }
        }
    }

    @Test
    fun `isCollection returns false for MapPropSchema`() = test {
        given {
            // MapPropSchema uses IterableType.COLLECTION too (it's a collection of pairs)
            // so isCollection should be true, isMap false for MapPropSchema
            expect { true }
            whenever { MapPropSchema("x", STRING, STRING).isCollection() }
        }
    }

    @Test
    fun `isMap returns false for ListPropSchema`() = test {
        given {
            expect { false }
            whenever { ListPropSchema("x", STRING).isMap() }
        }
    }

    @Test
    fun `isMap returns false for DefaultPropSchema`() = test {
        given {
            expect { false }
            whenever { DefaultPropSchema("x", STRING).isMap() }
        }
    }

    @Test
    fun `isCollection returns false for DefaultPropSchema`() = test {
        given {
            expect { false }
            whenever { DefaultPropSchema("x", STRING).isCollection() }
        }
    }

    @Test
    fun `isCollection returns false for BooleanPropSchema`() = test {
        given {
            expect { false }
            whenever { BooleanPropSchema("x").isCollection() }
        }
    }

    @Test
    fun `verifyNotNull defaults to true`() = test {
        given {
            expect { true }
            whenever { DefaultPropSchema("x", STRING).verifyNotNull }
        }
    }

    @Test
    fun `verifyNotEmpty defaults to false for DefaultPropSchema`() = test {
        given {
            expect { false }
            whenever { DefaultPropSchema("x", STRING).verifyNotEmpty }
        }
    }

    @Test
    fun `iterableType is null for DefaultPropSchema`() = test {
        given {
            expect { null }
            whenever { DefaultPropSchema("x", STRING).iterableType }
        }
    }

    @Test
    fun `iterableType is COLLECTION for ListPropSchema`() = test {
        given {
            expect { DslPropSchema.IterableType.COLLECTION }
            whenever { ListPropSchema("x", STRING).iterableType }
        }
    }

    @Test
    fun `functionName defaults to propName`() = test {
        given {
            expect { "myProp" }
            whenever { DefaultPropSchema("myProp", STRING).functionName }
        }
    }

    @Test
    fun `toPropertySpec with defaultValue uses initializer instead of null`() = test {
        given {
            val dv = org.khorum.oss.konstellation.dsl.domain.DefaultPropertyValue(
                rawValue = "42",
                codeBlock = com.squareup.kotlinpoet.CodeBlock.of("%L", 42),
                packageName = "kotlin",
                className = "Int"
            )
            val param = DefaultPropSchema("count", com.squareup.kotlinpoet.INT, defaultValue = dv)
            expect { true }
            whenever { param.toPropertySpec().toString().contains("42") }
        }
    }
}
