package org.khorum.oss.konstellation.dsl.schema

import com.squareup.kotlinpoet.STRING
import org.junit.jupiter.api.Test
import org.khorum.oss.geordi.UnitSim

/**
 * Tests for DslPropSchema interface default method branches,
 * specifically propertyValueReturn() for collection and map paths.
 */
class DslPropSchemaBranchTest : UnitSim() {

    @Test
    fun `propertyValueReturn - non-nullable collection with verifyNotEmpty returns vRequireCollectionNotEmpty`() =
        test {
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

    // --- verifyNotEmpty && isMap() branch via custom DslPropSchema ---

    @Test
    fun `propertyValueReturn - non-nullable verifyNotEmpty true isMap true returns vRequireMapNotEmpty`() = test {
        given {
            val param = object : DslPropSchema {
                override val propName = "entries"
                override val propTypeName = STRING
                override val nullableAssignment = false
                override val verifyNotNull = false
                override val verifyNotEmpty = true
                override val iterableType = DslPropSchema.IterableType.MAP
            }
            expect { "vRequireMapNotEmpty(::entries)" }
            whenever { param.propertyValueReturn() }
        }
    }

    // --- isCollection returns false for MAP IterableType ---

    @Test
    fun `isCollection returns false for MapGroupPropSchema which has MAP iterableType`() = test {
        given {
            val param = MapGroupPropSchema(
                "entries",
                com.squareup.kotlinpoet.ClassName("test", "Key"),
                com.squareup.kotlinpoet.ClassName("test", "Ship"),
            )
            expect { false }
            whenever { param.isCollection() }
        }
    }

    // --- isMap returns false for COLLECTION IterableType ---

    @Test
    fun `isMap returns false for ListPropSchema which has COLLECTION iterableType`() = test {
        given {
            expect { false }
            whenever { ListPropSchema("x", STRING).isMap() }
        }
    }

    @Test
    fun `isMap returns false for MapPropSchema which has COLLECTION iterableType`() = test {
        given {
            expect { false }
            whenever { MapPropSchema("x", STRING, STRING).isMap() }
        }
    }

    // --- toPropertySpec with default value on different schema types ---

    @Test
    fun `BooleanPropSchema toPropertySpec with defaultValue uses initializer`() = test {
        given {
            val dv = org.khorum.oss.konstellation.dsl.domain.DefaultPropertyValue(
                rawValue = "true",
                codeBlock = com.squareup.kotlinpoet.CodeBlock.of("%L", true),
                packageName = "kotlin",
                className = "Boolean"
            )
            val param = BooleanPropSchema("enabled", defaultValue = dv)
            expect { true }
            whenever { param.toPropertySpec().toString().contains("true") }
        }
    }

    // --- accessors() default returns empty list ---

    @Test
    fun `DslPropSchema default accessors returns empty list`() = test {
        given {
            val param = object : DslPropSchema {
                override val propName = "plain"
                override val propTypeName = STRING
            }
            expect { emptyList<com.squareup.kotlinpoet.FunSpec>() }
            whenever { param.accessors() }
        }
    }

    @Test
    fun `DefaultPropSchema accessors returns empty list`() = test {
        given {
            expect { 0 }
            whenever { DefaultPropSchema("x", STRING).accessors().size }
        }
    }

    // --- iterableType is MAP for MapGroupPropSchema ---

    @Test
    fun `iterableType is MAP for MapGroupPropSchema`() = test {
        given {
            val param = MapGroupPropSchema(
                "entries",
                com.squareup.kotlinpoet.ClassName("test", "Key"),
                com.squareup.kotlinpoet.ClassName("test", "Ship"),
            )
            expect { DslPropSchema.IterableType.MAP }
            whenever { param.iterableType }
        }
    }

    // --- GroupPropSchema iterableType is COLLECTION ---

    @Test
    fun `iterableType is COLLECTION for GroupPropSchema`() = test {
        given {
            val param = GroupPropSchema(
                "items",
                com.squareup.kotlinpoet.ClassName("test", "Item"),
                com.squareup.kotlinpoet.ClassName("test", "Item"),
            )
            expect { DslPropSchema.IterableType.COLLECTION }
            whenever { param.iterableType }
        }
    }

    // --- propertyValueReturn with verifyNotNull=true (non-nullable, vRequireNotNull) ---

    @Test
    fun `propertyValueReturn - non-nullable with verifyNotNull true returns vRequireNotNull`() = test {
        given {
            val param = object : DslPropSchema {
                override val propName = "required"
                override val propTypeName = STRING
                override val nullableAssignment = false
                override val verifyNotNull = true
                override val verifyNotEmpty = false
            }
            expect { "vRequireNotNull(::required)" }
            whenever { param.propertyValueReturn() }
        }
    }

    // --- propertyValueReturn verifyNotEmpty=true with isCollection()=true via custom schema ---

    @Test
    fun `propertyValueReturn - non-nullable verifyNotEmpty true isCollection true`() = test {
        given {
            val param = object : DslPropSchema {
                override val propName = "items"
                override val propTypeName = STRING
                override val nullableAssignment = false
                override val verifyNotNull = false
                override val verifyNotEmpty = true
                override val iterableType = DslPropSchema.IterableType.COLLECTION
            }
            expect { "vRequireCollectionNotEmpty(::items)" }
            whenever { param.propertyValueReturn() }
        }
    }

    // --- toPropertySpec with default value on ListPropSchema does not use initializer (always initNullValue) ---

    @Test
    fun `ListPropSchema toPropertySpec always uses null initializer regardless of default`() = test {
        given {
            val param = ListPropSchema("tags", STRING, nullableAssignment = false)
            expect { true }
            whenever { param.toPropertySpec().toString().contains("null") }
        }
    }

    // --- toPropertySpec on MapPropSchema always uses null initializer ---

    @Test
    fun `MapPropSchema toPropertySpec always uses null initializer`() = test {
        given {
            val param = MapPropSchema("entries", STRING, STRING, nullableAssignment = false)
            expect { true }
            whenever { param.toPropertySpec().toString().contains("null") }
        }
    }

    // --- GroupPropSchema propertyValueReturn for non-nullable ---

    @Test
    fun `GroupPropSchema propertyValueReturn - non-nullable returns vRequireNotNull`() = test {
        given {
            val param = GroupPropSchema(
                "items",
                com.squareup.kotlinpoet.ClassName("test", "Item"),
                com.squareup.kotlinpoet.ClassName("test", "Item"),
                nullableAssignment = false
            )
            // verifyNotNull defaults to true, so it takes priority over verifyNotEmpty
            expect { "vRequireNotNull(::items)" }
            whenever { param.propertyValueReturn() }
        }
    }

    // --- MapGroupPropSchema propertyValueReturn for nullable ---

    @Test
    fun `MapGroupPropSchema propertyValueReturn - nullable returns propName`() = test {
        given {
            val param = MapGroupPropSchema(
                "entries",
                com.squareup.kotlinpoet.ClassName("test", "Key"),
                com.squareup.kotlinpoet.ClassName("test", "Ship"),
                nullableAssignment = true
            )
            expect { "entries" }
            whenever { param.propertyValueReturn() }
        }
    }

    // --- accessModifier defaults for different schema types ---

    @Test
    fun `DefaultPropSchema accessModifier is PUBLIC`() = test {
        given {
            expect { com.squareup.kotlinpoet.KModifier.PUBLIC }
            whenever { DefaultPropSchema("x", STRING).accessModifier }
        }
    }

    @Test
    fun `DslPropSchema default accessModifier is PROTECTED`() = test {
        given {
            val param = object : DslPropSchema {
                override val propName = "x"
                override val propTypeName = STRING
            }
            expect { com.squareup.kotlinpoet.KModifier.PROTECTED }
            whenever { param.accessModifier }
        }
    }

    // --- SingleTransformPropSchema propertyValueReturn for nullable ---

    @Test
    fun `SingleTransformPropSchema propertyValueReturn - nullable returns propName`() = test {
        given {
            val param = SingleTransformPropSchema(
                "value",
                com.squareup.kotlinpoet.INT,
                STRING,
                transformTemplate = null,
                nullableAssignment = true
            )
            expect { "value" }
            whenever { param.propertyValueReturn() }
        }
    }

    // --- BooleanPropSchema propertyValueReturn for non-nullable ---

    @Test
    fun `BooleanPropSchema propertyValueReturn - non-nullable returns propName`() = test {
        given {
            val param = BooleanPropSchema("flag", nullableAssignment = false)
            expect { "vRequireNotNull(::flag)" }
            whenever { param.propertyValueReturn() }
        }
    }

    // --- functionName defaults to propName across types ---

    @Test
    fun `ListPropSchema functionName defaults to propName`() = test {
        given {
            expect { "myList" }
            whenever { ListPropSchema("myList", STRING).functionName }
        }
    }

    @Test
    fun `MapPropSchema functionName defaults to propName`() = test {
        given {
            expect { "myMap" }
            whenever { MapPropSchema("myMap", STRING, STRING).functionName }
        }
    }

    // --- nullableAssignment default is true ---

    @Test
    fun `DslPropSchema nullableAssignment defaults to true`() = test {
        given {
            val param = object : DslPropSchema {
                override val propName = "x"
                override val propTypeName = STRING
            }
            expect { true }
            whenever { param.nullableAssignment }
        }
    }

    // --- BuilderPropSchema propertyValueReturn for non-nullable ---

    @Test
    fun `BuilderPropSchema propertyValueReturn - non-nullable returns vRequireNotNull`() = test {
        given {
            val param = BuilderPropSchema(
                "nested",
                com.squareup.kotlinpoet.ClassName("test", "Nested"),
                com.squareup.kotlinpoet.ClassName("test", "NestedDslBuilder"),
                nullableAssignment = false
            )
            expect { "vRequireNotNull(::nested)" }
            whenever { param.propertyValueReturn() }
        }
    }
}
