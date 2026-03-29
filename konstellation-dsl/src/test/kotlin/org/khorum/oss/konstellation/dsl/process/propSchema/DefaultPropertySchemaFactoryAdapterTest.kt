package org.khorum.oss.konstellation.dsl.process.propSchema

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
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
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class DefaultPropertySchemaFactoryAdapterTest : UnitSim() {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setupAll() {
            mockkStatic(KSTypeReference::toTypeName)
            mockkStatic(KSClassDeclaration::toClassName)
            mockkStatic(KSType::toTypeName)
        }

        @JvmStatic
        @AfterAll
        fun teardownAll() {
            unmockkStatic(KSTypeReference::toTypeName)
            unmockkStatic(KSClassDeclaration::toClassName)
            unmockkStatic(KSType::toTypeName)
        }

        private fun mockKSName(value: String): KSName {
            val name: KSName = mockk()
            every { name.asString() } returns value
            every { name.getShortName() } returns value
            return name
        }

        private fun mockValueArg(name: String, value: Any?): KSValueArgument {
            val arg: KSValueArgument = mockk()
            every { arg.name } returns mockKSName(name)
            every { arg.value } returns value
            return arg
        }

        fun mockProp(
            name: String = "testProp",
            nullable: Boolean = false,
            annotations: Sequence<KSAnnotation> = emptySequence(),
            declarationClass: KSClassDeclaration? = null,
            typeArguments: List<KSTypeArgument> = emptyList()
        ): KSPropertyDeclaration {
            val typeRef: KSTypeReference = mockk()
            every { typeRef.toTypeName() } returns STRING

            val resolvedType: KSType = mockk()
            every { resolvedType.isMarkedNullable } returns nullable
            val defaultDecl = declarationClass ?: run {
                val d: KSClassDeclaration = mockk()
                every { d.toClassName() } returns ClassName("kotlin", "String")
                every { d.annotations } returns emptySequence()
                every { d.qualifiedName } returns mockKSName("kotlin.String")
                d
            }
            every { resolvedType.declaration } returns defaultDecl
            every { resolvedType.arguments } returns typeArguments
            every { typeRef.resolve() } returns resolvedType

            val prop: KSPropertyDeclaration = mockk()
            every { prop.simpleName } returns mockKSName(name)
            every { prop.type } returns typeRef
            every { prop.annotations } returns annotations
            return prop
        }

        fun mockDslPropertyAnnotation(withVararg: Boolean?, withProvider: Boolean?): KSAnnotation {
            val ann: KSAnnotation = mockk()
            val shortName = mockKSName("DslProperty")
            every { ann.shortName } returns shortName

            val args = mutableListOf<KSValueArgument>()
            if (withVararg != null) args.add(mockValueArg("withVararg", withVararg))
            if (withProvider != null) args.add(mockValueArg("withProvider", withProvider))

            every { ann.arguments } returns args
            return ann
        }

        fun mockSingleEntryTransformDecl(
            transformTemplate: String?,
            inputTypeKSType: Any?
        ): KSClassDeclaration {
            val decl: KSClassDeclaration = mockk()
            val ann: KSAnnotation = mockk()
            val shortName = mockKSName("SingleEntryTransformDsl")
            every { ann.shortName } returns shortName

            val args = mutableListOf<KSValueArgument>()
            if (transformTemplate != null) args.add(mockValueArg("transformTemplate", transformTemplate))
            if (inputTypeKSType != null) args.add(mockValueArg("inputType", inputTypeKSType))

            every { ann.arguments } returns args
            every { decl.annotations } returns sequenceOf(ann)
            return decl
        }
    }

    @Test
    fun `propName is extracted from property simpleName`() = test {
        given {
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp("myField"), null)
            expect { "myField" }
            whenever { adapter.propName }
        }
    }

    @Test
    fun `actualPropTypeName is STRING`() = test {
        given {
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(), null)
            expect { STRING }
            whenever { adapter.actualPropTypeName }
        }
    }

    @Test
    fun `hasSingleEntryTransform is false when transform is null`() = test {
        given {
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(), null)
            expect { false }
            whenever { adapter.hasSingleEntryTransform }
        }
    }

    @Test
    fun `hasNullableAssignment is true when type is nullable`() = test {
        given {
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(nullable = true), null)
            expect { true }
            whenever { adapter.hasNullableAssignment }
        }
    }

    @Test
    fun `hasNullableAssignment is false when type is not nullable`() = test {
        given {
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(nullable = false), null)
            expect { false }
            whenever { adapter.hasNullableAssignment }
        }
    }

    @Test
    fun `withVararg defaults to true when no DslProperty annotation`() = test {
        given {
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(), null)
            expect { true }
            whenever { adapter.withVararg }
        }
    }

    @Test
    fun `withProvider defaults to true when no DslProperty annotation`() = test {
        given {
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(), null)
            expect { true }
            whenever { adapter.withProvider }
        }
    }

    @Test
    fun `transformTemplate is null when no singleEntryTransform`() = test {
        given {
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(), null)
            expect { null }
            whenever { adapter.transformTemplate }
        }
    }

    @Test
    fun `transformType is null when no singleEntryTransform`() = test {
        given {
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(), null)
            expect { null }
            whenever { adapter.transformType }
        }
    }

    @Test
    fun `mapDetails returns null when no map group type`() = test {
        given {
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(), null)
            expect { null }
            whenever { adapter.mapDetails() }
        }
    }

    @Test
    fun `mapDetails caches result on second call`() = test {
        given {
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(), null)
            expect { true }
            whenever {
                val first = adapter.mapDetails()
                val second = adapter.mapDetails()
                first === second // same reference (both null, or cached)
            }
        }
    }

    // --- DslProperty annotation branches ---

    @Test
    fun `withVararg is false when DslProperty annotation sets it to false`() = test {
        given {
            val ann = mockDslPropertyAnnotation(withVararg = false, withProvider = true)
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(annotations = sequenceOf(ann)), null)
            expect { false }
            whenever { adapter.withVararg }
        }
    }

    @Test
    fun `withProvider is false when DslProperty annotation sets it to false`() = test {
        given {
            val ann = mockDslPropertyAnnotation(withVararg = true, withProvider = false)
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(annotations = sequenceOf(ann)), null)
            expect { false }
            whenever { adapter.withProvider }
        }
    }

    @Test
    fun `withVararg and withProvider both false when annotation sets both false`() = test {
        given {
            val ann = mockDslPropertyAnnotation(withVararg = false, withProvider = false)
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(annotations = sequenceOf(ann)), null)
            expect { false to false }
            whenever { adapter.withVararg to adapter.withProvider }
        }
    }

    @Test
    fun `withVararg defaults to true when DslProperty annotation has no arguments`() = test {
        given {
            val ann = mockDslPropertyAnnotation(withVararg = null, withProvider = null)
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(annotations = sequenceOf(ann)), null)
            expect { true }
            whenever { adapter.withVararg }
        }
    }

    // --- SingleEntryTransform branches ---

    @Test
    fun `hasSingleEntryTransform is true when transform class is provided`() = test {
        given {
            val transformDecl = mockSingleEntryTransformDecl(
                transformTemplate = "MyType(%N)",
                inputTypeKSType = null
            )
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(), transformDecl)
            expect { true }
            whenever { adapter.hasSingleEntryTransform }
        }
    }

    @Test
    fun `transformTemplate extracts value from SingleEntryTransformDsl annotation`() = test {
        given {
            val transformDecl = mockSingleEntryTransformDecl(
                transformTemplate = "MyType(%N)",
                inputTypeKSType = null
            )
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(), transformDecl)
            expect { "MyType(%N)" }
            whenever { adapter.transformTemplate }
        }
    }

    @Test
    fun `transformTemplate is null when template is blank`() = test {
        given {
            val transformDecl = mockSingleEntryTransformDecl(
                transformTemplate = "   ",
                inputTypeKSType = null
            )
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(), transformDecl)
            expect { null }
            whenever { adapter.transformTemplate }
        }
    }

    @Test
    fun `transformType is null when inputType argument value is not KSType`() = test {
        given {
            val transformDecl = mockSingleEntryTransformDecl(
                transformTemplate = "x",
                inputTypeKSType = null
            )
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(), transformDecl)
            expect { null }
            whenever { adapter.transformType }
        }
    }

    @Test
    fun `mapDetails returns null when type is not parameterized`() = test {
        given {
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(), null)
            expect { null }
            whenever { adapter.mapDetails() }
        }
    }

    @Test
    fun `transformTemplate is null when SingleEntryTransformDsl has no transformTemplate arg`() = test {
        given {
            val transformDecl = mockSingleEntryTransformDecl(
                transformTemplate = null,
                inputTypeKSType = null
            )
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(), transformDecl)
            expect { null }
            whenever { adapter.transformTemplate }
        }
    }

    @Test
    fun `hasSingleEntryTransform false means no annotation to scan`() = test {
        given {
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(), null)
            expect { null to null }
            whenever { adapter.transformTemplate to adapter.transformType }
        }
    }

    @Test
    fun `hasGeneratedDslAnnotation is false when class has no annotations`() = test {
        given {
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(), null)
            expect { false }
            whenever { adapter.hasGeneratedDslAnnotation }
        }
    }

    @Test
    fun `hasGeneratedDslAnnotation is true when class has GeneratedDsl annotation`() = test {
        given {
            val classDecl: KSClassDeclaration = mockk()
            val ann: KSAnnotation = mockk()
            val shortName = mockKSName("GeneratedDsl")
            io.mockk.every { ann.shortName } returns shortName
            io.mockk.every { classDecl.toClassName() } returns ClassName("org.test", "MyClass")
            io.mockk.every { classDecl.annotations } returns sequenceOf(ann)
            io.mockk.every { classDecl.qualifiedName } returns mockKSName("org.test.MyClass")

            val adapter = DefaultPropertySchemaFactoryAdapter(
                mockProp(declarationClass = classDecl), null
            )
            expect { true }
            whenever { adapter.hasGeneratedDslAnnotation }
        }
    }

    @Test
    fun `isGroupElement is false when collection element has no annotations`() = test {
        given {
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(), null)
            expect { false }
            whenever { adapter.isGroupElement }
        }
    }

    @Test
    fun `propertyNonNullableClassName returns class name from declaration`() = test {
        given {
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(), null)
            expect { ClassName("kotlin", "String") }
            whenever { adapter.propertyNonNullableClassName }
        }
    }

    @Test
    fun `propertyClassDeclarationQualifiedName returns qualified name`() = test {
        given {
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(), null)
            expect { "kotlin.String" }
            whenever { adapter.propertyClassDeclarationQualifiedName }
        }
    }

    @Test
    fun `defaultValue is null when not provided`() = test {
        given {
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(), null)
            expect { null }
            whenever { adapter.defaultValue }
        }
    }

    @Test
    fun `transformType returns TypeName when inputType argument is a KSType`() = test {
        given {
            val inputType: KSType = mockk()
            io.mockk.every { inputType.toTypeName() } returns STRING
            val transformDecl = mockSingleEntryTransformDecl(
                transformTemplate = "wrap(%N)",
                inputTypeKSType = inputType
            )
            val adapter = DefaultPropertySchemaFactoryAdapter(mockProp(), transformDecl)
            expect { STRING }
            whenever { adapter.transformType }
        }
    }

    // --- withVararg/withProvider from ListDsl/MapDsl annotation metadata ---

    @Test
    fun `withVararg from listDslWithVararg takes precedence over DslProperty`() = test {
        given {
            val ann = mockDslPropertyAnnotation(withVararg = true, withProvider = true)
            val meta = org.khorum.oss.konstellation.dsl.domain.PropertyAnnotationMetadata(
                listDslWithVararg = false
            )
            val adapter = DefaultPropertySchemaFactoryAdapter(
                mockProp(annotations = sequenceOf(ann)), null,
                annotationMetadata = meta
            )
            expect { false }
            whenever { adapter.withVararg }
        }
    }

    @Test
    fun `withProvider from listDslWithProvider takes precedence over DslProperty`() = test {
        given {
            val ann = mockDslPropertyAnnotation(withVararg = true, withProvider = true)
            val meta = org.khorum.oss.konstellation.dsl.domain.PropertyAnnotationMetadata(
                listDslWithProvider = false
            )
            val adapter = DefaultPropertySchemaFactoryAdapter(
                mockProp(annotations = sequenceOf(ann)), null,
                annotationMetadata = meta
            )
            expect { false }
            whenever { adapter.withProvider }
        }
    }

    @Test
    fun `withVararg from mapDslWithVararg takes precedence when no listDsl`() = test {
        given {
            val meta = org.khorum.oss.konstellation.dsl.domain.PropertyAnnotationMetadata(
                mapDslWithVararg = false
            )
            val adapter = DefaultPropertySchemaFactoryAdapter(
                mockProp(), null,
                annotationMetadata = meta
            )
            expect { false }
            whenever { adapter.withVararg }
        }
    }

    @Test
    fun `withProvider from mapDslWithProvider takes precedence when no listDsl`() = test {
        given {
            val meta = org.khorum.oss.konstellation.dsl.domain.PropertyAnnotationMetadata(
                mapDslWithProvider = false
            )
            val adapter = DefaultPropertySchemaFactoryAdapter(
                mockProp(), null,
                annotationMetadata = meta
            )
            expect { false }
            whenever { adapter.withProvider }
        }
    }

    @Test
    fun `withVararg listDsl takes precedence over mapDsl`() = test {
        given {
            val meta = org.khorum.oss.konstellation.dsl.domain.PropertyAnnotationMetadata(
                listDslWithVararg = true,
                mapDslWithVararg = false
            )
            val adapter = DefaultPropertySchemaFactoryAdapter(
                mockProp(), null,
                annotationMetadata = meta
            )
            expect { true }
            whenever { adapter.withVararg }
        }
    }
}
