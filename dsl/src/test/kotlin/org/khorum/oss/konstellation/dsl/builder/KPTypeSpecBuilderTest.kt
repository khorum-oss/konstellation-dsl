package org.khorum.oss.konstellation.dsl.builder

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeVariableName
import org.khorum.oss.geordi.UnitSim
import org.junit.jupiter.api.Test

class KPTypeSpecBuilderTest : UnitSim() {

    @Test
    fun `build with name only produces minimal class`() = test {
        given {
            expect { true }

            whenever {
                val builder = KPTypeSpecBuilder().apply {
                    name = "MyClass"
                }

                builder.build().toString().contains("class MyClass")
            }
        }
    }

    @Test
    fun `build throws when name is null`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>(exceptionMessage = "Type - name must be set") {
                KPTypeSpecBuilder().build()
            }
        }
    }

    @Test
    fun `superInterface appears in built TypeSpec`() = test {
        given {
            expect { true }

            whenever {
                val builder = KPTypeSpecBuilder().apply {
                    name = "MyClass"
                    superInterface(ClassName("com.example", "MyInterface"))
                }

                builder.build().toString().contains("MyInterface")
            }
        }
    }

    @Test
    fun `annotations block adds annotation`() = test {
        given {
            expect { true }

            whenever {
                val builder = KPTypeSpecBuilder().apply {
                    name = "MyClass"
                    annotations {
                        annotation("com.example", "MyAnnotation")
                    }
                }

                builder.build().toString().contains("@com.example.MyAnnotation")
            }
        }
    }

    @Test
    fun `annotation with packageName and simpleName adds annotation directly`() = test {
        given {
            expect { true }

            whenever {
                val builder = KPTypeSpecBuilder().apply {
                    name = "MyClass"
                    annotation("com.example", "DirectAnnotation")
                }

                builder.build().toString().contains("@com.example.DirectAnnotation")
            }
        }
    }

    @Test
    fun `typeVariables with String sets type params`() = test {
        given {
            expect { true }

            whenever {
                val builder = KPTypeSpecBuilder().apply {
                    name = "MyClass"
                    typeVariables("T", "U")
                }

                val output = builder.build().toString()
                output.contains("class MyClass<T, U>")
            }
        }
    }

    @Test
    fun `typeVariables with TypeVariableName sets type params`() = test {
        given {
            expect { true }

            whenever {
                val builder = KPTypeSpecBuilder().apply {
                    name = "MyClass"
                    typeVariables(TypeVariableName("T"), TypeVariableName("U"))
                }

                val output = builder.build().toString()
                output.contains("class MyClass<T, U>")
            }
        }
    }

    @Test
    fun `properties block adds properties`() = test {
        given {
            expect { true }

            whenever {
                val builder = KPTypeSpecBuilder().apply {
                    name = "MyClass"
                    properties {
                        add {
                            name = "myProp"
                            type = STRING
                        }
                    }
                }

                builder.build().toString().contains("myProp")
            }
        }
    }

    @Test
    fun `functions block adds functions`() = test {
        given {
            expect { true }

            whenever {
                val builder = KPTypeSpecBuilder().apply {
                    name = "MyClass"
                    functions {
                        add {
                            funName = "myFunction"
                        }
                    }
                }

                builder.build().toString().contains("fun myFunction()")
            }
        }
    }

    @Test
    fun `nested block adds nested types`() = test {
        given {
            expect { true }

            whenever {
                val builder = KPTypeSpecBuilder().apply {
                    name = "MyClass"
                    nested {
                        addType {
                            name = "NestedClass"
                        }
                    }
                }

                builder.build().toString().contains("class NestedClass")
            }
        }
    }

    @Test
    fun `Group addType builds and adds TypeSpec`() = test {
        given {
            expect { 1 }

            whenever {
                val group = KPTypeSpecBuilder.Group()
                group.addType {
                    name = "GroupedClass"
                }

                group.items.size
            }
        }
    }

    @Test
    fun `Group addType builds TypeSpec with correct name`() = test {
        given {
            expect { true }

            whenever {
                val group = KPTypeSpecBuilder.Group()
                group.addType {
                    name = "GroupedClass"
                }

                group.items.first().toString().contains("class GroupedClass")
            }
        }
    }
}
