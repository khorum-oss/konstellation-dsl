package org.khorum.oss.konstellation.dsl.builder

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.asTypeName
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.exception.KonstellationException
import org.junit.jupiter.api.Test

/**
 * Branch coverage tests for builder classes.
 */
class BuilderBranchTest : UnitSim() {

    // DefaultKotlinPoetSpec branches
    @Test
    fun `accessModifier throws when modifier already set`() = test<Unit> {
        given {
            wheneverThrows<KonstellationException>("access modifier already set to PUBLIC") {
                val builder = KPPropertySpecBuilder()
                builder.public()
                builder.private() // should throw
            }
        }
    }

    @Test
    fun `private sets PRIVATE modifier`() = test {
        given {
            val builder = KPPropertySpecBuilder()
            builder.private()
            expect { true }
            whenever { builder.modifiers.contains(KModifier.PRIVATE) }
        }
    }

    @Test
    fun `protected sets PROTECTED modifier`() = test {
        given {
            val builder = KPPropertySpecBuilder()
            builder.protected()
            expect { true }
            whenever { builder.modifiers.contains(KModifier.PROTECTED) }
        }
    }

    @Test
    fun `public sets PUBLIC modifier`() = test {
        given {
            val builder = KPPropertySpecBuilder()
            builder.public()
            expect { true }
            whenever { builder.modifiers.contains(KModifier.PUBLIC) }
        }
    }

    // KPPropertySpecBuilder branches
    @Test
    fun `build with initializer includes it in output`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPPropertySpecBuilder().apply {
                    name = "test"
                    type = STRING
                    initializer("\"hello\"")
                }.build()
                spec.initializer.toString().contains("hello")
            }
        }
    }

    @Test
    fun `build without initializer has no initializer`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPPropertySpecBuilder().apply {
                    name = "test"
                    type = STRING
                }.build()
                spec.initializer == null
            }
        }
    }

    @Test
    fun `build throws when name is null`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>("name must be set") {
                KPPropertySpecBuilder().apply { type = STRING }.build()
            }
        }
    }

    @Test
    fun `build throws when type is null`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>("type must be set") {
                KPPropertySpecBuilder().apply { name = "test" }.build()
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
                    name = "prop"
                    type = STRING
                }
                group.items.size
            }
        }
    }

    @Test
    fun `Group add with spec adds property directly`() = test {
        given {
            expect { 1 }
            whenever {
                val group = KPPropertySpecBuilder.Group()
                val spec = KPPropertySpecBuilder().apply {
                    name = "prop"
                    type = STRING
                }.build()
                group.add(spec)
                group.items.size
            }
        }
    }

    @Test
    fun `Group addForEachIn transforms and adds`() = test {
        given {
            expect { 2 }
            whenever {
                val group = KPPropertySpecBuilder.Group()
                group.addForEachIn(listOf("a", "b")) { n ->
                    KPPropertySpecBuilder().apply {
                        name = n
                        type = STRING
                    }.build()
                }
                group.items.size
            }
        }
    }

    // KPParameterSpecBuilder branches
    @Test
    fun `KPParameterSpecBuilder build throws when name is null`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>("name must be set") {
                KPParameterSpecBuilder().apply {
                    type = STRING
                }.build()
            }
        }
    }

    @Test
    fun `KPParameterSpecBuilder build throws when type is null`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>("type must be set") {
                KPParameterSpecBuilder().apply {
                    name = "test"
                }.build()
            }
        }
    }

    @Test
    fun `KPParameterSpecBuilder build with defaultValue`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPParameterSpecBuilder().apply {
                    name = "count"
                    type = Int::class.asTypeName()
                    defaultValue(42)
                }.build()
                spec.defaultValue.toString().contains("42")
            }
        }
    }

    // MutabilitySpec branches
    @Test
    fun `variable sets mutable to true`() = test {
        given {
            val builder = KPPropertySpecBuilder()
            builder.variable()
            expect { true }
            whenever { builder.mutable }
        }
    }

    @Test
    fun `value sets mutable to false`() = test {
        given {
            val builder = KPPropertySpecBuilder()
            builder.value()
            expect { false }
            whenever { builder.mutable }
        }
    }

    // TypedSpec branches
    @Test
    fun `type with nullable true produces nullable type`() = test {
        given {
            val builder = KPPropertySpecBuilder()
            builder.type(STRING, nullable = true)
            expect { true }
            whenever { builder.type!!.isNullable }
        }
    }

    @Test
    fun `type with nullable false produces non-nullable type`() = test {
        given {
            val builder = KPPropertySpecBuilder()
            builder.type(STRING, nullable = false)
            expect { false }
            whenever { builder.type!!.isNullable }
        }
    }

    @Test
    fun `booleanType sets type to BOOLEAN`() = test {
        given {
            val builder = KPParameterSpecBuilder()
            builder.booleanType()
            expect { true }
            whenever { builder.type.toString().contains("Boolean") }
        }
    }

}
