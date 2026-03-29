package org.khorum.oss.konstellation.dsl.builder

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.INT
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.exception.KonstellationException
import org.junit.jupiter.api.Test

class KPPropertySpecBuilderTest : UnitSim() {

    @Test
    fun `build with name and type produces property`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPPropertySpecBuilder().apply {
                    name = "myProp"
                    type = STRING
                }.build()
                spec.name == "myProp"
            }
        }
    }

    @Test
    fun `build throws when name is null`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>("name must be set") {
                KPPropertySpecBuilder().apply {
                    type = STRING
                }.build()
            }
        }
    }

    @Test
    fun `build throws when type is null`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>("type must be set") {
                KPPropertySpecBuilder().apply {
                    name = "myProp"
                }.build()
            }
        }
    }

    @Test
    fun `build defaults to mutable`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPPropertySpecBuilder().apply {
                    name = "myProp"
                    type = STRING
                }.build()
                spec.mutable
            }
        }
    }

    @Test
    fun `build with mutable false produces val`() = test {
        given {
            expect { false }
            whenever {
                val spec = KPPropertySpecBuilder().apply {
                    name = "myProp"
                    type = STRING
                    mutable = false
                }.build()
                spec.mutable
            }
        }
    }

    @Test
    fun `build with initializer sets initial value`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPPropertySpecBuilder().apply {
                    name = "myProp"
                    type = STRING
                    initializer("\"hello\"")
                }.build()
                spec.initializer.toString().contains("hello")
            }
        }
    }

    @Test
    fun `build with initNullValue sets null initializer`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPPropertySpecBuilder().apply {
                    name = "myProp"
                    type = STRING.copy(nullable = true)
                    initNullValue()
                }.build()
                spec.initializer.toString() == "null"
            }
        }
    }

    @Test
    fun `build with kdoc adds documentation`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPPropertySpecBuilder().apply {
                    name = "myProp"
                    type = STRING
                    kdoc("This is a property doc")
                }.build()
                spec.kdoc.toString().contains("This is a property doc")
            }
        }
    }

    @Test
    fun `build without kdoc has no documentation`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPPropertySpecBuilder().apply {
                    name = "myProp"
                    type = STRING
                }.build()
                spec.kdoc.isEmpty()
            }
        }
    }

    @Test
    fun `build without initializer has no initializer`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPPropertySpecBuilder().apply {
                    name = "myProp"
                    type = STRING
                }.build()
                spec.initializer == null
            }
        }
    }

    @Test
    fun `initializer property can be set directly with CodeBlock`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPPropertySpecBuilder().apply {
                    name = "myProp"
                    type = INT
                    initializer = CodeBlock.of("42")
                }.build()
                spec.initializer.toString() == "42"
            }
        }
    }

    @Test
    fun `ALL_ACCESS_MODIFIERS contains all four modifiers`() = test {
        given {
            expect { true }
            whenever {
                val modifiers = KPPropertySpecBuilder.ALL_ACCESS_MODIFIERS
                modifiers.size == 4 &&
                    modifiers.contains(KModifier.PUBLIC) &&
                    modifiers.contains(KModifier.PROTECTED) &&
                    modifiers.contains(KModifier.INTERNAL) &&
                    modifiers.contains(KModifier.PRIVATE)
            }
        }
    }

    @Test
    fun `Group add with block builds and adds property`() = test {
        given {
            expect { 1 }
            whenever {
                val group = KPPropertySpecBuilder.Group()
                group.add {
                    name = "prop1"
                    type = STRING
                }
                group.items.size
            }
        }
    }

    @Test
    fun `Group add with PropertySpec adds directly`() = test {
        given {
            expect { 1 }
            whenever {
                val group = KPPropertySpecBuilder.Group()
                val spec = KPPropertySpecBuilder().apply {
                    name = "prop1"
                    type = STRING
                }.build()
                group.add(spec)
                group.items.size
            }
        }
    }

    @Test
    fun `Group addForEachIn transforms and adds all`() = test {
        given {
            expect { 3 }
            whenever {
                val group = KPPropertySpecBuilder.Group()
                group.addForEachIn(listOf("a", "b", "c")) { propName ->
                    KPPropertySpecBuilder().apply {
                        name = propName
                        type = STRING
                    }.build()
                }
                group.items.size
            }
        }
    }

    @Test
    fun `Group addForEach extension transforms and adds all`() = test {
        given {
            expect { 2 }
            whenever {
                val group = KPPropertySpecBuilder.Group()
                with(group) {
                    listOf("x", "y").addForEach { propName ->
                        KPPropertySpecBuilder().apply {
                            name = propName
                            type = INT
                        }.build()
                    }
                }
                group.items.size
            }
        }
    }

    @Test
    fun `build with private modifier includes PRIVATE`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPPropertySpecBuilder().apply {
                    name = "myProp"
                    type = STRING
                    private()
                }.build()
                spec.modifiers.contains(KModifier.PRIVATE)
            }
        }
    }

    @Test
    fun `build with protected modifier includes PROTECTED`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPPropertySpecBuilder().apply {
                    name = "myProp"
                    type = STRING
                    protected()
                }.build()
                spec.modifiers.contains(KModifier.PROTECTED)
            }
        }
    }

    @Test
    fun `setting two access modifiers throws exception`() {
        try {
            KPPropertySpecBuilder().apply {
                name = "myProp"
                type = STRING
                public()
                private()
            }.build()
            assert(false) { "Expected KonstellationException" }
        } catch (e: KonstellationException) {
            assert(e.message!!.contains("access modifier already set"))
        }
    }

    @Test
    fun `build with variable makes property mutable`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPPropertySpecBuilder().apply {
                    name = "myProp"
                    type = STRING
                    variable()
                }.build()
                spec.mutable
            }
        }
    }

    @Test
    fun `kdocString getter returns set value`() = test {
        given {
            expect { "my doc" }
            whenever {
                val builder = KPPropertySpecBuilder()
                builder.kdoc("my doc")
                builder.kdocString
            }
        }
    }

    @Test
    fun `kdocString getter returns null when not set`() = test {
        given {
            expect { null }
            whenever { KPPropertySpecBuilder().kdocString }
        }
    }

    @Test
    fun `build with both initializer and kdoc includes both`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPPropertySpecBuilder().apply {
                    name = "myProp"
                    type = STRING
                    initializer("\"hello\"")
                    kdoc("The greeting")
                }.build()
                spec.initializer.toString().contains("hello") &&
                    spec.kdoc.toString().contains("The greeting")
            }
        }
    }
}
