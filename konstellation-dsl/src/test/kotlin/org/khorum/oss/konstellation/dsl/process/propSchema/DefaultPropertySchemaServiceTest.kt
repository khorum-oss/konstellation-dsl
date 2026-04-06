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
            every { domain.declarations } returns emptySequence()

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
            every { annQualName.asString() } returns "org.khorum.oss.konstellation.metaDsl.annotation.defaults.DefaultValue"
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

        /**
         * Mocks a @DefaultState annotation where the enum entry value is a KSClassDeclaration
         * (the actual KSP behavior for enum annotation values).
         */
        private fun mockDefaultStateAnnotation(enumEntryName: String): KSAnnotation {
            val annTypeRef: KSTypeReference = mockk()
            val annResolvedType: KSType = mockk()
            val annDecl: KSClassDeclaration = mockk()
            val annQualName: KSName = mockk()
            every { annQualName.asString() } returns "org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.DefaultState"
            every { annDecl.qualifiedName } returns annQualName
            every { annResolvedType.declaration } returns annDecl
            every { annTypeRef.resolve() } returns annResolvedType

            val ann: KSAnnotation = mockk()
            every { ann.annotationType } returns annTypeRef
            val shortName: KSName = mockk()
            every { shortName.asString() } returns "DefaultState"
            every { ann.shortName } returns shortName

            // KSP represents enum annotation values as KSClassDeclaration (enum entry)
            val enumEntryDecl: KSClassDeclaration = mockk()
            every { enumEntryDecl.simpleName } returns mockKSName(enumEntryName)

            every { ann.arguments } returns listOf(
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("type")
                    every { arg.value } returns enumEntryDecl
                }
            )
            return ann
        }

        /**
         * Mocks a @DefaultState annotation where the enum entry value is a KSType
         * (alternative KSP representation for some KSP implementations).
         */
        private fun mockDefaultStateAnnotationWithKSType(enumEntryName: String): KSAnnotation {
            val annTypeRef: KSTypeReference = mockk()
            val annResolvedType: KSType = mockk()
            val annDecl: KSClassDeclaration = mockk()
            val annQualName: KSName = mockk()
            every { annQualName.asString() } returns "org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.DefaultState"
            every { annDecl.qualifiedName } returns annQualName
            every { annResolvedType.declaration } returns annDecl
            every { annTypeRef.resolve() } returns annResolvedType

            val ann: KSAnnotation = mockk()
            every { ann.annotationType } returns annTypeRef
            val shortName: KSName = mockk()
            every { shortName.asString() } returns "DefaultState"
            every { ann.shortName } returns shortName

            val enumEntryType: KSType = mockk()
            val enumEntryTypeDecl: KSClassDeclaration = mockk()
            every { enumEntryTypeDecl.simpleName } returns mockKSName(enumEntryName)
            every { enumEntryType.declaration } returns enumEntryTypeDecl

            every { ann.arguments } returns listOf(
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("type")
                    every { arg.value } returns enumEntryType
                }
            )
            return ann
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

        /**
         * Mocks a shorthand default annotation (e.g. @DefaultEmptyString, @DefaultZeroInt).
         * The qualified name follows the convention: ...defaults.state.standard.<simpleName>
         */
        private fun mockShorthandDefaultAnnotation(simpleName: String): KSAnnotation {
            val ann: KSAnnotation = mockk()
            val shortName: KSName = mockk()
            every { shortName.asString() } returns simpleName
            every { ann.shortName } returns shortName

            val annTypeRef: KSTypeReference = mockk()
            val annResolvedType: KSType = mockk()
            val annDecl: KSClassDeclaration = mockk()
            val annQualName: KSName = mockk()
            every { annQualName.asString() } returns
                "org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard.$simpleName"
            every { annDecl.qualifiedName } returns annQualName
            every { annResolvedType.declaration } returns annDecl
            every { annTypeRef.resolve() } returns annResolvedType
            every { ann.annotationType } returns annTypeRef
            every { ann.arguments } returns emptyList()
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
            every { classDecl.packageName } returns mockKSName("kotlin")

            val resolvedType: KSType = mockk()
            every { resolvedType.isMarkedNullable } returns false
            every { resolvedType.declaration } returns classDecl
            every { resolvedType.arguments } returns emptyList()
            every { typeRef.resolve() } returns resolvedType

            val prop: KSPropertyDeclaration = mockk()
            every { prop.simpleName } returns mockKSName(name)
            every { prop.type } returns typeRef
            every { prop.annotations } returns annotations
            every { prop.docString } returns ""
            return prop
        }

        private fun mockDefaultEnumAnnotation(
            value: String,
            packageName: String = "",
            className: String = ""
        ): KSAnnotation {
            val annTypeRef: KSTypeReference = mockk()
            val annResolvedType: KSType = mockk()
            val annDecl: KSClassDeclaration = mockk()
            val annQualName: KSName = mockk()
            every { annQualName.asString() } returns "org.khorum.oss.konstellation.metaDsl.annotation.defaults.DefaultEnum"
            every { annDecl.qualifiedName } returns annQualName
            every { annResolvedType.declaration } returns annDecl
            every { annTypeRef.resolve() } returns annResolvedType

            val ann: KSAnnotation = mockk()
            every { ann.annotationType } returns annTypeRef
            val shortName: KSName = mockk()
            every { shortName.asString() } returns "DefaultEnum"
            every { ann.shortName } returns shortName
            every { ann.arguments } returns listOf(
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("value")
                    every { arg.value } returns value
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("packageName")
                    every { arg.value } returns packageName
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("className")
                    every { arg.value } returns className
                }
            )
            return ann
        }

        private fun mockPropWithDefaultEnum(
            propName: String = "rank",
            value: String,
            packageName: String = "",
            className: String = "",
            enumQualifiedName: String = "com.example.Rank",
            enumPackageName: String = "com.example"
        ): KSPropertyDeclaration {
            val ann = mockDefaultEnumAnnotation(value, packageName, className)

            val typeRef: KSTypeReference = mockk()
            every { typeRef.toTypeName() } returns STRING

            // Mock the property type declaration (the enum class)
            val enumDecl: KSClassDeclaration = mockk()
            every { enumDecl.toClassName() } returns ClassName(enumPackageName, enumQualifiedName.removePrefix("$enumPackageName."))
            every { enumDecl.annotations } returns emptySequence()
            every { enumDecl.qualifiedName } returns mockKSName(enumQualifiedName)
            every { enumDecl.packageName } returns mockKSName(enumPackageName)

            val resolvedType: KSType = mockk()
            every { resolvedType.isMarkedNullable } returns false
            every { resolvedType.declaration } returns enumDecl
            every { resolvedType.arguments } returns emptyList()
            every { typeRef.resolve() } returns resolvedType

            val prop: KSPropertyDeclaration = mockk()
            every { prop.simpleName } returns mockKSName(propName)
            every { prop.type } returns typeRef
            every { prop.annotations } returns sequenceOf(ann)
            every { prop.docString } returns ""
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
            every { prop.docString } returns ""

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
    fun `extractDefaultPropertyValue with inferType true and non-primitive type uses string template`() = test {
        given {
            val service = DefaultPropertySchemaService()
            // String is not in PRIMITIVE_TYPE_NAMES, so isLiteral=false, isStringClass=true
            val ann = mockDefaultValueAnnotation(
                listOf(
                    "value" to "hello",
                    "packageName" to "",
                    "className" to "",
                    "inferType" to true
                )
            )
            // mockPropWithAnnotations already uses kotlin.String as the resolved type
            val prop = mockPropWithAnnotations("strField", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val dv = service.getParamsFromDomain(domainConfig).first().defaultValue
                dv != null && dv.rawValue == "hello"
            }
        }
    }

    @Test
    fun `extractDefaultPropertyValue with className String uses string template`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockDefaultValueAnnotation(
                listOf(
                    "value" to "test",
                    "packageName" to "kotlin",
                    "className" to "String"
                )
            )
            val prop = mockPropWithAnnotations("strField", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val dv = service.getParamsFromDomain(domainConfig).first().defaultValue
                dv != null && dv.rawValue == "test"
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

    @Test
    fun `extractDefaultPropertyValue with inferType true and non-empty className skips literal check`() = test {
        given {
            val service = DefaultPropertySchemaService()
            // inferType=true but className is non-empty, so the literal check is skipped
            val ann = mockDefaultValueAnnotation(
                listOf(
                    "value" to "MyEnum.A",
                    "packageName" to "org.test",
                    "className" to "MyEnum",
                    "inferType" to true
                )
            )
            val prop = mockPropWithAnnotations("enumField", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val dv = service.getParamsFromDomain(domainConfig).first().defaultValue
                dv != null && dv.rawValue == "MyEnum.A" && dv.importString() == "org.test.MyEnum"
            }
        }
    }

    @Test
    fun `extractDefaultPropertyValue with inferType true and non-empty packageName only skips literal check`() = test {
        given {
            val service = DefaultPropertySchemaService()
            // packageName non-empty, className empty → skips literal check, isStringClass = true (empty className + empty packageName is false here since packageName is non-empty, but className is empty)
            val ann = mockDefaultValueAnnotation(
                listOf(
                    "value" to "test",
                    "packageName" to "org.test",
                    "className" to "",
                    "inferType" to true
                )
            )
            val prop = mockPropWithAnnotations("field", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val dv = service.getParamsFromDomain(domainConfig).first().defaultValue
                dv != null && dv.rawValue == "test"
            }
        }
    }

    @Test
    fun `extractDefaultPropertyValue without inferType argument defaults to true`() = test {
        given {
            val service = DefaultPropertySchemaService()
            // No inferType argument at all → defaults to true
            val ann = mockDefaultValueAnnotation(
                listOf(
                    "value" to "hello",
                    "packageName" to "",
                    "className" to ""
                )
            )
            val prop = mockPropWithAnnotations("noInferArg", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val dv = service.getParamsFromDomain(domainConfig).first().defaultValue
                dv != null && dv.rawValue == "hello"
            }
        }
    }

    @Test
    fun `extractDefaultPropertyValue with non-String non-empty className uses literal template`() = test {
        given {
            val service = DefaultPropertySchemaService()
            // className is non-empty and not "String" → isStringClass = false, isLiteral = false (inferType short-circuits)
            val ann = mockDefaultValueAnnotation(
                listOf(
                    "value" to "Color.RED",
                    "packageName" to "com.example",
                    "className" to "Color",
                    "inferType" to false
                )
            )
            val prop = mockPropWithAnnotations("colorField", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val dv = service.getParamsFromDomain(domainConfig).first().defaultValue
                dv != null && dv.rawValue == "Color.RED" && dv.importString() == "com.example.Color"
            }
        }
    }

    @Test
    fun `extractDefaultPropertyValue with raw null but annotation present returns null`() = test {
        given {
            val service = DefaultPropertySchemaService()
            // Annotation exists but the "value" argument is missing
            val ann = mockDefaultValueAnnotation(
                listOf("packageName" to "kotlin", "className" to "String")
            )
            val prop = mockPropWithAnnotations("noRaw", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue }
        }
    }

    @Test
    fun `extractDefaultPropertyValue with inferType false and empty classNames uses string template`() = test {
        given {
            val service = DefaultPropertySchemaService()
            // inferType = false, empty packageName/className → isLiteral stays false, isStringClass = true
            val ann = mockDefaultValueAnnotation(
                listOf(
                    "value" to "defaultVal",
                    "packageName" to "",
                    "className" to "",
                    "inferType" to false
                )
            )
            val prop = mockPropWithAnnotations("strField", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val dv = service.getParamsFromDomain(domainConfig).first().defaultValue
                // With inferType=false, className and packageName empty → isStringClass = true → %S template
                dv != null && dv.rawValue == "defaultVal"
            }
        }
    }

    @Test
    fun `extractDefaultPropertyValue with debug enabled and no annotation exercises null path`() = test {
        given {
            val service = DefaultPropertySchemaService()
            service.logger.enableDebug()
            // No DefaultValue annotation → ann is null, all ?.chains short-circuit
            val prop = mockPropWithAnnotations("noAnn", emptySequence())
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever {
                try {
                    service.getParamsFromDomain(domainConfig).first().defaultValue
                } finally {
                    service.logger.disableDebug()
                }
            }
        }
    }

    @Test
    fun `extractDefaultPropertyValue with inferType true and non-primitive non-String uses string template`() = test {
        given {
            val service = DefaultPropertySchemaService()
            // inferType=true, empty className/packageName, but property type is NOT primitive (it's kotlin.String which is NOT in PRIMITIVE_TYPE_NAMES)
            // → isLiteral=false, isStringClass=true (because both className and packageName are empty)
            val ann = mockDefaultValueAnnotation(
                listOf(
                    "value" to "someValue",
                    "packageName" to "",
                    "className" to "",
                    "inferType" to true
                )
            )
            // Default mock prop type is kotlin.String which is not in PRIMITIVE_TYPE_NAMES
            val prop = mockPropWithAnnotations("nonPrimField", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val dv = service.getParamsFromDomain(domainConfig).first().defaultValue
                dv != null && dv.rawValue == "someValue"
            }
        }
    }

    @Test
    fun `extractDefaultPropertyValue with annotation but empty args returns null`() = test {
        given {
            val service = DefaultPropertySchemaService()
            // @DefaultValue annotation exists but has no arguments at all
            val ann = mockDefaultValueAnnotation(emptyList())
            val prop = mockPropWithAnnotations("emptyArgs", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue }
        }
    }

    @Test
    fun `extractDefaultPropertyValue with annotation value and packageName but missing className`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockDefaultValueAnnotation(
                listOf("value" to "test", "packageName" to "com.test")
            )
            val prop = mockPropWithAnnotations("missingClass", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue }
        }
    }

    @Test
    fun `extractDefaultPropertyValue with annotation value and className but missing packageName`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockDefaultValueAnnotation(
                listOf("value" to "test", "className" to "MyClass")
            )
            val prop = mockPropWithAnnotations("missingPkg", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue }
        }
    }

    // --- @DefaultState tests ---

    @Test
    fun `getParamsFromDomain extracts DefaultState EMPTY_STRING`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockDefaultStateAnnotation("EMPTY_STRING")
            val prop = mockPropWithAnnotations("name", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val dv = service.getParamsFromDomain(domainConfig).first().defaultValue
                dv != null && dv.rawValue == "\"\"" && dv.packageName == "" && dv.className == ""
            }
        }
    }

    @Test
    fun `getParamsFromDomain extracts DefaultState ZERO_INT`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockDefaultStateAnnotation("ZERO_INT")
            val prop = mockPropWithAnnotations("count", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val dv = service.getParamsFromDomain(domainConfig).first().defaultValue
                dv != null && dv.rawValue == "0"
            }
        }
    }

    @Test
    fun `getParamsFromDomain extracts DefaultState ZERO_LONG`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockDefaultStateAnnotation("ZERO_LONG")
            val prop = mockPropWithAnnotations("timestamp", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "0L" }
            whenever {
                service.getParamsFromDomain(domainConfig).first().defaultValue?.rawValue
            }
        }
    }

    @Test
    fun `getParamsFromDomain extracts DefaultState ZERO_DOUBLE`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockDefaultStateAnnotation("ZERO_DOUBLE")
            val prop = mockPropWithAnnotations("rate", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "0.0" }
            whenever {
                service.getParamsFromDomain(domainConfig).first().defaultValue?.rawValue
            }
        }
    }

    @Test
    fun `getParamsFromDomain extracts DefaultState ZERO_FLOAT`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockDefaultStateAnnotation("ZERO_FLOAT")
            val prop = mockPropWithAnnotations("weight", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "0.0f" }
            whenever {
                service.getParamsFromDomain(domainConfig).first().defaultValue?.rawValue
            }
        }
    }

    @Test
    fun `getParamsFromDomain extracts DefaultState EMPTY_LIST`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockDefaultStateAnnotation("EMPTY_LIST")
            val prop = mockPropWithAnnotations("tags", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "mutableListOf()" }
            whenever {
                service.getParamsFromDomain(domainConfig).first().defaultValue?.rawValue
            }
        }
    }

    @Test
    fun `getParamsFromDomain extracts DefaultState EMPTY_MAP`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockDefaultStateAnnotation("EMPTY_MAP")
            val prop = mockPropWithAnnotations("metadata", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "mutableMapOf()" }
            whenever {
                service.getParamsFromDomain(domainConfig).first().defaultValue?.rawValue
            }
        }
    }

    @Test
    fun `getParamsFromDomain extracts DefaultState TRUE`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockDefaultStateAnnotation("TRUE")
            val prop = mockPropWithAnnotations("enabled", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "true" }
            whenever {
                service.getParamsFromDomain(domainConfig).first().defaultValue?.rawValue
            }
        }
    }

    @Test
    fun `getParamsFromDomain extracts DefaultState FALSE`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockDefaultStateAnnotation("FALSE")
            val prop = mockPropWithAnnotations("active", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "false" }
            whenever {
                service.getParamsFromDomain(domainConfig).first().defaultValue?.rawValue
            }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultState has no import string`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockDefaultStateAnnotation("EMPTY_STRING")
            val prop = mockPropWithAnnotations("name", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever {
                service.getParamsFromDomain(domainConfig).first().defaultValue?.importString()
            }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultState takes precedence when both DefaultState and DefaultValue present`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val stateAnn = mockDefaultStateAnnotation("EMPTY_STRING")
            val valueAnn = mockDefaultValueAnnotation(
                listOf("value" to "hello", "packageName" to "kotlin", "className" to "String")
            )
            val prop = mockPropWithAnnotations("conflict", sequenceOf(stateAnn, valueAnn))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "\"\"" }
            whenever {
                service.getParamsFromDomain(domainConfig).first().defaultValue?.rawValue
            }
        }
    }

    @Test
    fun `getParamsFromDomain extracts DefaultState via KSType representation`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockDefaultStateAnnotationWithKSType("ZERO_INT")
            val prop = mockPropWithAnnotations("count", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "0" }
            whenever {
                service.getParamsFromDomain(domainConfig).first().defaultValue?.rawValue
            }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultState returns null when type is unknown enum entry`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockDefaultStateAnnotation("NONEXISTENT_TYPE")
            val prop = mockPropWithAnnotations("badType", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultState resolves from string toString fallback`() = test {
        given {
            val service = DefaultPropertySchemaService()
            // Create a @DefaultState annotation where the type argument is a plain String (fallback branch)
            val annTypeRef: KSTypeReference = mockk()
            val annResolvedType: KSType = mockk()
            val annDecl: KSClassDeclaration = mockk()
            val annQualName: KSName = mockk()
            every { annQualName.asString() } returns "org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.DefaultState"
            every { annDecl.qualifiedName } returns annQualName
            every { annResolvedType.declaration } returns annDecl
            every { annTypeRef.resolve() } returns annResolvedType

            val ann: KSAnnotation = mockk()
            every { ann.annotationType } returns annTypeRef
            val shortName: KSName = mockk()
            every { shortName.asString() } returns "DefaultState"
            every { ann.shortName } returns shortName
            // Return a plain String as the value (not KSClassDeclaration or KSType)
            every { ann.arguments } returns listOf(
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("type")
                    every { arg.value } returns "DefaultStateType.ZERO_INT"
                }
            )

            val prop = mockPropWithAnnotations("stringFallback", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "0" }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue?.rawValue }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultState returns null when no type argument exists`() = test {
        given {
            val service = DefaultPropertySchemaService()
            // Create a @DefaultState annotation with no arguments at all
            val annTypeRef: KSTypeReference = mockk()
            val annResolvedType: KSType = mockk()
            val annDecl: KSClassDeclaration = mockk()
            val annQualName: KSName = mockk()
            every { annQualName.asString() } returns "org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.DefaultState"
            every { annDecl.qualifiedName } returns annQualName
            every { annResolvedType.declaration } returns annDecl
            every { annTypeRef.resolve() } returns annResolvedType

            val ann: KSAnnotation = mockk()
            every { ann.annotationType } returns annTypeRef
            val shortName: KSName = mockk()
            every { shortName.asString() } returns "DefaultState"
            every { ann.shortName } returns shortName
            every { ann.arguments } returns emptyList()

            val prop = mockPropWithAnnotations("noArgs", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultState returns null default when annotation has wrong argument name`() = test {
        given {
            val service = DefaultPropertySchemaService()
            // Annotation with a "wrongName" argument instead of "type"
            val annTypeRef: KSTypeReference = mockk()
            val annResolvedType: KSType = mockk()
            val annDecl: KSClassDeclaration = mockk()
            val annQualName: KSName = mockk()
            every { annQualName.asString() } returns "org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.DefaultState"
            every { annDecl.qualifiedName } returns annQualName
            every { annResolvedType.declaration } returns annDecl
            every { annTypeRef.resolve() } returns annResolvedType

            val ann: KSAnnotation = mockk()
            every { ann.annotationType } returns annTypeRef
            val shortName: KSName = mockk()
            every { shortName.asString() } returns "DefaultState"
            every { ann.shortName } returns shortName
            every { ann.arguments } returns listOf(
                mockk<KSValueArgument>(relaxed = true).also { arg ->
                    every { arg.name } returns mockKSName("wrongName")
                }
            )

            val prop = mockPropWithAnnotations("wrongArg", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue }
        }
    }

    @Test
    fun `getParamsFromDomain returns null default when neither DefaultState nor DefaultValue present`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val prop = mockPropWithAnnotations("plain", emptySequence())
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue }
        }
    }

    @Test
    fun `getParamsFromDomain resolves shorthand DefaultEmptyString annotation`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockShorthandDefaultAnnotation("DefaultEmptyString")
            val prop = mockPropWithAnnotations("label", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "\"\"" }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue?.rawValue }
        }
    }

    @Test
    fun `getParamsFromDomain resolves shorthand DefaultZeroInt annotation`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockShorthandDefaultAnnotation("DefaultZeroInt")
            val prop = mockPropWithAnnotations("count", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "0" }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue?.rawValue }
        }
    }

    @Test
    fun `getParamsFromDomain resolves shorthand DefaultTrue annotation`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockShorthandDefaultAnnotation("DefaultTrue")
            val prop = mockPropWithAnnotations("enabled", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "true" }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue?.rawValue }
        }
    }

    @Test
    fun `getParamsFromDomain resolves shorthand DefaultEmptyList annotation`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockShorthandDefaultAnnotation("DefaultEmptyList")
            val prop = mockPropWithAnnotations("items", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "mutableListOf()" }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue?.rawValue }
        }
    }

    @Test
    fun `getParamsFromDomain ignores unknown shorthand annotation`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockShorthandDefaultAnnotation("DefaultUnknown")
            val prop = mockPropWithAnnotations("x", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultState with debug enabled exercises logging`() = test {
        given {
            val service = DefaultPropertySchemaService()
            service.logger.enableDebug()
            val ann = mockDefaultStateAnnotation("ZERO_INT")
            val prop = mockPropWithAnnotations("count", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                try {
                    val dv = service.getParamsFromDomain(domainConfig).first().defaultValue
                    dv != null && dv.rawValue == "0"
                } finally {
                    service.logger.disableDebug()
                }
            }
        }
    }

    @Test
    fun `getParamsFromDomain TransientDsl with null reason exercises null branch`() = test {
        given {
            val service = DefaultPropertySchemaService()
            service.logger.enableDebug()
            // TransientDsl without reason argument
            val ann = mockAnnotation("TransientDsl")
            val prop = mockPropWithAnnotations("transientNoReason", sequenceOf(ann))
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
    fun `getParamsFromDomain shorthand DefaultFalse annotation`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockShorthandDefaultAnnotation("DefaultFalse")
            val prop = mockPropWithAnnotations("disabled", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "false" }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue?.rawValue }
        }
    }

    @Test
    fun `getParamsFromDomain shorthand DefaultEmptyMap annotation`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockShorthandDefaultAnnotation("DefaultEmptyMap")
            val prop = mockPropWithAnnotations("data", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "mutableMapOf()" }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue?.rawValue }
        }
    }

    @Test
    fun `getParamsFromDomain shorthand DefaultZeroLong annotation`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockShorthandDefaultAnnotation("DefaultZeroLong")
            val prop = mockPropWithAnnotations("ts", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "0L" }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue?.rawValue }
        }
    }

    @Test
    fun `getParamsFromDomain shorthand DefaultZeroDouble annotation`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockShorthandDefaultAnnotation("DefaultZeroDouble")
            val prop = mockPropWithAnnotations("rate", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "0.0" }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue?.rawValue }
        }
    }

    @Test
    fun `getParamsFromDomain shorthand DefaultZeroFloat annotation`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockShorthandDefaultAnnotation("DefaultZeroFloat")
            val prop = mockPropWithAnnotations("weight", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "0.0f" }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue?.rawValue }
        }
    }

    @Test
    fun `getParamsFromDomain shorthand with debug enabled exercises logging`() = test {
        given {
            val service = DefaultPropertySchemaService()
            service.logger.enableDebug()
            val ann = mockShorthandDefaultAnnotation("DefaultEmptyString")
            val prop = mockPropWithAnnotations("label", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "\"\"" }
            whenever {
                try {
                    service.getParamsFromDomain(domainConfig).first().defaultValue?.rawValue
                } finally {
                    service.logger.disableDebug()
                }
            }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultTrue with no template args has null booleanAccessorConfig`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockShorthandDefaultAnnotation("DefaultTrue")
            val prop = mockPropWithAnnotations("enabled", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue?.booleanAccessorConfig }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultTrue with template args creates BooleanAccessorConfig`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockShorthandDefaultAnnotation("DefaultTrue")

            // Add template arguments to the annotation
            val validTemplateDecl: KSClassDeclaration = mockk()
            every { validTemplateDecl.simpleName } returns mockKSName("SELF")

            val negationTemplateDecl: KSClassDeclaration = mockk()
            every { negationTemplateDecl.simpleName } returns mockKSName("NOT")

            every { ann.arguments } returns listOf(
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("validTemplate")
                    every { arg.value } returns validTemplateDecl
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("negationTemplate")
                    every { arg.value } returns negationTemplateDecl
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("validFunctionName")
                    every { arg.value } returns ""
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("negationFunctionName")
                    every { arg.value } returns ""
                }
            )

            val prop = mockPropWithAnnotations("isCool", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val config = service.getParamsFromDomain(domainConfig).first().defaultValue?.booleanAccessorConfig
                config != null && config.validTemplate == "SELF" && config.negationTemplate == "NOT"
            }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultFalse with SELF negation blanks out valid template`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockShorthandDefaultAnnotation("DefaultFalse")

            val negationTemplateDecl: KSClassDeclaration = mockk()
            every { negationTemplateDecl.simpleName } returns mockKSName("SELF")

            every { ann.arguments } returns listOf(
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("negationTemplate")
                    every { arg.value } returns negationTemplateDecl
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("validFunctionName")
                    every { arg.value } returns ""
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("negationFunctionName")
                    every { arg.value } returns ""
                }
            )

            val prop = mockPropWithAnnotations("withoutMonthly", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val config = service.getParamsFromDomain(domainConfig).first().defaultValue?.booleanAccessorConfig
                config != null && config.validTemplate == "NONE" && config.negationTemplate == "SELF"
            }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultTrue with explicit valid function name`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockShorthandDefaultAnnotation("DefaultTrue")

            every { ann.arguments } returns listOf(
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("validFunctionName")
                    every { arg.value } returns "myValidFn"
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("negationFunctionName")
                    every { arg.value } returns "myNegFn"
                }
            )

            val prop = mockPropWithAnnotations("enabled", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val config = service.getParamsFromDomain(domainConfig)
                    .first().defaultValue?.booleanAccessorConfig
                config != null &&
                    config.validFunctionName == "myValidFn" &&
                    config.negationFunctionName == "myNegFn"
            }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultTrue with SELF negation and IS valid preserves both`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockShorthandDefaultAnnotation("DefaultTrue")

            val validDecl: KSClassDeclaration = mockk()
            every { validDecl.simpleName } returns mockKSName("IS")
            val negDecl: KSClassDeclaration = mockk()
            every { negDecl.simpleName } returns mockKSName("SELF")

            every { ann.arguments } returns listOf(
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("validTemplate")
                    every { arg.value } returns validDecl
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("negationTemplate")
                    every { arg.value } returns negDecl
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("validFunctionName")
                    every { arg.value } returns ""
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("negationFunctionName")
                    every { arg.value } returns ""
                }
            )

            val prop = mockPropWithAnnotations("isCool", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val config = service.getParamsFromDomain(domainConfig)
                    .first().defaultValue?.booleanAccessorConfig
                // negation is SELF but valid IS is set → no blanking
                config != null &&
                    config.validTemplate == "IS" &&
                    config.negationTemplate == "SELF"
            }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultTrue template resolves string enum entry`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockShorthandDefaultAnnotation("DefaultTrue")

            // Test the else branch of resolveEnumEntryName where value is a string
            every { ann.arguments } returns listOf(
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("validTemplate")
                    every { arg.value } returns "ValidFunctionTemplate.WITH"
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("negationFunctionName")
                    every { arg.value } returns ""
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("validFunctionName")
                    every { arg.value } returns ""
                }
            )

            val prop = mockPropWithAnnotations("cool", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val config = service.getParamsFromDomain(domainConfig)
                    .first().defaultValue?.booleanAccessorConfig
                config != null && config.validTemplate == "WITH"
            }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultFalse with null ann returns null config`() = test {
        given {
            val service = DefaultPropertySchemaService()
            // DefaultEmptyString is not TRUE or FALSE, so booleanAccessorConfig is null
            val ann = mockShorthandDefaultAnnotation("DefaultEmptyString")
            val prop = mockPropWithAnnotations("label", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever {
                service.getParamsFromDomain(domainConfig)
                    .first().defaultValue?.booleanAccessorConfig
            }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultTrue with KSType template value resolves via declaration`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockShorthandDefaultAnnotation("DefaultTrue")

            // Use KSType as template value (covers resolveEnumEntryName KSType branch)
            val validTemplateType: KSType = mockk()
            val validTemplateDecl: com.google.devtools.ksp.symbol.KSDeclaration = mockk()
            every { validTemplateDecl.simpleName } returns mockKSName("IS")
            every { validTemplateType.declaration } returns validTemplateDecl

            every { ann.arguments } returns listOf(
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("validTemplate")
                    every { arg.value } returns validTemplateType
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("negationFunctionName")
                    every { arg.value } returns ""
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("validFunctionName")
                    every { arg.value } returns ""
                }
            )

            val prop = mockPropWithAnnotations("cool", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val config = service.getParamsFromDomain(domainConfig)
                    .first().defaultValue?.booleanAccessorConfig
                config != null && config.validTemplate == "IS"
            }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultFalse with named negation preserves valid SELF`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockShorthandDefaultAnnotation("DefaultFalse")

            // Simulates KSP including default validTemplate=SELF when user only sets negationTemplate
            val validTemplateDecl: KSClassDeclaration = mockk()
            every { validTemplateDecl.simpleName } returns mockKSName("SELF")

            val negationTemplateDecl: KSClassDeclaration = mockk()
            every { negationTemplateDecl.simpleName } returns mockKSName("DOES_NOT_HAVE")

            every { ann.arguments } returns listOf(
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("validTemplate")
                    every { arg.value } returns validTemplateDecl
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("negationTemplate")
                    every { arg.value } returns negationTemplateDecl
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("validFunctionName")
                    every { arg.value } returns ""
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("negationFunctionName")
                    every { arg.value } returns ""
                }
            )

            val prop = mockPropWithAnnotations("hasTouch", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val config = service.getParamsFromDomain(domainConfig).first().defaultValue?.booleanAccessorConfig
                // Valid SELF must be preserved (not blanked to NONE) when negation is not SELF
                config != null &&
                    config.validTemplate == "SELF" &&
                    config.negationTemplate == "DOES_NOT_HAVE" &&
                    config.resolveValidFunctionName("hasTouch") == "hasTouch" &&
                    config.resolveNegationFunctionName("hasTouch") == "doesNotHaveTouch"
            }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultFalse with negation only and no valid in args preserves valid`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockShorthandDefaultAnnotation("DefaultFalse")

            // Simulates KSP NOT including default validTemplate (some KSP versions)
            val negationTemplateDecl: KSClassDeclaration = mockk()
            every { negationTemplateDecl.simpleName } returns mockKSName("DISABLED")

            every { ann.arguments } returns listOf(
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("negationTemplate")
                    every { arg.value } returns negationTemplateDecl
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("validFunctionName")
                    every { arg.value } returns ""
                },
                mockk<KSValueArgument>().also { arg ->
                    every { arg.name } returns mockKSName("negationFunctionName")
                    every { arg.value } returns ""
                }
            )

            val prop = mockPropWithAnnotations("someItemEnabled", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val config = service.getParamsFromDomain(domainConfig).first().defaultValue?.booleanAccessorConfig
                // validTemplate is null (not in args), but resolveValidFunctionName returns propName
                config != null &&
                    config.validTemplate == null &&
                    config.negationTemplate == "DISABLED" &&
                    config.resolveValidFunctionName("someItemEnabled") == "someItemEnabled" &&
                    config.resolveNegationFunctionName("someItemEnabled") == "someItemDisabled"
            }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultState with missing type argument returns null`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val annTypeRef: KSTypeReference = mockk()
            val annResolvedType: KSType = mockk()
            val annDecl: KSClassDeclaration = mockk()
            val annQualName: KSName = mockk()
            every { annQualName.asString() } returns "org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.DefaultState"
            every { annDecl.qualifiedName } returns annQualName
            every { annResolvedType.declaration } returns annDecl
            every { annTypeRef.resolve() } returns annResolvedType

            val ann: KSAnnotation = mockk()
            every { ann.annotationType } returns annTypeRef
            val shortName: KSName = mockk()
            every { shortName.asString() } returns "DefaultState"
            every { ann.shortName } returns shortName
            // Argument with "type" name but null value - uses relaxed mock that returns default
            val typeArg: KSValueArgument = mockk(relaxed = true)
            every { typeArg.name } returns mockKSName("type")
            every { typeArg.value } returns "INVALID..NO..MATCH"
            every { ann.arguments } returns listOf(typeArg)

            val prop = mockPropWithAnnotations("badEntry", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue }
        }
    }

    @Test
    fun `getParamsFromDomain annotation with null qualifiedName on type declaration is skipped`() = test {
        given {
            val service = DefaultPropertySchemaService()
            // Annotation whose type declaration has null qualifiedName
            val ann: KSAnnotation = mockk()
            val shortName: KSName = mockk()
            every { shortName.asString() } returns "SomeAnnotation"
            every { ann.shortName } returns shortName
            val annTypeRef: KSTypeReference = mockk()
            val annResolvedType: KSType = mockk()
            val annDecl: KSClassDeclaration = mockk()
            val annQualName: KSName = mockk()
            every { annQualName.asString() } returns "unknown.package.UnknownAnnotation"
            every { annDecl.qualifiedName } returns annQualName
            every { annResolvedType.declaration } returns annDecl
            every { annTypeRef.resolve() } returns annResolvedType
            every { ann.annotationType } returns annTypeRef

            val prop = mockPropWithAnnotations("field", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever { service.getParamsFromDomain(domainConfig).first().defaultValue }
        }
    }

    @Test
    fun `getParamsFromDomain extracts docString from property`() = test {
        given {
            val service = DefaultPropertySchemaService()

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
            every { prop.simpleName } returns mockKSName("name")
            every { prop.type } returns typeRef
            every { prop.annotations } returns emptySequence()
            every { prop.docString } returns " The ship name\n"

            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "The ship name" }
            whenever { service.getParamsFromDomain(domainConfig).first().annotationMetadata.docString }
        }
    }

    @Test
    fun `getParamsFromDomain sets null docString when property has empty docString`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val prop = mockPropWithAnnotations("name", emptySequence())
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever { service.getParamsFromDomain(domainConfig).first().annotationMetadata.docString }
        }
    }

    // --- @DefaultEnum tests ---

    @Test
    fun `getParamsFromDomain extracts DefaultEnum with inferred type`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val prop = mockPropWithDefaultEnum(
                value = "CAPTAIN",
                enumQualifiedName = "com.example.Rank",
                enumPackageName = "com.example"
            )
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val schemas = service.getParamsFromDomain(domainConfig)
                val dv = schemas.first().defaultValue
                dv != null && dv.rawValue == "CAPTAIN"
            }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultEnum infers package and class from property type`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val prop = mockPropWithDefaultEnum(
                value = "ENSIGN",
                enumQualifiedName = "com.example.Rank",
                enumPackageName = "com.example"
            )
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "Rank.ENSIGN" }
            whenever {
                val schemas = service.getParamsFromDomain(domainConfig)
                schemas.first().defaultValue?.codeBlock?.toString()
            }
        }
    }

    @Test
    fun `getParamsFromDomain extracts DefaultEnum with explicit package and class`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val prop = mockPropWithDefaultEnum(
                value = "BRIDGE",
                packageName = "com.example",
                className = "Department"
            )
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "Department.BRIDGE" }
            whenever {
                val schemas = service.getParamsFromDomain(domainConfig)
                schemas.first().defaultValue?.codeBlock?.toString()
            }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultEnum with nested enum infers correct className`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val prop = mockPropWithDefaultEnum(
                value = "CIVILIAN",
                enumQualifiedName = "com.example.Passenger.Rank",
                enumPackageName = "com.example"
            )
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "Passenger.Rank.CIVILIAN" }
            whenever {
                val schemas = service.getParamsFromDomain(domainConfig)
                schemas.first().defaultValue?.codeBlock?.toString()
            }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultEnum with blank value returns no default`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val ann = mockDefaultEnumAnnotation(value = "", packageName = "", className = "")
            val prop = mockPropWithAnnotations("rank", sequenceOf(ann))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { null }
            whenever {
                service.getParamsFromDomain(domainConfig).first().defaultValue
            }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultEnum with DefaultState prefers DefaultState`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val enumAnn = mockDefaultEnumAnnotation(value = "CAPTAIN")
            val stateAnn = mockDefaultStateAnnotation("EMPTY_STRING")
            val prop = mockPropWithAnnotations("field", sequenceOf(stateAnn, enumAnn))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { true }
            whenever {
                val schemas = service.getParamsFromDomain(domainConfig)
                val dv = schemas.first().defaultValue
                // DefaultState takes precedence — its codeSnippet is "" for EMPTY_STRING
                dv != null && dv.rawValue == "\"\""
            }
        }
    }

    @Test
    fun `getParamsFromDomain DefaultEnum with DefaultValue prefers DefaultEnum`() = test {
        given {
            val service = DefaultPropertySchemaService()
            val enumAnn = mockDefaultEnumAnnotation(
                value = "ENSIGN",
                packageName = "com.example",
                className = "Rank"
            )
            val valueAnn = mockDefaultValueAnnotation(
                listOf("value" to "fallback", "packageName" to "kotlin", "className" to "String")
            )
            val prop = mockPropWithAnnotations("rank", sequenceOf(enumAnn, valueAnn))
            val domainConfig = mockDomainConfig(sequenceOf(prop))

            expect { "ENSIGN" }
            whenever {
                service.getParamsFromDomain(domainConfig).first().defaultValue?.rawValue
            }
        }
    }
}
