package org.khorum.oss.konstellation.generateTest

import org.khorum.oss.geordi.UnitSim
import org.junit.jupiter.api.Test

/**
 * Tests for @MapDsl annotation features:
 * - minSize/maxSize validation
 * - withVararg/withProvider accessor control
 */
class MapDslTest : UnitSim() {

    // ========== @MapDsl Happy Path ==========

    @Test
    fun `MapDsl - happy path with vararg and provider accessors`() = test {
        given {
            expect {
                FleetCommand(
                    commandName = "Deploy",
                    shipNames = listOf("Enterprise"),
                    shipAssignments = mapOf("Enterprise" to "Sector 001"),
                    sectorCodes = mapOf("Alpha" to 1, "Beta" to 2),
                    commandCodes = mapOf("code1" to "red", "code2" to "blue")
                )
            }

            whenever {
                fleetCommand {
                    commandName = "Deploy"
                    shipNames("Enterprise")
                    shipAssignments("Enterprise" to "Sector 001")
                    // withProvider=false → vararg only
                    sectorCodes("Alpha" to 1, "Beta" to 2)
                    // withVararg=false → provider only
                    commandCodes {
                        this["code1"] = "red"
                        this["code2"] = "blue"
                    }
                }
            }
        }
    }

    // ========== @MapDsl minSize Validation ==========

    @Test
    fun `MapDsl - minSize validation fails when map is empty`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>(null) {
                fleetCommand {
                    commandName = "No Assignments"
                    shipNames("Enterprise")
                    shipAssignments() // empty - violates minSize=1
                }
            }
        }
    }

    // ========== @MapDsl maxSize Validation ==========

    @Test
    fun `MapDsl - maxSize validation fails when map exceeds maximum`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>(null) {
                fleetCommand {
                    commandName = "Too Many Assignments"
                    shipNames("Enterprise")
                    // 6 entries exceeds maxSize=5
                    shipAssignments(
                        "Ship1" to "S1", "Ship2" to "S2", "Ship3" to "S3",
                        "Ship4" to "S4", "Ship5" to "S5", "Ship6" to "S6"
                    )
                }
            }
        }
    }

    // ========== @MapDsl both vararg and provider (default) ==========

    @Test
    fun `MapDsl - default generates both vararg and provider for shipAssignments`() = test {
        given {
            expect {
                FleetCommand(
                    commandName = "Mixed",
                    shipNames = listOf("Enterprise"),
                    shipAssignments = mapOf("Enterprise" to "Sector 001", "Defiant" to "Sector 002")
                )
            }

            // Test provider function
            whenever {
                fleetCommand {
                    commandName = "Mixed"
                    shipNames("Enterprise")
                    shipAssignments {
                        this["Enterprise"] = "Sector 001"
                        this["Defiant"] = "Sector 002"
                    }
                }
            }
        }
    }

    private fun fleetCommand(block: FleetCommandDslBuilder.() -> Unit): FleetCommand {
        val builder = FleetCommandDslBuilder()
        builder.block()
        return builder.build()
    }
}
