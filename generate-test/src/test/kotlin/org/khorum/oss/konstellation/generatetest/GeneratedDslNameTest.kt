package org.khorum.oss.konstellation.generatetest

import org.khorum.oss.geordi.UnitSim
import org.junit.jupiter.api.Test

/**
 * Tests for @GeneratedDsl(name=...) custom builder naming.
 * ShieldConfig uses @GeneratedDsl(name = "Shield"), so the builder is ShieldDslBuilder.
 */
class GeneratedDslNameTest : UnitSim() {

    @Test
    fun `GeneratedDsl name - custom builder name produces correct instance`() = test {
        given {
            expect {
                ShieldConfig(
                    frequency = 257.4,
                    strength = 100,
                    modulation = "rotating"
                )
            }

            // ShieldDslBuilder is generated from @GeneratedDsl(name = "Shield")
            whenever {
                val builder = ShieldDslBuilder()
                builder.frequency = 257.4
                builder.modulation = "rotating"
                // strength defaults to 100 via @DefaultValue("100")
                builder.build()
            }
        }
    }

    @Test
    fun `GeneratedDsl name - default value applies for strength`() = test {
        given {
            expect {
                ShieldConfig(
                    frequency = 100.0,
                    strength = 100
                )
            }

            whenever {
                val builder = ShieldDslBuilder()
                builder.frequency = 100.0
                // strength not set — uses @DefaultValue("100") = 100
                builder.build()
            }
        }
    }

    @Test
    fun `GeneratedDsl name - custom strength overrides default`() = test {
        given {
            expect {
                ShieldConfig(
                    frequency = 100.0,
                    strength = 50,
                    modulation = null
                )
            }

            whenever {
                val builder = ShieldDslBuilder()
                builder.frequency = 100.0
                builder.strength = 50
                builder.build()
            }
        }
    }
}
