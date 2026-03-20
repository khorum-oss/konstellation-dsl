package org.khorum.oss.konstellation.dsl.builder

import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeVariableName
import org.khorum.oss.geordi.UnitSim
import org.junit.jupiter.api.Test

class KPTypeAliasSpecBuilderTest : UnitSim() {

    @Test
    fun `build with name and type produces TypeAliasSpec`() = test {
        given {
            expect { true }

            whenever {
                val builder = KPTypeAliasSpecBuilder().apply {
                    name = "MyAlias"
                    type = STRING
                }

                builder.build().toString().contains("typealias MyAlias = kotlin.String")
            }
        }
    }

    @Test
    fun `build throws when name is null`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>(exceptionMessage = "name must be set") {
                KPTypeAliasSpecBuilder().apply {
                    type = STRING
                }.build()
            }
        }
    }

    @Test
    fun `build throws when type is null`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>("Required value was null.") {
                KPTypeAliasSpecBuilder().apply {
                    name = "MyAlias"
                }.build()
            }
        }
    }

    @Test
    fun `typeVariables with String adds type params`() = test {
        given {
            expect { true }

            whenever {
                val builder = KPTypeAliasSpecBuilder().apply {
                    name = "MyAlias"
                    type = STRING
                    typeVariables("T")
                }

                builder.build().toString().contains("<T>")
            }
        }
    }

    @Test
    fun `typeVariables with TypeVariableName adds type params`() = test {
        given {
            expect { true }

            whenever {
                val builder = KPTypeAliasSpecBuilder().apply {
                    name = "MyAlias"
                    type = STRING
                    typeVariables(TypeVariableName("T"), TypeVariableName("U"))
                }

                val output = builder.build().toString()
                output.contains("<T, U>")
            }
        }
    }

    @Test
    fun `type function sets type`() = test {
        given {
            expect { true }

            whenever {
                val builder = KPTypeAliasSpecBuilder().apply {
                    name = "MyAlias"
                    type(STRING)
                }

                builder.build().toString().contains("typealias MyAlias = kotlin.String")
            }
        }
    }
}
