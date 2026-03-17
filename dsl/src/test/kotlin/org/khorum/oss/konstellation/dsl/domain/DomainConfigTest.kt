package org.khorum.oss.konstellation.dsl.domain

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.utils.Logger
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DomainConfigTest : UnitSim() {
    private val mockDomain: KSClassDeclaration = mockk()
    private val mockFile: KSFile = mockk()

    @BeforeEach
    fun setup() {
        mockkStatic(KSClassDeclaration::toClassName)

        val pkgName: KSName = mockk()
        every { pkgName.asString() } returns "org.khorum.oss.test"
        every { mockDomain.packageName } returns pkgName

        val simpleName: KSName = mockk()
        every { simpleName.asString() } returns "StarShip"
        every { mockDomain.simpleName } returns simpleName

        every { mockDomain.toClassName() } returns ClassName("org.khorum.oss.test", "StarShip")
        every { mockDomain.containingFile } returns mockFile
    }

    @AfterEach
    fun teardown() {
        unmockkStatic(KSClassDeclaration::toClassName)
    }

    private fun builderConfig(): BuilderConfig {
        val logger = Logger("DomainConfigTest")
        return BuilderConfig(
            mapOf(
                "projectRootClasspath" to "org.khorum.oss.test",
                "dslBuilderClasspath" to "org.khorum.oss.test"
            ),
            logger
        )
    }

    @Test
    fun `packageName is derived from domain`() = test {
        given {
            expect { "org.khorum.oss.test" }
            whenever { DomainConfig(builderConfig(), emptyMap(), mockDomain, false).packageName }
        }
    }

    @Test
    fun `typeName is derived from domain simpleName`() = test {
        given {
            expect { "StarShip" }
            whenever { DomainConfig(builderConfig(), emptyMap(), mockDomain, false).typeName }
        }
    }

    @Test
    fun `builderName appends DslBuilder postfix`() = test {
        given {
            expect { "StarShipDslBuilder" }
            whenever { DomainConfig(builderConfig(), emptyMap(), mockDomain, false).builderName }
        }
    }

    @Test
    fun `builderClassName has correct package and name`() = test {
        given {
            expect { ClassName("org.khorum.oss.test", "StarShipDslBuilder") }
            whenever { DomainConfig(builderConfig(), emptyMap(), mockDomain, false).builderClassName }
        }
    }

    @Test
    fun `fileClassName has correct package and Dsl postfix`() = test {
        given {
            expect { ClassName("org.khorum.oss.test", "StarShipDsl") }
            whenever { DomainConfig(builderConfig(), emptyMap(), mockDomain, false).fileClassName }
        }
    }

    @Test
    fun `domainClassName is derived from domain toClassName`() = test {
        given {
            expect { ClassName("org.khorum.oss.test", "StarShip") }
            whenever { DomainConfig(builderConfig(), emptyMap(), mockDomain, false).domainClassName }
        }
    }

    @Test
    fun `dependencies includes containingFile`() = test {
        given {
            expect { 1 }
            whenever { DomainConfig(builderConfig(), emptyMap(), mockDomain, false).dependencies.originatingFiles.size }
        }
    }

}
