package org.khorum.oss.konstellation.dsl.builder

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeSpec
import org.khorum.oss.geordi.UnitSim
import org.junit.jupiter.api.Test

class KPFileSpecBuilderTest : UnitSim() {
    private val defaultClassName = ClassName("com.example", "MyClass")

    private fun fileSpecBuilder(block: KPFileSpecBuilder.() -> Unit = {}): KPFileSpecBuilder =
        KPFileSpecBuilder().apply { className = defaultClassName }.apply(block)

    @Test
    fun `build produces FileSpec with correct package and className`() = test {
        given {
            expect { "com.example" }

            whenever { fileSpecBuilder().build().packageName }
        }
    }

    @Test
    fun `build throws when className is null`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>(exceptionMessage = "File - Class name must be set") {
                KPFileSpecBuilder().build()
            }
        }
    }

    @Test
    fun `addImport with ClassName adds import`() = test {
        given {
            expect { true }

            whenever {
                fileSpecBuilder { addImport(ClassName("com.other", "OtherClass")) }
                    .build().toString().contains("import com.other.OtherClass")
            }
        }
    }

    @Test
    fun `addImport with String parses and adds import`() = test {
        given {
            expect { true }

            whenever {
                fileSpecBuilder { addImport("com.other.OtherClass") }
                    .build().toString().contains("import com.other.OtherClass")
            }
        }
    }

    @Test
    fun `addImport with Pair adds import`() = test {
        given {
            expect { true }

            whenever {
                fileSpecBuilder { addImport("com.other" to "OtherClass") }
                    .build().toString().contains("import com.other.OtherClass")
            }
        }
    }

    @Test
    fun `addImportIf true adds import`() = test {
        given {
            expect { true }

            whenever {
                fileSpecBuilder { addImportIf(true, "com.other", "OtherClass") }
                    .build().toString().contains("import com.other.OtherClass")
            }
        }
    }

    @Test
    fun `addImportIf false skips import`() = test {
        given {
            expect { false }

            whenever {
                fileSpecBuilder { addImportIf(false, "com.other", "OtherClass") }
                    .build().toString().contains("import com.other.OtherClass")
            }
        }
    }

    @Test
    fun `types vararg includes types in output`() = test {
        given {
            expect { true }

            whenever {
                val typeSpec = TypeSpec.classBuilder("Inner").build()
                fileSpecBuilder { types(typeSpec) }
                    .build().toString().contains("class Inner")
            }
        }
    }

    @Test
    fun `typeAliases vararg includes aliases in output`() = test {
        given {
            expect { true }

            whenever {
                val alias = TypeAliasSpec.builder("MyAlias", STRING).build()
                fileSpecBuilder { typeAliases(alias) }
                    .build().toString().contains("typealias MyAlias")
            }
        }
    }

    @Test
    fun `functions block includes functions in output`() = test {
        given {
            expect { true }

            whenever {
                fileSpecBuilder {
                    functions { add { funName = "myFunction" } }
                }.build().toString().contains("fun myFunction()")
            }
        }
    }

    @Test
    fun `types block includes types in output`() = test {
        given {
            expect { true }

            whenever {
                fileSpecBuilder {
                    types { addType { name = "InnerClass" } }
                }.build().toString().contains("class InnerClass")
            }
        }
    }
}
