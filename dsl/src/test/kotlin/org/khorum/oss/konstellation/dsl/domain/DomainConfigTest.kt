package org.khorum.oss.konstellation.dsl.domain

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSValueArgument
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
        every { mockDomain.annotations } returns emptySequence()
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

    @Test
    fun `customName overrides builderName when GeneratedDsl name is set`() = test {
        given {
            val ann = mockGeneratedDslAnnotation("CustomShip")
            every { mockDomain.annotations } returns sequenceOf(ann)

            expect { "CustomShipDslBuilder" }
            whenever { DomainConfig(builderConfig(), emptyMap(), mockDomain, false).builderName }
        }
    }

    @Test
    fun `customName is null when GeneratedDsl name is blank`() = test {
        given {
            val ann = mockGeneratedDslAnnotation("")
            every { mockDomain.annotations } returns sequenceOf(ann)

            expect { "StarShipDslBuilder" }
            whenever { DomainConfig(builderConfig(), emptyMap(), mockDomain, false).builderName }
        }
    }

    @Test
    fun `fileClassName uses customName when GeneratedDsl name is set`() = test {
        given {
            val ann = mockGeneratedDslAnnotation("CustomShip")
            every { mockDomain.annotations } returns sequenceOf(ann)

            expect { ClassName("org.khorum.oss.test", "CustomShipDsl") }
            whenever { DomainConfig(builderConfig(), emptyMap(), mockDomain, false).fileClassName }
        }
    }

    private fun mockGeneratedDslAnnotation(name: String): KSAnnotation {
        val ann: KSAnnotation = mockk()
        val shortName: KSName = mockk()
        every { shortName.asString() } returns "GeneratedDsl"
        every { ann.shortName } returns shortName

        val argName: KSName = mockk()
        every { argName.asString() } returns "name"
        val arg: KSValueArgument = mockk()
        every { arg.name } returns argName
        every { arg.value } returns name

        every { ann.arguments } returns listOf(arg)
        return ann
    }
}
