package org.khorum.oss.konstellation.dsl.domain

import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.exception.RequiredDataException
import org.khorum.oss.konstellation.dsl.utils.Logger
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BuilderConfigTest : UnitSim() {
    private val logger = Logger("BuilderConfigTest")

    private fun requiredMap(): Map<String, String?> = mapOf(
        "projectRootClasspath" to "org.khorum.oss.test",
        "dslBuilderClasspath" to "org.khorum.oss.test.TestBuilder"
    )

    private fun fullMap(overrides: Map<String, String?> = emptyMap()): Map<String, String?> =
        requiredMap() + mapOf(
            "rootDslFileClasspath" to "org.khorum.oss.test.root",
            "dslMarkerClass" to "org.khorum.oss.test.Marker",
            "isIgnored" to "false"
        ) + overrides

    @Nested
    inner class Construction {
        @Test
        fun `happy path - all keys present`() = test {
            given {
                val config = BuilderConfig(fullMap(), logger)

                expect { "org.khorum.oss.test" }

                whenever { config.projectRootClasspath }
            }
        }

        @Test
        fun `happy path - dslBuilderClasspath is set`() = test {
            given {
                val config = BuilderConfig(fullMap(), logger)

                expect { "org.khorum.oss.test.TestBuilder" }

                whenever { config.dslBuilderClasspath }
            }
        }

        @Test
        fun `happy path - dslMarkerClass is set`() = test {
            given {
                val config = BuilderConfig(fullMap(), logger)

                expect { "org.khorum.oss.test.Marker" }

                whenever { config.dslMarkerClass }
            }
        }
    }

    @Nested
    inner class IsIgnored {
        @Test
        fun `defaults to false when key missing`() = test {
            given {
                val config = BuilderConfig(requiredMap(), logger)

                expect { false }

                whenever { config.isIgnored }
            }
        }

        @Test
        fun `is true when isIgnored is true`() = test {
            given {
                val config = BuilderConfig(fullMap(mapOf("isIgnored" to "true")), logger)

                expect { true }

                whenever { config.isIgnored }
            }
        }
    }

    @Nested
    inner class RequiredFields {
        @Test
        fun `throws RequiredDataException when projectRootClasspath missing`() = test<Unit> {
            given {
                wheneverThrows<RequiredDataException>("value is required. value: projectRootClasspath") {
                    BuilderConfig(
                        mapOf("dslBuilderClasspath" to "org.khorum.oss.test.TestBuilder"),
                        logger
                    )
                }
            }
        }

        @Test
        fun `throws RequiredDataException when dslBuilderClasspath missing`() = test<Unit> {
            given {
                wheneverThrows<RequiredDataException>("value is required. value: dslBuilderClasspath") {
                    BuilderConfig(
                        mapOf("projectRootClasspath" to "org.khorum.oss.test"),
                        logger
                    )
                }
            }
        }
    }

    @Nested
    inner class RootDslFileClasspath {
        @Test
        fun `returns override when provided`() = test {
            given {
                val config = BuilderConfig(fullMap(), logger)

                expect { "org.khorum.oss.test.root" }

                whenever { config.rootDslFileClasspath() }
            }
        }

        @Test
        fun `falls back to projectRootClasspath when not provided`() = test {
            given {
                val config = BuilderConfig(requiredMap(), logger)

                expect { "org.khorum.oss.test" }

                whenever { config.rootDslFileClasspath() }
            }
        }
    }

    @Nested
    inner class PrintDebug {
        @Test
        fun `executes without error`() = test {
            given {
                val config = BuilderConfig(fullMap(), logger)

                expect {  }

                whenever { config.printDebug() }
            }
        }
    }
}
