package org.khorum.oss.konstellation.dsl.utils

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSValueArgument
import io.mockk.mockk
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.metaDsl.annotation.MapGroupType
import org.junit.jupiter.api.Test

class KSClassDeclarationExtTest : UnitSim() {

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

    @Test
    fun `isGroupDsl returns true when withListGroup is true`() = test {
        given {
            val decl: KSClassDeclaration = mockk()
            every { decl.annotations } returns sequenceOf(mockAnnotation(mapOf("withListGroup" to true)))

            expect { true }
            whenever { decl.isGroupDsl() }
        }
    }

    @Test
    fun `isGroupDsl returns false when withListGroup is false`() = test {
        given {
            val decl: KSClassDeclaration = mockk()
            every { decl.annotations } returns sequenceOf(mockAnnotation(mapOf("withListGroup" to false)))

            expect { false }
            whenever { decl.isGroupDsl() }
        }
    }

    @Test
    fun `isGroupDsl returns false when no annotations`() = test {
        given {
            val decl: KSClassDeclaration = mockk()
            every { decl.annotations } returns emptySequence()

            expect { false }
            whenever { decl.isGroupDsl() }
        }
    }

    @Test
    fun `isGroupDsl returns false on null receiver`() = test {
        given {
            val decl: KSClassDeclaration? = null

            expect { false }
            whenever { decl.isGroupDsl() }
        }
    }

    @Test
    fun `mapGroupType returns enum value when annotation present`() = test {
        given {
            val decl: KSClassDeclaration = mockk()
            every { decl.annotations } returns sequenceOf(mockAnnotation(mapOf("withMapGroup" to "SINGLE")))

            expect { MapGroupType.SINGLE }
            whenever { decl.mapGroupType() }
        }
    }

    @Test
    fun `mapGroupType returns null when no annotation`() = test {
        given {
            val decl: KSClassDeclaration = mockk()
            every { decl.annotations } returns emptySequence()

            expect { null }
            whenever { decl.mapGroupType() }
        }
    }

    @Test
    fun `mapGroupType returns null on null receiver`() = test {
        given {
            val decl: KSClassDeclaration? = null

            expect { null }
            whenever { decl.mapGroupType() }
        }
    }
}
