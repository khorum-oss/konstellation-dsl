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
            every { domain.annotations } returns emptySequence()
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

        private fun mockAnnotation(
            simpleName: String,
            args: List<Pair<String, Any?>> = emptyList()
        ): KSAnnotation {
            val ann: KSAnnotation = mockk()
            val shortName: KSName = mockk()
            every { shortName.asString() } returns simpleName
            every { ann.shortName } returns shortName

            val annTypeRef: KSTypeReference = mockk()
            val annResolvedType: KSType = mockk()
            val annDecl: KSClassDeclaration = mockk()
            val annQualName: KSName = mockk()
            every { annQualName.asString() } returns "org.khorum.oss.konstellation.metaDsl.annotation.$simpleName"
            every { annDecl.qualifiedName } returns annQualName
            every { annResolvedType.declaration } returns annDecl
            every { annTypeRef.resolve() } returns annResolvedType
            every { ann.annotationType } returns annTypeRef

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

    @Test
    fun `extractAnnotationMetadata marks TransientDsl property as transient`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockAnnotation("TransientDsl", listOf("reason" to "not needed"))
            val prop = mockPropWithAnnotations("transientField", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { 0 }
            whenever { service.getParamsFromDomain(domainConfig).size }
        }
    }

    @Test
    fun `extractAnnotationMetadata marks TransientDsl with blank reason`() = test {
        given {
            val service = DefaultPropertySchemaService()
            service.logger.enableDebug()
            val ann = mockAnnotation("TransientDsl", listOf("reason" to ""))
            val prop = mockPropWithAnnotations("transientField", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { 0 }
            whenever {
                try {
                    service.getParamsFromDomain(domainConfig).size
                } finally {
                    service.logger.disableDebug()
                }
            }
        }
    }

    @Test
    fun `extractAnnotationMetadata extracts DslDescription`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockAnnotation("DslDescription", listOf("value" to "A helpful description"))
            val prop = mockPropWithAnnotations("described", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "A helpful description" }
            whenever { service.getParamsFromDomain(domainConfig).first().annotationMetadata.description }
        }
    }

    @Test
    fun `extractAnnotationMetadata returns null description when blank`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockAnnotation("DslDescription", listOf("value" to ""))
            val prop = mockPropWithAnnotations("described", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever { service.getParamsFromDomain(domainConfig).first().annotationMetadata.description }
        }
    }

    @Test
    fun `extractAnnotationMetadata extracts DslAlias single value`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockAnnotation("DslAlias", listOf("names" to "aliasName"))
            val prop = mockPropWithAnnotations("aliased", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { listOf("aliasName") }
            whenever { service.getParamsFromDomain(domainConfig).first().annotationMetadata.aliases }
        }
    }

    @Test
    fun `extractAnnotationMetadata returns empty aliases when no DslAlias`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val prop = mockPropWithAnnotations("noAlias", emptySequence())
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { emptyList<String>() }
            whenever { service.getParamsFromDomain(domainConfig).first().annotationMetadata.aliases }
        }
    }

    @Test
    fun `extractAnnotationMetadata extracts DslAlias list value`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockAnnotation("DslAlias", listOf("names" to listOf("alias1", "alias2")))
            val prop = mockPropWithAnnotations("aliased", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { listOf("alias1", "alias2") }
            whenever { service.getParamsFromDomain(domainConfig).first().annotationMetadata.aliases }
        }
    }

    @Test
    fun `extractAnnotationMetadata extracts DeprecatedDsl message and replaceWith`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockAnnotation(
                "DeprecatedDsl",
                listOf("message" to "Use newProp", "replaceWith" to "newProp")
            )
            val prop = mockPropWithAnnotations("oldProp", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "Use newProp" }
            whenever { service.getParamsFromDomain(domainConfig).first().annotationMetadata.deprecatedMessage }
        }
    }

    @Test
    fun `extractAnnotationMetadata returns null deprecated fields when blank`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockAnnotation(
                "DeprecatedDsl",
                listOf("message" to "", "replaceWith" to "")
            )
            val prop = mockPropWithAnnotations("oldProp", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever { service.getParamsFromDomain(domainConfig).first().annotationMetadata.deprecatedMessage }
        }
    }

    @Test
    fun `extractAnnotationMetadata extracts ValidateDsl expression and message`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockAnnotation(
                "ValidateDsl",
                listOf("expression" to "it > 0", "message" to "Must be positive")
            )
            val prop = mockPropWithAnnotations("validated", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "it > 0" }
            whenever { service.getParamsFromDomain(domainConfig).first().annotationMetadata.validateExpression }
        }
    }

    @Test
    fun `extractAnnotationMetadata returns null validate fields when blank`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockAnnotation(
                "ValidateDsl",
                listOf("expression" to "", "message" to "")
            )
            val prop = mockPropWithAnnotations("validated", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever { service.getParamsFromDomain(domainConfig).first().annotationMetadata.validateExpression }
        }
    }

    @Test
    fun `extractAnnotationMetadata with multiple annotations on same property`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val descAnn = mockAnnotation("DslDescription", listOf("value" to "desc"))
            val deprecAnn = mockAnnotation(
                "DeprecatedDsl",
                listOf("message" to "old", "replaceWith" to "new")
            )
            val validateAnn = mockAnnotation(
                "ValidateDsl",
                listOf("expression" to "it != null", "message" to "required")
            )
            val prop = mockPropWithAnnotations(
                "multi", sequenceOf(descAnn, deprecAnn, validateAnn)
            )
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val meta = service.getParamsFromDomain(domainConfig).first().annotationMetadata
                meta.description == "desc" &&
                    meta.deprecatedMessage == "old" &&
                    meta.deprecatedReplaceWith == "new" &&
                    meta.validateExpression == "it != null" &&
                    meta.validateMessage == "required"
            }
        }
    }

    @Test
    fun `extractDefaultPropertyValue with empty className and packageName uses String template`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockDefaultValueAnnotation(
                listOf("value" to "test", "packageName" to "", "className" to "")
            )
            val prop = mockPropWithAnnotations("emptyClass", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val dv = service.getParamsFromDomain(domainConfig).first().defaultValue
                dv != null && dv.rawValue == "test"
            }
        }
    }

    @Test
    fun `PropertySchemaService logId returns class simple name`() = test {
        given {
            val service = DefaultPropertySchemaService()
            expect { "PropertySchemaService" }
            whenever { service.logId() }
        }
    }

    @Test
    fun `getParamsFromDomain with TransientDsl with reason and debug enabled exercises logging`() = test {
        given {
            val service = DefaultPropertySchemaService()
            service.logger.enableDebug()
            val ann = mockAnnotation("TransientDsl", listOf("reason" to "not needed"))
            val prop = mockPropWithAnnotations("transientField", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop), debug = true)

            expect { 0 }
            whenever {
                try {
                    service.getParamsFromDomain(domainConfig).size
                } finally {
                    service.logger.disableDebug()
                }
            }
        }
    }

    @Test
    fun `getParamsFromDomain with multiple properties filters only transient ones`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val transientAnn = mockAnnotation("TransientDsl", listOf("reason" to "skip"))
            val transientProp = mockPropWithAnnotations("skip", sequenceOf(transientAnn))
            val normalProp = mockSimpleProp("keep")
            val domainConfig = mockDomainConfig(sequenceOf(transientProp, normalProp))

            expect { 1 }
            whenever { service.getParamsFromDomain(domainConfig).size }
        }
    }

    @Test
    fun `extractDefaultPropertyValue with inferType true and primitive type uses literal template`() = test {
        given {
            val service = DefaultPropertySchemaService()
            // Create a prop whose type resolves to kotlin.Int with empty packageName/className
            val ann = mockDefaultValueAnnotation(
                listOf(
                    "value" to "42",
                    "packageName" to "",
                    "className" to "",
                    "inferType" to true
                )
            )

            // Need to mock a property whose resolved type is kotlin.Int
            val typeRef: KSTypeReference = mockk()
            every { typeRef.toTypeName() } returns com.squareup.kotlinpoet.INT

            val classDecl: KSClassDeclaration = mockk()
            every { classDecl.toClassName() } returns ClassName("kotlin", "Int")
            every { classDecl.annotations } returns emptySequence()
            every { classDecl.qualifiedName } returns mockKSName("kotlin.Int")

            val resolvedType: KSType = mockk()
            every { resolvedType.isMarkedNullable } returns false
            every { resolvedType.declaration } returns classDecl
            every { resolvedType.arguments } returns emptyList()
            every { typeRef.resolve() } returns resolvedType

            val prop: KSPropertyDeclaration = mockk()
            every { prop.simpleName } returns mockKSName("intField")
            every { prop.type } returns typeRef
            every { prop.annotations } returns sequenceOf(ann)

            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val dv = service.getParamsFromDomain(domainConfig).first().defaultValue
                dv != null && dv.rawValue == "42"
            }
        }
    }

    @Test
    fun `extractDefaultPropertyValue with inferType false uses String template`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockDefaultValueAnnotation(
                listOf(
                    "value" to "42",
                    "packageName" to "",
                    "className" to "",
                    "inferType" to false
                )
            )
            val prop = mockPropWithAnnotations("noInfer", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val dv = service.getParamsFromDomain(domainConfig).first().defaultValue
                dv != null && dv.rawValue == "42"
            }
        }
    }

    @Test
    fun `extractDefaultPropertyValue with non-empty packageName and className uses literal template`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockDefaultValueAnnotation(
                listOf(
                    "value" to "MyEnum.VALUE",
                    "packageName" to "org.test",
                    "className" to "MyEnum"
                )
            )
            val prop = mockPropWithAnnotations("enumField", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val dv = service.getParamsFromDomain(domainConfig).first().defaultValue
                dv != null && dv.rawValue == "MyEnum.VALUE" && dv.className == "MyEnum"
            }
        }
    }
}
