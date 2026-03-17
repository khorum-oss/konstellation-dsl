package org.khorum.oss.konstellation.dsl.process.generator

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSValueArgument
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.builder.KPTypeSpecBuilder
import org.khorum.oss.konstellation.dsl.domain.BuilderConfig
import org.khorum.oss.konstellation.dsl.domain.DomainConfig
import org.khorum.oss.konstellation.dsl.utils.Logger
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class GroupGeneratorTest : UnitSim() {

    companion object {
        private lateinit var domainWithListGroup: KSClassDeclaration
        private lateinit var domainWithoutListGroup: KSClassDeclaration
        private lateinit var domainWithMapSingle: KSClassDeclaration
        private lateinit var domainWithMapNone: KSClassDeclaration
        private lateinit var config: BuilderConfig

        private fun mockAnnotation(args: Map<String, Any>): KSAnnotation {
            val ann: KSAnnotation = mockk()
            val shortName: KSName = mockk()
            every { shortName.asString() } returns "GeneratedDsl"
            every { ann.shortName } returns shortName
            every { ann.arguments } returns args.map { (k, v) ->
                val argName: KSName = mockk()
                every { argName.asString() } returns k
                val arg: KSValueArgument = mockk()
                every { arg.name } returns argName
                every { arg.value } returns v
                arg
            }
            return ann
        }

        private fun makeDomain(annotations: List<KSAnnotation>): KSClassDeclaration {
            val domain: KSClassDeclaration = mockk()
            every { domain.annotations } returns annotations.asSequence()
            every { domain.toClassName() } returns ClassName("org.test", "StarShip")
            val pkgName: KSName = mockk()
            every { pkgName.asString() } returns "org.test"
            every { domain.packageName } returns pkgName
            val simpleName: KSName = mockk()
            every { simpleName.asString() } returns "StarShip"
            every { domain.simpleName } returns simpleName
            val file: KSFile = mockk()
            every { domain.containingFile } returns file
            return domain
        }

        @JvmStatic
        @BeforeAll
        fun setupAll() {
            mockkStatic(KSClassDeclaration::toClassName)
            config = BuilderConfig(
                mapOf("projectRootClasspath" to "org.test", "dslBuilderClasspath" to "org.test"),
                Logger("GroupGeneratorTest")
            )
            domainWithListGroup = makeDomain(listOf(mockAnnotation(mapOf("withListGroup" to true))))
            domainWithoutListGroup = makeDomain(listOf(mockAnnotation(mapOf("withListGroup" to false))))
            domainWithMapSingle = makeDomain(listOf(mockAnnotation(mapOf("withMapGroup" to "SINGLE"))))
            domainWithMapNone = makeDomain(listOf(mockAnnotation(mapOf("withMapGroup" to "NONE"))))
        }

        @JvmStatic
        @AfterAll
        fun teardownAll() {
            unmockkStatic(KSClassDeclaration::toClassName)
        }
    }

    @Test
    fun `ListGroupGenerator does not add nested type when isGroup is false`() = test {
        given {
            val builder = KPTypeSpecBuilder()
            builder.name = "TestBuilder"
            val dc = DomainConfig(config, emptyMap(), domainWithoutListGroup, false)

            expect { true }
            whenever {
                ListGroupGenerator().generate(builder, dc)
                !builder.build().toString().contains("class Group")
            }
        }
    }

    @Test
    fun `ListGroupGenerator adds nested Group type when isGroup is true`() = test {
        given {
            val builder = KPTypeSpecBuilder()
            builder.name = "TestBuilder"
            val dc = DomainConfig(config, emptyMap(), domainWithListGroup, false)

            expect { true }
            whenever {
                ListGroupGenerator().generate(builder, dc)
                builder.build().toString().contains("class Group")
            }
        }
    }

    @Test
    fun `MapGroupGenerator does not add nested type when withMapGroup is NONE`() = test {
        given {
            val builder = KPTypeSpecBuilder()
            builder.name = "TestBuilder"
            val dc = DomainConfig(config, emptyMap(), domainWithMapNone, false)

            expect { true }
            whenever {
                MapGroupGenerator().generate(builder, dc)
                !builder.build().toString().contains("class MapGroup")
            }
        }
    }

    @Test
    fun `MapGroupGenerator adds nested MapGroup type when withMapGroup is SINGLE`() = test {
        given {
            val builder = KPTypeSpecBuilder()
            builder.name = "TestBuilder"
            val dc = DomainConfig(config, emptyMap(), domainWithMapSingle, false)

            expect { true }
            whenever {
                MapGroupGenerator().generate(builder, dc)
                builder.build().toString().contains("class MapGroup")
            }
        }
    }
}
