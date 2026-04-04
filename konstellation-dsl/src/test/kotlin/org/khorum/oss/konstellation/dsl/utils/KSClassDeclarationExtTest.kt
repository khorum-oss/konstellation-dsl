package org.khorum.oss.konstellation.dsl.utils

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSValueArgument
import io.mockk.mockk
import org.khorum.oss.geordi.UnitSim
import org.junit.jupiter.api.Test

class KSClassDeclarationExtTest : UnitSim() {

    private fun mockAnnotationWithName(name: String): KSAnnotation {
        val ann: KSAnnotation = mockk()
        val shortName: KSName = mockk()
        every { shortName.asString() } returns name
        every { ann.shortName } returns shortName
        return ann
    }

    @Test
    fun `isGroupDsl returns true when ListDsl annotation is present`() = test {
        given {
            val decl: KSClassDeclaration = mockk()
            every { decl.annotations } returns sequenceOf(mockAnnotationWithName("GeneratedDsl"))

            expect { true }
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
    fun `hasMapDsl returns true when MapDsl annotation is present`() = test {
        given {
            val decl: KSClassDeclaration = mockk()
            every { decl.annotations } returns sequenceOf(mockAnnotationWithName("GeneratedDsl"))

            expect { true }
            whenever { decl.hasMapDsl() }
        }
    }

    @Test
    fun `hasMapDsl returns false when no annotation`() = test {
        given {
            val decl: KSClassDeclaration = mockk()
            every { decl.annotations } returns emptySequence()

            expect { false }
            whenever { decl.hasMapDsl() }
        }
    }

    @Test
    fun `hasMapDsl returns false on null receiver`() = test {
        given {
            val decl: KSClassDeclaration? = null

            expect { false }
            whenever { decl.hasMapDsl() }
        }
    }

    @Test
    fun `isGroupDsl returns false when annotation has non-matching name`() = test {
        given {
            val decl: KSClassDeclaration = mockk()
            every { decl.annotations } returns sequenceOf(mockAnnotationWithName("OtherAnnotation"))

            expect { false }
            whenever { decl.isGroupDsl() }
        }
    }

    @Test
    fun `hasMapDsl returns false when annotation has non-matching name`() = test {
        given {
            val decl: KSClassDeclaration = mockk()
            every { decl.annotations } returns sequenceOf(mockAnnotationWithName("OtherAnnotation"))

            expect { false }
            whenever { decl.hasMapDsl() }
        }
    }
}
