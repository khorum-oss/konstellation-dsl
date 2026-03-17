package org.khorum.oss.konstellation.dsl.process.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import io.mockk.mockk
import io.mockk.verify
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.metaDsl.annotation.GeneratedDsl
import org.khorum.oss.konstellation.metaDsl.annotation.SingleEntryTransformDsl
import org.junit.jupiter.api.Test
import org.khorum.oss.konstellation.dsl.process.root.DefaultRootDslAccessorGenerator

class DefaultDslGeneratorTest : UnitSim() {

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
                    verify(exactly = 0) { rootGenerator.generate(any(), any(), any()) }
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }
    }
}
