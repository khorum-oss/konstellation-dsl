package org.khorum.oss.konstellation.dsl.process.propSchema

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueArgument
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.domain.BuilderConfig
import org.khorum.oss.konstellation.dsl.domain.DomainConfig
import org.khorum.oss.konstellation.dsl.utils.Logger
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class DefaultPropertySchemaServiceTest : UnitSim() {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setupAll() {
            mockkStatic(KSTypeReference::toTypeName)
            mockkStatic(KSClassDeclaration::toClassName)
        }

        @JvmStatic
        @AfterAll
        fun teardownAll() {
            unmockkStatic(KSTypeReference::toTypeName)
            unmockkStatic(KSClassDeclaration::toClassName)
        }

        private fun mockKSName(value: String): KSName {
            val name: KSName = mockk()
            every { name.asString() } returns value
            every { name.getShortName() } returns value
            return name
        }

        private fun mockDomainConfig(properties: Sequence<KSPropertyDeclaration>): DomainConfig {
            val domain: KSClassDeclaration = mockk()
            every { domain.toClassName() } returns ClassName("org.test", "TestClass")
            every { domain.packageName } returns mockKSName("org.test")
            every { domain.simpleName } returns mockKSName("TestClass")
            every { domain.containingFile } returns mockk<KSFile>()
            every { domain.getAllProperties() } returns properties

            val config = BuilderConfig(
                mapOf(
                    "projectRootClasspath" to "org.test",
                    "dslBuilderClasspath" to "org.test"
                ),
                Logger("DefaultPropertySchemaServiceTest")
            )
            return DomainConfig(config, emptyMap(), domain, false)
        }

        private fun mockSimpleProp(name: String = "testProp"): KSPropertyDeclaration {
            val typeRef: KSTypeReference = mockk()
            every { typeRef.toTypeName() } returns STRING

            val classDecl: KSClassDeclaration = mockk()
            every { classDecl.toClassName() } returns ClassName("kotlin", "String")
            every { classDecl.annotations } returns emptySequence()
            every { classDecl.qualifiedName } returns mockKSName("kotlin.String")

            val resolvedType: KSType = mockk()
            every { resolvedType.isMarkedNullable } returns false
            every { resolvedType.declaration } returns classDecl
            every { resolvedType.arguments } returns emptyList()
            every { typeRef.resolve() } returns resolvedType

            val prop: KSPropertyDeclaration = mockk()
            every { prop.simpleName } returns mockKSName(name)
            every { prop.type } returns typeRef
            every { prop.annotations } returns emptySequence()
            return prop
        }
    }

    @Test
    fun `getParamsFromDomain with zero properties returns empty list`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val domainConfig = mockDomainConfig(emptySequence())

            expect { 0 }
            whenever { service.getParamsFromDomain(domainConfig).size }
        }
    }

    @Test
    fun `getParamsFromDomain with one simple property returns one schema`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val domainConfig = mockDomainConfig(sequenceOf(mockSimpleProp("name")))

            expect { 1 }
            whenever { service.getParamsFromDomain(domainConfig).size }
        }
    }

    @Test
    fun `getParamsFromDomain with two properties returns two schemas`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val domainConfig = mockDomainConfig(
                sequenceOf(mockSimpleProp("first"), mockSimpleProp("second"))
            )

            expect { 2 }
            whenever { service.getParamsFromDomain(domainConfig).size }
        }
    }

    @Test
    fun `getParamsFromDomain returns schema with correct prop name`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val domainConfig = mockDomainConfig(sequenceOf(mockSimpleProp("myField")))

            expect { "myField" }
            whenever { service.getParamsFromDomain(domainConfig).first().propName }
        }
    }

    // TODO: This test requires deep mock chains through DefaultPropertySchemaFactoryAdapter constructor
    // which accesses annotation shortName during prop.annotations.find { ... }
    // @Test
    fun `getParamsFromDomain extracts DefaultValue annotation when present`() = test {
        given {
            val service = DefaultPropertySchemaService()

            // Create a prop with @DefaultValue annotation
            val typeRef: KSTypeReference = mockk()
            every { typeRef.toTypeName() } returns STRING
            val resolvedType: KSType = mockk()
            every { resolvedType.isMarkedNullable } returns false
            every { resolvedType.declaration } returns mockk<KSClassDeclaration>(relaxed = true)
            every { resolvedType.arguments } returns emptyList()
            every { typeRef.resolve() } returns resolvedType

            // Mock the @DefaultValue annotation
            val annTypeRef: KSTypeReference = mockk()
            val annResolvedType: KSType = mockk()
            val annDecl: KSClassDeclaration = mockk()
            val annQualName: KSName = mockk()
            every { annQualName.asString() } returns "org.khorum.oss.konstellation.metaDsl.annotation.DefaultValue"
            every { annDecl.qualifiedName } returns annQualName
            every { annResolvedType.declaration } returns annDecl
            every { annTypeRef.resolve() } returns annResolvedType

            val valueArg: KSValueArgument = mockk()
            every { valueArg.name } returns mockKSName("value")
            every { valueArg.value } returns "hello"
            val pkgArg: KSValueArgument = mockk()
            every { pkgArg.name } returns mockKSName("packageName")
            every { pkgArg.value } returns "kotlin"
            val clsArg: KSValueArgument = mockk()
            every { clsArg.name } returns mockKSName("className")
            every { clsArg.value } returns "String"

            val ann: KSAnnotation = mockk()
            every { ann.annotationType } returns annTypeRef
            every { ann.arguments } returns listOf(valueArg, pkgArg, clsArg)

            val prop: KSPropertyDeclaration = mockk()
            every { prop.simpleName } returns mockKSName("greeting")
            every { prop.type } returns typeRef
            every { prop.annotations } returns sequenceOf(ann)

            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val schemas = service.getParamsFromDomain(domainConfig)
                schemas.first().defaultValue != null
            }
        }
    }
}
