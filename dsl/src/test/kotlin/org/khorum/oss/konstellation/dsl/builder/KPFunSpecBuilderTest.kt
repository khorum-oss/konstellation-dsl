package org.khorum.oss.konstellation.dsl.builder

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.STRING
import org.khorum.oss.geordi.UnitSim
import org.junit.jupiter.api.Test

class KPFunSpecBuilderTest : UnitSim() {

    @Test
    fun `build with just funName produces minimal function`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPFunSpecBuilder().apply { funName = "hello" }.build()
                spec.name == "hello"
            }
        }
    }

    @Test
    fun `build throws when funName is null`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>("Fun - funName must be set") {
                KPFunSpecBuilder().build()
            }
        }
    }

    @Test
    fun `build with returns sets return type`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPFunSpecBuilder().apply {
                    funName = "greet"
                    returns = STRING
                }.build()
                spec.returnType.toString().contains("String")
            }
        }
    }

    @Test
    fun `build without returns has no return type`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPFunSpecBuilder().apply { funName = "doIt" }.build()
                !spec.toString().contains(": ")
            }
        }
    }

    @Test
    fun `build with override adds OVERRIDE modifier`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPFunSpecBuilder().apply {
                    funName = "test"
                    override()
                }.build()
                spec.toString().contains("override")
            }
        }
    }

    @Test
    fun `build without override has no OVERRIDE modifier`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPFunSpecBuilder().apply { funName = "test" }.build()
                !spec.toString().contains("override")
            }
        }
    }

    @Test
    fun `build with kdoc adds documentation`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPFunSpecBuilder().apply {
                    funName = "test"
                    kdoc("This is a doc")
                }.build()
                spec.kdoc.toString().contains("This is a doc")
            }
        }
    }

    @Test
    fun `build without kdoc has no documentation`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPFunSpecBuilder().apply { funName = "test" }.build()
                spec.kdoc.isEmpty()
            }
        }
    }

    @Test
    fun `build with annotations adds annotations`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPFunSpecBuilder().apply {
                    funName = "test"
                    annotations {
                        annotation("org.test", "MyAnnotation")
                    }
                }.build()
                spec.annotations.isNotEmpty()
            }
        }
    }

    @Test
    fun `build with statements adds code`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPFunSpecBuilder().apply {
                    funName = "test"
                    statements {
                        addLine("return 42")
                    }
                }.build()
                spec.body.toString().contains("return 42")
            }
        }
    }

    @Test
    fun `Group addAll adds all specs`() = test {
        given {
            expect { 2 }
            whenever {
                val group = KPFunSpecBuilder.Group()
                val spec1 = KPFunSpecBuilder().apply { funName = "a" }.build()
                val spec2 = KPFunSpecBuilder().apply { funName = "b" }.build()
                group.addAll(listOf(spec1, spec2))
                group.items.size
            }
        }
    }

    @Test
    fun `build with override false does not add OVERRIDE modifier`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPFunSpecBuilder().apply {
                    funName = "test"
                    override(false)
                }.build()
                !spec.toString().contains("override")
            }
        }
    }

    @Test
    fun `build with addAnnotationSpec adds pre-built annotation`() = test {
        given {
            expect { true }
            whenever {
                val annotationSpec = AnnotationSpec.builder(
                    ClassName("org.test", "CustomAnnotation")
                ).build()
                val spec = KPFunSpecBuilder().apply {
                    funName = "test"
                    addAnnotationSpec(annotationSpec)
                }.build()
                spec.annotations.any { it.typeName.toString().contains("CustomAnnotation") }
            }
        }
    }

    @Test
    fun `build with params adds parameters`() = test {
        given {
            expect { true }
            whenever {
                val spec = KPFunSpecBuilder().apply {
                    funName = "greet"
                    params {
                        param {
                            name = "name"
                            type = STRING
                        }
                    }
                }.build()
                println(spec.parameters)
                spec.parameters.any { it.name == "name" }
            }
        }
    }

    @Test
    fun `Group add with block builds and adds function`() = test {
        given {
            expect { 1 }
            whenever {
                val group = KPFunSpecBuilder.Group()
                group.add {
                    funName = "myFun"
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
                val group = KPFunSpecBuilder.Group()
                with(group) {
                    listOf("foo", "bar").addForEach { name ->
                        listOf(KPFunSpecBuilder().apply { funName = name }.build())
                    }
                }
                group.items.size
            }
        }
    }

    @Test
    fun `Group addForEachIn transforms and adds all`() = test {
        given {
            expect { 2 }
            whenever {
                val group = KPFunSpecBuilder.Group()
                group.addForEachIn(listOf("foo", "bar")) { name ->
                    listOf(KPFunSpecBuilder().apply { funName = name }.build())
                }
                group.items.size
            }
        }
    }
}
