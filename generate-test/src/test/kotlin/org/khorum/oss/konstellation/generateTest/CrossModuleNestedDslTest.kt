package org.khorum.oss.konstellation.generateTest

import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.crossmoduletest.CrossModuleNested
import org.junit.jupiter.api.Test

/**
 * Regression tests for cross-module nested DSL detection.
 *
 * `@GeneratedDsl` has `SOURCE` retention, so when a parent DSL in one module
 * references a nested DSL type compiled into another module's JAR, KSP in the
 * downstream module cannot see the annotation on the imported class. Before the
 * fix, this caused the parent's nested property to fall through to
 * `DefaultPropSchema` and no builder accessor was generated — the nested DSL
 * method silently disappeared. Users would end up accidentally calling a
 * top-level `@RootDsl` accessor of the same name instead.
 *
 * Each test below constructs a builder DSL where the nested/list/map element
 * type is [CrossModuleNested] (defined in the `:cross-module-test` Gradle
 * module). The fact that these tests **compile** is already the core
 * regression — without the fix, `nested { }`, `nestedList { nested { } }`, and
 * `nestedMap { "key"(...) { } }` would not exist on the generated builder and
 * the file would fail to compile.
 */
class CrossModuleNestedDslTest : UnitSim() {

    @Test
    fun `direct nested cross-module DSL prop generates fun named after property`() = test {
        given {
            expect {
                CrossModuleParent(
                    nested = CrossModuleNested(label = "hello", count = 7),
                )
            }

            whenever {
                val builder = CrossModuleParentDslBuilder()
                builder.nested {
                    label = "hello"
                    count = 7
                }
                builder.build()
            }
        }
    }

    @Test
    fun `cross-module list element exposes prop-named group accessor`() = test {
        given {
            expect {
                CrossModuleCollectionParent(
                    nestedList = listOf(
                        CrossModuleNested(label = "first", count = 1),
                        CrossModuleNested(label = "second", count = 2),
                    ),
                )
            }

            whenever {
                val builder = CrossModuleCollectionParentDslBuilder()
                builder.nestedList {
                    crossModuleNested {
                        label = "first"
                        count = 1
                    }
                    crossModuleNested {
                        label = "second"
                        count = 2
                    }
                }
                builder.build().copy(nestedMap = null)
            }
        }
    }

    @Test
    fun `cross-module map value exposes prop-named map-group accessor`() = test {
        given {
            expect {
                CrossModuleCollectionParent(
                    nestedMap = mapOf(
                        "a" to CrossModuleNested(label = "alpha", count = 10),
                    ),
                )
            }

            whenever {
                val builder = CrossModuleCollectionParentDslBuilder()
                builder.nestedMap {
                    crossModuleNested("a") {
                        label = "alpha"
                        count = 10
                    }
                }
                builder.build().copy(nestedList = null)
            }
        }
    }
}
