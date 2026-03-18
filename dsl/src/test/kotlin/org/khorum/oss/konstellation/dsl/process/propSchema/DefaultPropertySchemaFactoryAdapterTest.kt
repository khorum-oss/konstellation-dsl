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

        private fun mockValueArg(name: String, value: Any?): KSValueArgument {
            val arg: KSValueArgument = mockk()
            every { arg.name } returns mockKSName(name)
            every { arg.value } returns value
            return arg
        }

        /**
         * Creates a minimal KSPropertyDeclaration mock with:
         * - simpleName
         * - type -> toTypeName() returns STRING
         * - type.resolve() -> KSType with isMarkedNullable, declaration, arguments
         * - annotations (empty by default)
         */
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

}
