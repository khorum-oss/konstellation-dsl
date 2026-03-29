package org.khorum.oss.konstellation.dsl.builder

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.asTypeName
import org.khorum.oss.geordi.UnitSim
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class KotlinPoetBuilderTest : UnitSim() {

    @Nested
    inner class KotlinPoetFunctionTest {
        @Test
        fun `kotlinPoet returns builder result`() = test {
            given {
                expect { 42 }
                whenever { kotlinPoet { 42 } }
            }
        }

    }

    @Nested
    inner class PropertyTest {
        @Test
        fun `property builds a PropertySpec`() = test {
            given {
                expect { "var myProp: kotlin.String" }

                whenever {
                    kotlinPoet {
                        property {
                            name = "myProp"
                            type = String::class.asTypeName()
                        }
                    }.toString().trimIndent().trim()
                }
            }
        }
    }

    @Nested
    inner class FunctionTest {
        @Test
        fun `function builds a FunSpec`() = test {
            given {
                expect {
                    """
                        |public fun greet() {
                        |}
                    """.trimMargin()
                }

                whenever {
                    kotlinPoet {
                        function {
                            funName = "greet"
                        }
                    }.toString().trimIndent().trim()
                }
            }
        }
    }

    @Nested
    inner class FunctionsTest {
        @Test
        fun `functions builds a list of FunSpec`() = test {
            given {
                expect { 2 }

                whenever {
                    kotlinPoet {
                        functions {
                            add { funName = "foo" }
                            add { funName = "bar" }
                        }
                    }.size
                }
            }
        }

        @Test
        fun `functions returns specs with correct names`() = test {
            given {
                expect { listOf("foo", "bar") }

                whenever {
                    kotlinPoet {
                        functions {
                            add { funName = "foo" }
                            add { funName = "bar" }
                        }
                    }.map { it.name }
                }
            }
        }
    }

    @Nested
    inner class TypeTest {
        @Test
        fun `type builds a TypeSpec`() = test {
            given {
                expect { "MyClass" }

                whenever {
                    kotlinPoet {
                        type { name = "MyClass" }
                    }.name
                }
            }
        }
    }

    @Nested
    inner class TypeAliasTest {
        @Test
        fun `typeAlias builds a TypeAliasSpec`() = test {
            given {
                expect { "public typealias StringList = kotlin.collections.List<kotlin.String>" }

                whenever {
                    kotlinPoet {
                        typeAlias {
                            name = "StringList"
                            type = kpListOf(String::class.asTypeName(), nullable = false)
                        }
                    }.toString().trimIndent().trim()
                }
            }
        }
    }

    @Nested
    inner class FileTest {
        @Test
        fun `file builds a FileSpec`() = test {
            given {
                expect { "com.example" }

                whenever {
                    kotlinPoet {
                        file {
                            className = ClassName("com.example", "MyFile")
                        }
                    }.packageName
                }
            }
        }
    }

    @Nested
    inner class ListTypeOfTest {
        @Test
        fun `listTypeOf produces nullable parameterized list type`() = test {
            given {
                expect { "kotlin.collections.List<kotlin.String>?" }

                whenever {
                    kotlinPoet {
                        listTypeOf(ClassName("kotlin", "String"))
                    }.toString()
                }
            }
        }

        @Test
        fun `listTypeOf with nullable false produces non-null type`() = test {
            given {
                expect { "kotlin.collections.List<kotlin.String>" }

                whenever {
                    kotlinPoet {
                        listTypeOf(ClassName("kotlin", "String"), nullable = false)
                    }.toString()
                }
            }
        }
    }

    @Nested
    inner class MutableListOfTest {
        @Test
        fun `mutableListOf produces non-nullable mutable list type`() = test {
            given {
                expect { "kotlin.collections.MutableList<kotlin.String>" }

                whenever {
                    kotlinPoet {
                        mutableListOf(ClassName("kotlin", "String"))
                    }.toString()
                }
            }
        }
    }

    @Nested
    inner class PairTypeOfTest {
        @Test
        fun `pairTypeOf produces nullable pair type`() = test {
            given {
                expect { "kotlin.Pair<kotlin.String, kotlin.Int>?" }

                whenever {
                    kotlinPoet {
                        pairTypeOf(STRING, INT)
                    }.toString()
                }
            }
        }

        @Test
        fun `pairTypeOf with nullable false produces non-null pair type`() = test {
            given {
                expect { "kotlin.Pair<kotlin.String, kotlin.Int>" }

                whenever {
                    kotlinPoet {
                        pairTypeOf(STRING, INT, nullable = false)
                    }.toString()
                }
            }
        }
    }

    @Nested
    inner class TopLevelKpMapOfTest {
        @Test
        fun `kpMapOf produces nullable map type`() = test {
            given {
                expect { "kotlin.collections.Map<kotlin.String, kotlin.Int>?" }
                whenever { kpMapOf(STRING, INT).toString() }
            }
        }

        @Test
        fun `kpMapOf with nullable false produces non-null map type`() = test {
            given {
                expect { "kotlin.collections.Map<kotlin.String, kotlin.Int>" }
                whenever { kpMapOf(STRING, INT, nullable = false).toString() }
            }
        }
    }

    @Nested
    inner class TopLevelKpMutableMapOfTest {
        @Test
        fun `kpMutableMapOf produces nullable mutable map type`() = test {
            given {
                expect { "kotlin.collections.MutableMap<kotlin.String, kotlin.Int>?" }
                whenever { kpMutableMapOf(STRING, INT).toString() }
            }
        }

        @Test
        fun `kpMutableMapOf with nullable false produces non-null mutable map type`() = test {
            given {
                expect { "kotlin.collections.MutableMap<kotlin.String, kotlin.Int>" }
                whenever { kpMutableMapOf(STRING, INT, nullable = false).toString() }
            }
        }
    }

    @Nested
    inner class TopLevelKpListOfTest {
        @Test
        fun `kpListOf produces nullable list type`() = test {
            given {
                expect { "kotlin.collections.List<kotlin.String>?" }
                whenever { kpListOf(STRING).toString() }
            }
        }

        @Test
        fun `kpListOf with nullable false produces non-null list type`() = test {
            given {
                expect { "kotlin.collections.List<kotlin.String>" }
                whenever { kpListOf(STRING, nullable = false).toString() }
            }
        }
    }

    @Nested
    inner class TopLevelKpMutableListOfTest {
        @Test
        fun `kpMutableListOf produces nullable mutable list type`() = test {
            given {
                expect { "kotlin.collections.MutableList<kotlin.Int>?" }
                whenever { kpMutableListOf(INT).toString() }
            }
        }

        @Test
        fun `kpMutableListOf with nullable false produces non-null mutable list type`() = test {
            given {
                expect { "kotlin.collections.MutableList<kotlin.Int>" }
                whenever { kpMutableListOf(INT, nullable = false).toString() }
            }
        }
    }
}
