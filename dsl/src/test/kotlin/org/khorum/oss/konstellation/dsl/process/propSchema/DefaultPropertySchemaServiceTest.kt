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
import org.khorum.oss.konstellation.dsl.utils.VLoggable
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

        private fun mockDomainConfig(
            properties: Sequence<KSPropertyDeclaration>,
            debug: Boolean = false
        ): DomainConfig {
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
            return DomainConfig(config, emptyMap(), domain, debug)
        }

        private fun mockSimpleProp(name: String = "testProp"): KSPropertyDeclaration {
            return mockPropWithAnnotations(name, emptySequence())
        }

        private fun mockDefaultValueAnnotation(args: List<Pair<String, Any?>>): KSAnnotation {
            val annTypeRef: KSTypeReference = mockk()
            val annResolvedType: KSType = mockk()
            val annDecl: KSClassDeclaration = mockk()
            val annQualName: KSName = mockk()
            every { annQualName.asString() } returns "org.khorum.oss.konstellation.metaDsl.annotation.DefaultValue"
            every { annDecl.qualifiedName } returns annQualName
            every { annResolvedType.declaration } returns annDecl
            every { annTypeRef.resolve() } returns annResolvedType

            val ann: KSAnnotation = mockk()
            every { ann.annotationType } returns annTypeRef
            val shortName: KSName = mockk()
            every { shortName.asString() } returns "DefaultValue"
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

        private fun mockPropWithDefaultValue(
            rawValue: String,
            packageName: String,
            className: String
        ): KSPropertyDeclaration {
            val ann = mockDefaultValueAnnotation(
                listOf("value" to rawValue, "packageName" to packageName, "className" to className)
            )
            return mockPropWithAnnotations("testProp", sequenceOf(ann))
        }

        private fun mockPropWithPartialDefaultValue(
            rawValue: String?,
            packageName: String?,
            className: String?
        ): KSPropertyDeclaration {
            val args = mutableListOf<Pair<String, Any?>>()
            if (rawValue != null) args.add("value" to rawValue)
            if (packageName != null) args.add("packageName" to packageName)
            if (className != null) args.add("className" to className)
            val ann = mockDefaultValueAnnotation(args)
            return mockPropWithAnnotations("testProp", sequenceOf(ann))
        }

        private fun mockPropWithAnnotations(
            name: String,
            annotations: Sequence<KSAnnotation>
        ): KSPropertyDeclaration {
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
            every { prop.annotations } returns annotations
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

    @Test
    fun `getParamsFromDomain extracts DefaultValue annotation for String class`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val prop = mockPropWithDefaultValue("hello", "kotlin", "String")
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val schemas = service.getParamsFromDomain(domainConfig)
                schemas.first().defaultValue != null
            }
        }
    }

    @Test
    fun `getParamsFromDomain extracts DefaultValue annotation for non-String class`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val prop = mockPropWithDefaultValue("42", "kotlin", "Int")
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val schemas = service.getParamsFromDomain(domainConfig)
                val dv = schemas.first().defaultValue
                dv != null && dv.rawValue == "42"
            }
        }
    }

    @Test
    fun `getParamsFromDomain returns null default value when annotation value is missing`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val prop = mockPropWithPartialDefaultValue(rawValue = null, packageName = "kotlin", className = "String")
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue }
        }
    }

    @Test
    fun `getParamsFromDomain returns null default value when packageName is missing`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val prop = mockPropWithPartialDefaultValue(rawValue = "hello", packageName = null, className = "String")
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue }
        }
    }

    @Test
    fun `getParamsFromDomain returns null default value when className is missing`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val prop = mockPropWithPartialDefaultValue(rawValue = "hello", packageName = "kotlin", className = null)
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue }
        }
    }

    @Test
    fun `getParamsFromDomain returns null default value when no DefaultValue annotation`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val domainConfig = mockDomainConfig(sequenceOf(mockSimpleProp("noDefault")))

            expect { null }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue }
        }
    }

    @Test
    fun `getParamsFromDomain with debug enabled exercises logging branches for String default`() = test {
        given {
            val service = DefaultPropertySchemaService()
            service.logger.enableDebug()
            val prop = mockPropWithDefaultValue("hello", "kotlin", "String")
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                try {
                    val schemas = service.getParamsFromDomain(domainConfig)
                    schemas.first().defaultValue != null
                } finally {
                    service.logger.disableDebug()
                }
            }
        }
    }

    @Test
    fun `getParamsFromDomain with debug enabled for non-String default`() = test {
        given {
            val service = DefaultPropertySchemaService()
            service.logger.enableDebug()
            val prop = mockPropWithDefaultValue("42", "kotlin", "Int")
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                try {
                    val schemas = service.getParamsFromDomain(domainConfig)
                    schemas.first().defaultValue != null
                } finally {
                    service.logger.disableDebug()
                }
            }
        }
    }

    @Test
    fun `getParamsFromDomain with debug enabled and no default value`() = test {
        given {
            val service = DefaultPropertySchemaService()
            service.logger.enableDebug()
            val domainConfig = mockDomainConfig(sequenceOf(mockSimpleProp("field")))

            expect { true }
            whenever {
                try {
                    service.getParamsFromDomain(domainConfig).first().defaultValue == null
                } finally {
                    service.logger.disableDebug()
                }
            }
        }
    }
}
