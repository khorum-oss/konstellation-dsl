package org.khorum.oss.konstellation.dsl.builder

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.INT
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
    fun `kdoc adds documentation to type`() = test {
        given {
            expect { true }

            whenever {
                val builder = KPTypeSpecBuilder().apply {
                    name = "MyClass"
                    kdoc("This is a documented class")
                }

                builder.build().toString().contains("This is a documented class")
            }
        }
    }

    @Test
    fun `properties with list overload adds properties`() = test {
        given {
            expect { true }

            whenever {
                val propList = listOf(
                    PropertySpec.builder("prop1", STRING).build(),
                    PropertySpec.builder("prop2", INT).build()
                )
                val builder = KPTypeSpecBuilder().apply {
                    name = "MyClass"
                    properties(propList)
                }

                val output = builder.build().toString()
                output.contains("prop1") && output.contains("prop2")
            }
        }
    }

    @Test
    fun `nested called twice uses shared group`() = test {
        given {
            expect { true }

            whenever {
                val builder = KPTypeSpecBuilder().apply {
                    name = "MyClass"
                    nested {
                        addType { name = "First" }
                    }
                    nested {
                        addType { name = "Second" }
                    }
                }

                val output = builder.build().toString()
                output.contains("class First") && output.contains("class Second")
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

    @Test
    fun `build with all features combined`() = test {
        given {
            expect { true }
            whenever {
                val builder = KPTypeSpecBuilder().apply {
                    name = "FullClass"
                    kdoc("A fully featured class")
                    annotation("com.example", "MyAnnotation")
                    superInterface(ClassName("com.example", "MyInterface"))
                    typeVariables("T")
                    properties {
                        add {
                            name = "prop1"
                            type = STRING
                        }
                    }
                    functions {
                        add {
                            funName = "doStuff"
                        }
                    }
                    nested {
                        addType { name = "Inner" }
                    }
                }
                val output = builder.build().toString()
                output.contains("FullClass") &&
                    output.contains("MyAnnotation") &&
                    output.contains("MyInterface") &&
                    output.contains("prop1") &&
                    output.contains("doStuff") &&
                    output.contains("class Inner")
            }
        }
    }

    @Test
    fun `build without kdoc has no documentation`() = test {
        given {
            expect { true }
            whenever {
                val builder = KPTypeSpecBuilder().apply {
                    name = "MyClass"
                }
                builder.build().kdoc.isEmpty()
            }
        }
    }

    @Test
    fun `multiple annotations are all included`() = test {
        given {
            expect { true }
            whenever {
                val builder = KPTypeSpecBuilder().apply {
                    name = "MyClass"
                    annotations {
                        annotation("com.a", "First")
                        annotation("com.b", "Second")
                    }
                }
                val output = builder.build().toString()
                output.contains("First") && output.contains("Second")
            }
        }
    }
}
