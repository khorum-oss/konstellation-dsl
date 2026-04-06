package org.khorum.oss.konstellation.dsl.process.root

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.domain.BuilderConfig
import org.khorum.oss.konstellation.dsl.utils.Logger
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RootFunctionGeneratorTest : UnitSim() {
    private val generator = DefaultRootFunctionGenerator()
    private val domain: KSClassDeclaration = mockk()

    @BeforeEach
    fun setup() {
        mockkStatic(KSClassDeclaration::toClassName)
        every { domain.toClassName() } returns ClassName("org.test", "StarShip")
        val simpleName: KSName = mockk()
        every { simpleName.asString() } returns "StarShip"
        every { domain.simpleName } returns simpleName
        every { domain.docString } returns ""
    }

    @AfterEach
    fun teardown() {
        unmockkStatic(KSClassDeclaration::toClassName)
    }

    private fun builderConfig() = BuilderConfig(
        mapOf(
            "projectRootClasspath" to "org.test",
            "dslBuilderClasspath" to "org.test"
        ),
        Logger("RootFunctionGeneratorTest")
    )

    @Test
    fun `generate produces function with lowercase first char name`() = test {
        given {
            expect { "starShip" }
            whenever { generator.generate(domain, builderConfig()).name }
        }
    }

    @Test
    fun `generate function returns domain class type`() = test {
        given {
            expect { true }
            whenever { generator.generate(domain, builderConfig()).returnType.toString().contains("StarShip") }
        }
    }

    @Test
    fun `generate function has block parameter`() = test {
        given {
            expect { 1 }
            whenever { generator.generate(domain, builderConfig()).parameters.size }
        }
    }

    @Test
    fun `generate function body contains builder build`() = test {
        given {
            expect { true }
            whenever { generator.generate(domain, builderConfig()).body.toString().contains("builder.build()") }
        }
    }

    @Test
    fun `generate with customName uses custom name instead of class name`() = test {
        given {
            expect { true }
            whenever {
                // Call without customName first to satisfy @BeforeEach mock stubs
                generator.generate(domain, builderConfig())
                // Now verify customName overrides the generated function name
                generator.generate(domain, builderConfig(), "myCustomName").name == "myCustomName"
            }
        }
    }

    @Test
    fun `generate with null customName uses lowercase class name`() = test {
        given {
            expect { "starShip" }
            whenever { generator.generate(domain, builderConfig(), null).name }
        }
    }

    @Test
    fun `generate includes KDoc from domain docString`() = test {
        given {
            every { domain.docString } returns " A powerful starship\n"
            expect { true }
            whenever {
                generator.generate(domain, builderConfig()).toString().contains("A powerful starship")
            }
        }
    }

    @Test
    fun `generate without docString has no KDoc`() = test {
        given {
            every { domain.docString } returns ""
            expect { false }
            whenever {
                generator.generate(domain, builderConfig()).toString().contains("/**")
            }
        }
    }
}
