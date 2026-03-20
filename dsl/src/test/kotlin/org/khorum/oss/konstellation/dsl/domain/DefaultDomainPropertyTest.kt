package org.khorum.oss.konstellation.dsl.domain

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.ksp.toTypeName
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.khorum.oss.geordi.UnitSim
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class DefaultDomainPropertyTest : UnitSim() {

    companion object {
        private lateinit var mockTypeRef: KSTypeReference
        private lateinit var mockSimpleName: KSName
        private lateinit var mockProp: KSPropertyDeclaration

        @JvmStatic
        @BeforeAll
        fun setupAll() {
            mockkStatic(KSTypeReference::toTypeName)
            mockTypeRef = mockk()
            every { mockTypeRef.toTypeName() } returns STRING.copy(nullable = true)
            mockSimpleName = mockk()
            every { mockSimpleName.asString() } returns "name"
            mockProp = mockk()
            every { mockProp.type } returns mockTypeRef
            every { mockProp.simpleName } returns mockSimpleName
        }

        @JvmStatic
        @AfterAll
        fun teardownAll() {
            unmockkStatic(KSTypeReference::toTypeName)
        }
    }

    @Test
    fun `simpleName returns the property simple name`() = test {
        given {
            val prop = DefaultDomainProperty(0, 2, mockProp, emptyMap())
            expect { "name" }
            whenever { prop.simpleName() }
        }
    }

    @Test
    fun `continueBranch returns true when index does not equal lastIndex`() = test {
        given {
            val prop = DefaultDomainProperty(0, 2, mockProp, emptyMap())
            expect { true }
            whenever { prop.continueBranch() }
        }
    }

    @Test
    fun `continueBranch returns false when index equals lastIndex`() = test {
        given {
            val prop = DefaultDomainProperty(2, 2, mockProp, emptyMap())
            expect { false }
            whenever { prop.continueBranch() }
        }
    }

    @Test
    fun `type produces non-nullable TypeName`() = test {
        given {
            val prop = DefaultDomainProperty(0, 0, mockProp, emptyMap())
            expect { false }
            whenever { prop.type!!.isNullable }
        }
    }

    @Test
    fun `singleEntryTransformString returns class name when type matches`() = test {
        given {
            val mockClass: KSClassDeclaration = mockk()
            every { mockClass.toString() } returns "TransformClass"
            val prop = DefaultDomainProperty(0, 0, mockProp, mapOf("kotlin.String" to mockClass))
            expect { "TransformClass" }
            whenever { prop.singleEntryTransformString() }
        }
    }

    @Test
    fun `singleEntryTransformString returns null when type not in map`() = test {
        given {
            val prop = DefaultDomainProperty(0, 0, mockProp, emptyMap())
            expect { null }
            whenever { prop.singleEntryTransformString() }
        }
    }
}
