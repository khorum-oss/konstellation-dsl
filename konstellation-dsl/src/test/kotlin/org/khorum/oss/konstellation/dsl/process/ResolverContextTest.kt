package org.khorum.oss.konstellation.dsl.process

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import io.mockk.every
import io.mockk.mockk
import org.khorum.oss.geordi.UnitSim
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for [ResolverContext], which underpins cross-module nested DSL
 * detection. Cross-module detection matters because `@GeneratedDsl` has
 * `SOURCE` retention: when a parent DSL's property type is compiled into a
 * separate module's JAR, KSP cannot see the annotation on the imported type.
 * [ResolverContext.hasGeneratedDslBuilderFor] falls back to looking up
 * `${qualifiedName}DslBuilder` via the active [Resolver].
 */
class ResolverContextTest : UnitSim() {

    @AfterEach
    fun cleanup() {
        // Defensive — `withResolver` normally restores state itself.
        @Suppress("UNCHECKED_CAST")
        val field = ResolverContext::class.java.getDeclaredField("current")
        field.isAccessible = true
        field.set(ResolverContext, null)
    }

    private fun mockKSName(value: String): KSName {
        val name: KSName = mockk(relaxed = true)
        every { name.asString() } returns value
        return name
    }

    @Test
    fun `current is null outside of withResolver`() = test {
        given {
            expect { null }
            whenever { ResolverContext.current }
        }
    }

    @Test
    fun `withResolver exposes the resolver inside the block and clears it after`() = test {
        given {
            val resolver: Resolver = mockk(relaxed = true)
            expect { resolver to null }
            whenever {
                val inside = ResolverContext.withResolver(resolver) { ResolverContext.current }
                val after = ResolverContext.current
                inside to after
            }
        }
    }

    @Test
    fun `withResolver restores previous resolver even if block throws`() = test {
        given {
            val resolver: Resolver = mockk(relaxed = true)
            expect { null }
            whenever {
                runCatching {
                    ResolverContext.withResolver(resolver) {
                        error("boom")
                    }
                }
                ResolverContext.current
            }
        }
    }

    @Test
    fun `hasGeneratedDslBuilderFor returns false when no resolver is active`() = test {
        given {
            val decl: KSClassDeclaration = mockk(relaxed = true)
            expect { false }
            whenever { ResolverContext.hasGeneratedDslBuilderFor(decl) }
        }
    }

    @Test
    fun `hasGeneratedDslBuilderFor returns false when declaration has no qualified name`() = test {
        given {
            val resolver: Resolver = mockk(relaxed = true)
            val decl: KSClassDeclaration = mockk(relaxed = true)
            io.mockk.every { decl.qualifiedName } answers { null }
            expect { false }
            whenever {
                ResolverContext.withResolver(resolver) {
                    ResolverContext.hasGeneratedDslBuilderFor(decl)
                }
            }
        }
    }

    @Test
    fun `hasGeneratedDslBuilderFor returns true when resolver finds sibling DslBuilder`() = test {
        given {
            val builderName: KSName = mockk(relaxed = true)
            val builderDecl: KSClassDeclaration = mockk(relaxed = true)
            val resolver: Resolver = mockk(relaxed = true)
            io.mockk.every { resolver.getKSNameFromString("org.example.FooDslBuilder") } returns builderName
            io.mockk.every { resolver.getClassDeclarationByName(builderName) } returns builderDecl
            val decl: KSClassDeclaration = mockk(relaxed = true)
            io.mockk.every { decl.qualifiedName } returns mockKSName("org.example.Foo")
            expect { true }
            whenever {
                ResolverContext.withResolver(resolver) {
                    ResolverContext.hasGeneratedDslBuilderFor(decl)
                }
            }
        }
    }

    @Test
    fun `hasGeneratedDslBuilderFor returns false when resolver cannot find sibling DslBuilder`() = test {
        given {
            val builderName: KSName = mockk(relaxed = true)
            val resolver: Resolver = mockk(relaxed = true)
            io.mockk.every { resolver.getKSNameFromString("org.example.BarDslBuilder") } returns builderName
            io.mockk.every { resolver.getClassDeclarationByName(builderName) } answers { null }
            val decl: KSClassDeclaration = mockk(relaxed = true)
            io.mockk.every { decl.qualifiedName } returns mockKSName("org.example.Bar")
            expect { false }
            whenever {
                ResolverContext.withResolver(resolver) {
                    ResolverContext.hasGeneratedDslBuilderFor(decl)
                }
            }
        }
    }
}
