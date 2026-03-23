package org.khorum.oss.konstellation.generateTest

import org.khorum.oss.geordi.UnitSim
import org.junit.jupiter.api.Test

/**
 * Tests for @ListDsl annotation features:
 * - minSize/maxSize validation
 * - uniqueElements (distinct)
 * - sorted
 * - withVararg/withProvider accessor control
 */
class ListDslTest : UnitSim() {

    // ========== @ListDsl Happy Path ==========

    @Test
    fun `ListDsl - happy path with uniqueElements and sorted`() = test {
        given {
            // Input has duplicates and is unsorted; @ListDsl(uniqueElements=true, sorted=true) should fix that
            expect {
                FleetCommand(
                    commandName = "Alpha Strike",
                    shipNames = listOf("Defiant", "Enterprise", "Voyager"),
                    shipAssignments = mapOf("Enterprise" to "Sector 001")
                )
            }

            whenever {
                fleetCommand {
                    commandName = "Alpha Strike"
                    // Duplicates and unsorted - should be deduplicated and sorted
                    shipNames("Voyager", "Enterprise", "Defiant", "Enterprise")
                    shipAssignments("Enterprise" to "Sector 001")
                }
            }
        }
    }

    @Test
    fun `ListDsl - provider function for shipNames`() = test {
        given {
            expect {
                FleetCommand(
                    commandName = "Patrol",
                    shipNames = listOf("Enterprise", "Voyager"),
                    shipAssignments = mapOf("Enterprise" to "Sector 001")
                )
            }

            whenever {
                fleetCommand {
                    commandName = "Patrol"
                    shipNames { addAll(listOf("Voyager", "Enterprise")) }
                    shipAssignments("Enterprise" to "Sector 001")
                }
            }
        }
    }

    // ========== @ListDsl minSize Validation ==========

    @Test
    fun `ListDsl - minSize validation fails when list is empty`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>(null) {
                fleetCommand {
                    commandName = "Empty Fleet"
                    shipNames() // empty - violates minSize=1
                    shipAssignments("X" to "Y")
                }
            }
        }
    }

    // ========== @ListDsl maxSize Validation ==========

    @Test
    fun `ListDsl - maxSize validation fails when list exceeds maximum`() = test<Unit> {
        given {
            wheneverThrows<IllegalArgumentException>(null) {
                fleetCommand {
                    commandName = "Huge Fleet"
                    // 11 unique names exceeds maxSize=10
                    shipNames(
                        "Ship1", "Ship2", "Ship3", "Ship4", "Ship5",
                        "Ship6", "Ship7", "Ship8", "Ship9", "Ship10", "Ship11"
                    )
                    shipAssignments("X" to "Y")
                }
            }
        }
    }

    // ========== @ListDsl withProvider=false (vararg only) ==========

    @Test
    fun `ListDsl - withProvider false generates only vararg for priorities`() = test {
        given {
            expect {
                FleetCommand(
                    commandName = "Priority Mission",
                    shipNames = listOf("Enterprise"),
                    shipAssignments = mapOf("Enterprise" to "Sector 001"),
                    priorities = listOf(1, 2, 3)
                )
            }

            whenever {
                fleetCommand {
                    commandName = "Priority Mission"
                    shipNames("Enterprise")
                    shipAssignments("Enterprise" to "Sector 001")
                    priorities(1, 2, 3) // vararg accessor
                }
            }
        }
    }

    // ========== @ListDsl withVararg=false (provider only) ==========

    @Test
    fun `ListDsl - withVararg false generates only provider for objectives`() = test {
        given {
            expect {
                FleetCommand(
                    commandName = "Exploration",
                    shipNames = listOf("Voyager"),
                    shipAssignments = mapOf("Voyager" to "Delta Quadrant"),
                    objectives = listOf("Map nebula", "First contact")
                )
            }

            whenever {
                fleetCommand {
                    commandName = "Exploration"
                    shipNames("Voyager")
                    shipAssignments("Voyager" to "Delta Quadrant")
                    objectives { addAll(listOf("Map nebula", "First contact")) }
                }
            }
        }
    }

    // Helper: create a FleetCommand using the generated DSL
    private fun fleetCommand(block: FleetCommandDslBuilder.() -> Unit): FleetCommand {
        val builder = FleetCommandDslBuilder()
        builder.block()
        return builder.build()
    }
}
