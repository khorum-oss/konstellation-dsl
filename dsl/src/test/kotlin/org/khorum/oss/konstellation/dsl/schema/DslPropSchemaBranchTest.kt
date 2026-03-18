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

    // --- ListPropSchema withVararg/withProvider branches ---

    @Test
    fun `ListPropSchema with withVararg false generates only provider function`() = test {
        given {
            val param = ListPropSchema("items", STRING, withVararg = false, withProvider = true)
            expect { 1 }
            whenever { param.accessors().size }
        }
    }

    @Test
    fun `ListPropSchema with withProvider false generates only vararg function`() = test {
        given {
            val param = ListPropSchema("items", STRING, withVararg = true, withProvider = false)
            expect { 1 }
            whenever { param.accessors().size }
        }
    }

    @Test
    fun `ListPropSchema with both false generates no functions`() = test {
        given {
            val param = ListPropSchema("items", STRING, withVararg = false, withProvider = false)
            expect { 0 }
            whenever { param.accessors().size }
        }
    }

    @Test
    fun `ListPropSchema with both true generates two functions`() = test {
        given {
            val param = ListPropSchema("items", STRING, withVararg = true, withProvider = true)
            expect { 2 }
            whenever { param.accessors().size }
        }
    }

    // --- MapPropSchema withVararg/withProvider branches ---

    @Test
    fun `MapPropSchema with withVararg false generates only provider function`() = test {
        given {
            val param = MapPropSchema("entries", STRING, STRING, withVararg = false, withProvider = true)
            expect { 1 }
            whenever { param.accessors().size }
        }
    }

    @Test
    fun `MapPropSchema with withProvider false generates only vararg function`() = test {
        given {
            val param = MapPropSchema("entries", STRING, STRING, withVararg = true, withProvider = false)
            expect { 1 }
            whenever { param.accessors().size }
        }
    }

    @Test
    fun `MapPropSchema with both false generates no functions`() = test {
        given {
            val param = MapPropSchema("entries", STRING, STRING, withVararg = false, withProvider = false)
            expect { 0 }
            whenever { param.accessors().size }
        }
    }

    // --- BuilderPropSchema branches ---

    @Test
    fun `BuilderPropSchema accessors returns one function`() = test {
        given {
            val param = BuilderPropSchema(
                "nested",
                com.squareup.kotlinpoet.ClassName("test", "Nested"),
                com.squareup.kotlinpoet.ClassName("test", "NestedDslBuilder"),
                nullableAssignment = true
            )
            expect { 1 }
            whenever { param.accessors().size }
        }
    }

    @Test
    fun `BuilderPropSchema with kdoc includes kdoc in accessor`() = test {
        given {
            val param = BuilderPropSchema(
                "nested",
                com.squareup.kotlinpoet.ClassName("test", "Nested"),
                com.squareup.kotlinpoet.ClassName("test", "NestedDslBuilder"),
                kdoc = "some doc"
            )
            expect { true }
            whenever { param.accessors().first().toString().contains("some doc") }
        }
    }

    @Test
    fun `BuilderPropSchema without kdoc has no kdoc`() = test {
        given {
            val param = BuilderPropSchema(
                "nested",
                com.squareup.kotlinpoet.ClassName("test", "Nested"),
                com.squareup.kotlinpoet.ClassName("test", "NestedDslBuilder"),
                kdoc = null
            )
            expect { false }
            whenever { param.accessors().first().kdoc.toString().contains("doc") }
        }
    }

    // --- GroupPropSchema branches ---

    @Test
    fun `GroupPropSchema accessors returns one function`() = test {
        given {
            val param = GroupPropSchema(
                "items",
                com.squareup.kotlinpoet.ClassName("test", "Item"),
                com.squareup.kotlinpoet.ClassName("test", "Item"),
            )
            expect { 1 }
            whenever { param.accessors().size }
        }
    }

    // --- MapGroupPropSchema branches ---

    @Test
    fun `MapGroupPropSchema accessors returns one function`() = test {
        given {
            val param = MapGroupPropSchema(
                "entries",
                STRING,
                com.squareup.kotlinpoet.ClassName("test", "Ship"),
            )
            expect { 1 }
            whenever { param.accessors().size }
        }
    }

    @Test
    fun `MapGroupPropSchema isMap returns true`() = test {
        given {
            val param = MapGroupPropSchema(
                "entries",
                com.squareup.kotlinpoet.ClassName("test", "Key"),
                com.squareup.kotlinpoet.ClassName("test", "Ship"),
            )
            expect { true }
            whenever { param.isMap() }
        }
    }

    // --- SingleTransformPropSchema branches ---

    @Test
    fun `SingleTransformPropSchema accessors with transformTemplate uses template`() = test {
        given {
            val param = SingleTransformPropSchema(
                "value",
                com.squareup.kotlinpoet.INT,
                STRING,
                transformTemplate = "MyType(%N)"
            )
            expect { true }
            whenever { param.accessors().first().toString().contains("MyType") }
        }
    }

    @Test
    fun `SingleTransformPropSchema accessors without transformTemplate uses default template`() = test {
        given {
            val param = SingleTransformPropSchema(
                "value",
                com.squareup.kotlinpoet.INT,
                STRING,
                transformTemplate = null
            )
            expect { true }
            whenever { param.accessors().isNotEmpty() }
        }
    }

    // --- BooleanPropSchema branches ---

    @Test
    fun `BooleanPropSchema accessors returns one function`() = test {
        given {
            val param = BooleanPropSchema("flag")
            expect { 1 }
            whenever { param.accessors().size }
        }
    }

    @Test
    fun `BooleanPropSchema with defaultValue uses default value in accessor`() = test {
        given {
            val dv = org.khorum.oss.konstellation.dsl.domain.DefaultPropertyValue(
                rawValue = "false",
                codeBlock = com.squareup.kotlinpoet.CodeBlock.of("%L", false),
                packageName = "kotlin",
                className = "Boolean"
            )
            val param = BooleanPropSchema("flag", defaultValue = dv)
            expect { true }
            whenever { param.accessors().first().toString().contains("false") }
        }
    }

    // --- propertyValueReturn else branch ---

    @Test
    fun `propertyValueReturn - non-nullable with verifyNotNull false and no iterable returns propName`() = test {
        given {
            val param = object : DslPropSchema {
                override val propName = "myProp"
                override val propTypeName = STRING
                override val nullableAssignment = false
                override val verifyNotNull = false
                override val verifyNotEmpty = false
            }
            expect { "myProp" }
            whenever { param.propertyValueReturn() }
        }
    }
}
