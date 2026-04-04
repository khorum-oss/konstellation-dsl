package org.khorum.oss.konstellation.dsl.process.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSValueArgument
import com.squareup.kotlinpoet.ksp.toClassName
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.metaDsl.annotation.GeneratedDsl
import org.khorum.oss.konstellation.metaDsl.annotation.SingleEntryTransformDsl
import org.junit.jupiter.api.Test
import org.khorum.oss.konstellation.dsl.process.root.DefaultRootDslAccessorGenerator

class DefaultDslGeneratorTest : UnitSim() {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setupAll() {
            mockkStatic(KSClassDeclaration::toClassName)
        }

        @JvmStatic
        @AfterAll
        fun teardownAll() {
            unmockkStatic(KSClassDeclaration::toClassName)
        }

        private fun mockKSName(value: String): KSName {
            val name: KSName = mockk()
            every { name.asString() } returns value
            every { name.getShortName() } returns value
            return name
        }

        fun mockClassWithAnnotation(vararg args: Pair<String, Any>): KSClassDeclaration {
            val cls: KSClassDeclaration = mockk()
            val ann: KSAnnotation = mockk()
            every { ann.shortName } returns mockKSName("GeneratedDsl")
            every { ann.arguments } returns args.map { (k, v) ->
                val arg: KSValueArgument = mockk()
                every { arg.name } returns mockKSName(k)
                every { arg.value } returns v
                arg
            }
            every { cls.annotations } returns sequenceOf(ann)
            every { cls.simpleName } returns mockKSName("TestDomain")
            every { cls.toClassName() } returns com.squareup.kotlinpoet.ClassName("org.test", "TestDomain")
            // Return empty properties for @RootDsl scanning
            every { cls.getAllProperties() } returns emptySequence()
            return cls
        }

        fun mockRootClassWithAnnotation(vararg args: Pair<String, Any>): KSClassDeclaration {
            val cls = mockClassWithAnnotation(*args)
            val rootAnn: KSAnnotation = mockk()
            every { rootAnn.shortName } returns mockKSName("RootDsl")
            every { rootAnn.arguments } returns emptyList()
            val existingAnns = cls.annotations.toList()
            every { cls.annotations } returns (existingAnns + rootAnn).asSequence()
            return cls
        }
    }

    private fun options() = mapOf<String, String?>(
        "projectRootClasspath" to "org.test",
        "dslBuilderClasspath" to "org.test"
    )

    @Test
    fun `generate with isIgnored returns early without calling builderGenerator`() = test {
        given {
            val resolver: Resolver = mockk()
            val codeGenerator: CodeGenerator = mockk()
            val builderGenerator: DefaultBuilderGenerator = mockk(relaxed = true)
            val generator = DefaultDslGenerator(builderGenerator = builderGenerator)
            val opts = options() + ("isIgnored" to "true")

            expect { true }
            whenever {
                generator.generate(resolver, codeGenerator, opts)
                // builderGenerator should NOT have been called
                try {
                    verify(exactly = 0) { builderGenerator.generate(any(), any(), any(), any(), any()) }
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }
    }

    @Test
    fun `generate skips rootDslAccessorGenerator when no root classes`() = test {
        given {
            val resolver: Resolver = mockk()
            val codeGenerator: CodeGenerator = mockk()
            val builderGenerator: DefaultBuilderGenerator = mockk(relaxed = true)
            val rootGenerator = mockk<DefaultRootDslAccessorGenerator>(relaxed = true)
            val generator = DefaultDslGenerator(
                builderGenerator = builderGenerator,
                rootDslAccessorGenerator = rootGenerator
            )

            // No @GeneratedDsl classes found
            every {
                resolver.getSymbolsWithAnnotation(GeneratedDsl::class.qualifiedName!!)
            } returns emptySequence()
            every {
                resolver.getSymbolsWithAnnotation(SingleEntryTransformDsl::class.qualifiedName!!)
            } returns emptySequence()

            expect { true }
            whenever {
                generator.generate(resolver, codeGenerator, options())
                try {
                    verify(exactly = 0) { rootGenerator.generate(any(), any(), any(), any()) }
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }
    }

    @Test
    fun `generate calls builderGenerator for non-root classes and skips root accessor`() = test {
        given {
            val resolver: Resolver = mockk()
            val codeGenerator: CodeGenerator = mockk()
            val builderGenerator: DefaultBuilderGenerator = mockk(relaxed = true)
            val rootGenerator = mockk<DefaultRootDslAccessorGenerator>(relaxed = true)
            val generator = DefaultDslGenerator(
                builderGenerator = builderGenerator,
                rootDslAccessorGenerator = rootGenerator
            )

            // Class with debug=false (non-root, no @RootDsl)
            val nonRootClass = mockClassWithAnnotation(
                "debug" to false
            )

            every {
                resolver.getSymbolsWithAnnotation(GeneratedDsl::class.qualifiedName!!)
            } returns sequenceOf(nonRootClass)
            every {
                resolver.getSymbolsWithAnnotation(SingleEntryTransformDsl::class.qualifiedName!!)
            } returns emptySequence()

            expect { true }
            whenever {
                generator.generate(resolver, codeGenerator, options())
                try {
                    verify(exactly = 1) { builderGenerator.generate(any(), eq(nonRootClass), any(), any(), eq(false)) }
                    verify(exactly = 0) { rootGenerator.generate(any(), any(), any(), any()) }
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }
    }

    @Test
    fun `generate calls rootDslAccessorGenerator when root class is present`() = test {
        given {
            val resolver: Resolver = mockk()
            val codeGenerator: CodeGenerator = mockk()
            val builderGenerator: DefaultBuilderGenerator = mockk(relaxed = true)
            val rootGenerator = mockk<DefaultRootDslAccessorGenerator>(relaxed = true)
            val generator = DefaultDslGenerator(
                builderGenerator = builderGenerator,
                rootDslAccessorGenerator = rootGenerator
            )

            val rootClass = mockRootClassWithAnnotation(
                "debug" to true
            )

            every {
                resolver.getSymbolsWithAnnotation(GeneratedDsl::class.qualifiedName!!)
            } returns sequenceOf(rootClass)
            every {
                resolver.getSymbolsWithAnnotation(SingleEntryTransformDsl::class.qualifiedName!!)
            } returns emptySequence()

            expect { true }
            whenever {
                generator.generate(resolver, codeGenerator, options())
                try {
                    verify(exactly = 1) { builderGenerator.generate(any(), eq(rootClass), any(), any(), eq(true)) }
                    verify(exactly = 1) { rootGenerator.generate(any(), match { it.any { triple -> triple.first == rootClass } }, any(), any()) }
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }
    }

    @Test
    fun `generate with mix of root and non-root classes`() = test {
        given {
            val resolver: Resolver = mockk()
            val codeGenerator: CodeGenerator = mockk()
            val builderGenerator: DefaultBuilderGenerator = mockk(relaxed = true)
            val rootGenerator = mockk<DefaultRootDslAccessorGenerator>(relaxed = true)
            val generator = DefaultDslGenerator(
                builderGenerator = builderGenerator,
                rootDslAccessorGenerator = rootGenerator
            )

            val rootClass = mockRootClassWithAnnotation("debug" to false)
            val nonRootClass = mockClassWithAnnotation("debug" to false)

            every {
                resolver.getSymbolsWithAnnotation(GeneratedDsl::class.qualifiedName!!)
            } returns sequenceOf(rootClass, nonRootClass)
            every {
                resolver.getSymbolsWithAnnotation(SingleEntryTransformDsl::class.qualifiedName!!)
            } returns emptySequence()

            expect { true }
            whenever {
                generator.generate(resolver, codeGenerator, options())
                try {
                    verify(exactly = 2) { builderGenerator.generate(any(), any(), any(), any(), any()) }
                    verify(exactly = 1) { rootGenerator.generate(any(), match { it.size == 1 }, any(), any()) }
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }
    }

    @Test
    fun `generate processes rootDslProperties from property-level RootDsl annotation`() = test {
        given {
            val resolver: Resolver = mockk()
            val codeGenerator: CodeGenerator = mockk()
            val builderGenerator: DefaultBuilderGenerator = mockk(relaxed = true)
            val rootGenerator = mockk<DefaultRootDslAccessorGenerator>(relaxed = true)
            val generator = DefaultDslGenerator(
                builderGenerator = builderGenerator,
                rootDslAccessorGenerator = rootGenerator
            )

            // Class without @RootDsl but has a property annotated with @RootDsl
            val cls = mockClassWithAnnotation("debug" to false)

            // Mock a property with @RootDsl annotation
            val propAnn: KSAnnotation = mockk()
            every { propAnn.shortName } returns mockKSName("RootDsl")
            every { propAnn.arguments } returns listOf<KSValueArgument>(
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("name")
                    every { arg.value } returns "customRoot"
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("alias")
                    every { arg.value } returns "rootAlias"
                }
            )

            val prop: com.google.devtools.ksp.symbol.KSPropertyDeclaration = mockk()
            every { prop.annotations } returns sequenceOf(propAnn)

            // Override getAllProperties to return the annotated property
            every { cls.getAllProperties() } returns sequenceOf(prop)

            every {
                resolver.getSymbolsWithAnnotation(GeneratedDsl::class.qualifiedName!!)
            } returns sequenceOf(cls)
            every {
                resolver.getSymbolsWithAnnotation(SingleEntryTransformDsl::class.qualifiedName!!)
            } returns emptySequence()

            expect { true }
            whenever {
                generator.generate(resolver, codeGenerator, options())
                try {
                    verify(exactly = 1) {
                        rootGenerator.generate(any(), any(), any(), match { it.isNotEmpty() })
                    }
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }
    }

    @Test
    fun `generate processes rootDslProperties with blank name and alias falls back to null`() = test {
        given {
            val resolver: Resolver = mockk()
            val codeGenerator: CodeGenerator = mockk()
            val builderGenerator: DefaultBuilderGenerator = mockk(relaxed = true)
            val rootGenerator = mockk<DefaultRootDslAccessorGenerator>(relaxed = true)
            val generator = DefaultDslGenerator(
                builderGenerator = builderGenerator,
                rootDslAccessorGenerator = rootGenerator
            )

            val cls = mockClassWithAnnotation("debug" to false)

            // Mock a property with @RootDsl annotation with blank name and alias
            val propAnn: KSAnnotation = mockk()
            every { propAnn.shortName } returns mockKSName("RootDsl")
            every { propAnn.arguments } returns listOf<KSValueArgument>(
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("name")
                    every { arg.value } returns "  "
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("alias")
                    every { arg.value } returns ""
                }
            )

            val prop: com.google.devtools.ksp.symbol.KSPropertyDeclaration = mockk()
            every { prop.annotations } returns sequenceOf(propAnn)
            every { cls.getAllProperties() } returns sequenceOf(prop)

            every {
                resolver.getSymbolsWithAnnotation(GeneratedDsl::class.qualifiedName!!)
            } returns sequenceOf(cls)
            every {
                resolver.getSymbolsWithAnnotation(SingleEntryTransformDsl::class.qualifiedName!!)
            } returns emptySequence()

            expect { true }
            whenever {
                generator.generate(resolver, codeGenerator, options())
                try {
                    verify(exactly = 1) {
                        rootGenerator.generate(any(), any(), any(), match { props ->
                            props.isNotEmpty() &&
                                props.first().second == null &&
                                props.first().third == null
                        })
                    }
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }
    }

    @Test
    fun `generate detects class-level RootDsl annotation as root class with name and alias`() = test {
        given {
            val resolver: Resolver = mockk()
            val codeGenerator: CodeGenerator = mockk()
            val builderGenerator: DefaultBuilderGenerator = mockk(relaxed = true)
            val rootGenerator = mockk<DefaultRootDslAccessorGenerator>(relaxed = true)
            val generator = DefaultDslGenerator(
                builderGenerator = builderGenerator,
                rootDslAccessorGenerator = rootGenerator
            )

            // Class with @GeneratedDsl and @RootDsl(name="vessel", alias="ship")
            val cls: KSClassDeclaration = mockk(relaxed = true)

            val generatedDslAnn: KSAnnotation = mockk()
            every { generatedDslAnn.shortName } returns mockKSName("GeneratedDsl")
            every { generatedDslAnn.arguments } returns listOf<KSValueArgument>(
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("debug")
                    every { arg.value } returns false
                }
            )

            val rootDslAnn: KSAnnotation = mockk()
            every { rootDslAnn.shortName } returns mockKSName("RootDsl")
            every { rootDslAnn.arguments } returns listOf<KSValueArgument>(
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("name")
                    every { arg.value } returns "vessel"
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("alias")
                    every { arg.value } returns "ship"
                }
            )

            every { cls.annotations } returns sequenceOf(generatedDslAnn, rootDslAnn)
            every { cls.getAllProperties() } returns emptySequence()

            every {
                resolver.getSymbolsWithAnnotation(GeneratedDsl::class.qualifiedName!!)
            } returns sequenceOf(cls)
            every {
                resolver.getSymbolsWithAnnotation(SingleEntryTransformDsl::class.qualifiedName!!)
            } returns emptySequence()

            expect { true }
            whenever {
                generator.generate(resolver, codeGenerator, options())
                try {
                    verify(exactly = 1) {
                        rootGenerator.generate(any(), match { domains ->
                            domains.size == 1 &&
                                domains.first().second == "vessel" &&
                                domains.first().third == "ship"
                        }, any(), any())
                    }
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }
    }

    @Test
    fun `generate class-level RootDsl with no name alias passes nulls`() = test {
        given {
            val resolver: Resolver = mockk()
            val codeGenerator: CodeGenerator = mockk()
            val builderGenerator: DefaultBuilderGenerator = mockk(relaxed = true)
            val rootGenerator = mockk<DefaultRootDslAccessorGenerator>(relaxed = true)
            val generator = DefaultDslGenerator(
                builderGenerator = builderGenerator,
                rootDslAccessorGenerator = rootGenerator
            )

            // Class with @GeneratedDsl and @RootDsl with empty arguments
            val cls: KSClassDeclaration = mockk(relaxed = true)
            val generatedDslAnn: KSAnnotation = mockk()
            every { generatedDslAnn.shortName } returns mockKSName("GeneratedDsl")
            every { generatedDslAnn.arguments } returns listOf<KSValueArgument>(
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("debug")
                    every { arg.value } returns false
                }
            )
            val rootDslAnn: KSAnnotation = mockk()
            every { rootDslAnn.shortName } returns mockKSName("RootDsl")
            every { rootDslAnn.arguments } returns emptyList()

            every { cls.annotations } returns sequenceOf(generatedDslAnn, rootDslAnn)
            every { cls.getAllProperties() } returns emptySequence()

            every {
                resolver.getSymbolsWithAnnotation(GeneratedDsl::class.qualifiedName!!)
            } returns sequenceOf(cls)
            every {
                resolver.getSymbolsWithAnnotation(SingleEntryTransformDsl::class.qualifiedName!!)
            } returns emptySequence()

            expect { true }
            whenever {
                generator.generate(resolver, codeGenerator, options())
                try {
                    verify(exactly = 1) {
                        rootGenerator.generate(any(), match { domains ->
                            domains.size == 1 &&
                                domains.first().second == null &&
                                domains.first().third == null
                        }, any(), any())
                    }
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }
    }

    @Test
    fun `generate class-level RootDsl with blank name alias passes nulls`() = test {
        given {
            val resolver: Resolver = mockk()
            val codeGenerator: CodeGenerator = mockk()
            val builderGenerator: DefaultBuilderGenerator = mockk(relaxed = true)
            val rootGenerator = mockk<DefaultRootDslAccessorGenerator>(relaxed = true)
            val generator = DefaultDslGenerator(
                builderGenerator = builderGenerator,
                rootDslAccessorGenerator = rootGenerator
            )

            val cls: KSClassDeclaration = mockk(relaxed = true)
            val generatedDslAnn: KSAnnotation = mockk()
            every { generatedDslAnn.shortName } returns mockKSName("GeneratedDsl")
            every { generatedDslAnn.arguments } returns listOf<KSValueArgument>(
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("debug")
                    every { arg.value } returns false
                }
            )
            val rootDslAnn: KSAnnotation = mockk()
            every { rootDslAnn.shortName } returns mockKSName("RootDsl")
            every { rootDslAnn.arguments } returns listOf<KSValueArgument>(
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("name")
                    every { arg.value } returns "  "
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("alias")
                    every { arg.value } returns ""
                }
            )

            every { cls.annotations } returns sequenceOf(generatedDslAnn, rootDslAnn)
            every { cls.getAllProperties() } returns emptySequence()

            every {
                resolver.getSymbolsWithAnnotation(GeneratedDsl::class.qualifiedName!!)
            } returns sequenceOf(cls)
            every {
                resolver.getSymbolsWithAnnotation(SingleEntryTransformDsl::class.qualifiedName!!)
            } returns emptySequence()

            expect { true }
            whenever {
                generator.generate(resolver, codeGenerator, options())
                try {
                    verify(exactly = 1) {
                        rootGenerator.generate(any(), match { domains ->
                            domains.size == 1 &&
                                domains.first().second == null &&
                                domains.first().third == null
                        }, any(), any())
                    }
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }
    }

    @Test
    fun `generate filters out properties without RootDsl annotation`() = test {
        given {
            val resolver: Resolver = mockk()
            val codeGenerator: CodeGenerator = mockk()
            val builderGenerator: DefaultBuilderGenerator = mockk(relaxed = true)
            val rootGenerator = mockk<DefaultRootDslAccessorGenerator>(relaxed = true)
            val generator = DefaultDslGenerator(
                builderGenerator = builderGenerator,
                rootDslAccessorGenerator = rootGenerator
            )

            val rootClass = mockRootClassWithAnnotation("debug" to false)

            // Add a property WITHOUT @RootDsl (just a normal annotation)
            val normalProp: com.google.devtools.ksp.symbol.KSPropertyDeclaration = mockk()
            val normalAnn: KSAnnotation = mockk()
            every { normalAnn.shortName } returns mockKSName("SomeOtherAnnotation")
            every { normalProp.annotations } returns sequenceOf(normalAnn)
            every { rootClass.getAllProperties() } returns sequenceOf(normalProp)

            every {
                resolver.getSymbolsWithAnnotation(GeneratedDsl::class.qualifiedName!!)
            } returns sequenceOf(rootClass)
            every {
                resolver.getSymbolsWithAnnotation(SingleEntryTransformDsl::class.qualifiedName!!)
            } returns emptySequence()

            expect { true }
            whenever {
                generator.generate(resolver, codeGenerator, options())
                try {
                    verify(exactly = 1) {
                        rootGenerator.generate(any(), any(), any(), match { it.isEmpty() })
                    }
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }
    }

    @Test
    fun `generate processes SingleEntryTransformDsl annotations`() = test {
        given {
            val resolver: Resolver = mockk()
            val codeGenerator: CodeGenerator = mockk()
            val builderGenerator: DefaultBuilderGenerator = mockk(relaxed = true)
            val rootGenerator = mockk<DefaultRootDslAccessorGenerator>(relaxed = true)
            val generator = DefaultDslGenerator(
                builderGenerator = builderGenerator,
                rootDslAccessorGenerator = rootGenerator
            )

            val nonRootClass = mockClassWithAnnotation("debug" to false)

            // SingleEntryTransform class
            val transformClass: KSClassDeclaration = mockk()
            every { transformClass.simpleName } returns mockKSName("MyTransform")
            every { transformClass.toClassName() } returns com.squareup.kotlinpoet.ClassName("org.test", "MyTransform")

            every {
                resolver.getSymbolsWithAnnotation(GeneratedDsl::class.qualifiedName!!)
            } returns sequenceOf(nonRootClass)
            every {
                resolver.getSymbolsWithAnnotation(SingleEntryTransformDsl::class.qualifiedName!!)
            } returns sequenceOf(transformClass)

            expect { true }
            whenever {
                generator.generate(resolver, codeGenerator, options())
                try {
                    verify(exactly = 1) {
                        builderGenerator.generate(any(), any(), any(), match {
                            it.containsKey("org.test.MyTransform") }, any())
                    }
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }
    }
}
