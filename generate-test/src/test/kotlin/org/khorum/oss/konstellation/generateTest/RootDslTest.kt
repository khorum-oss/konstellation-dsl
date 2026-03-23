package org.khorum.oss.konstellation.generateTest

import org.khorum.oss.geordi.UnitSim
import org.junit.jupiter.api.Test

/**
 * Tests for @RootDsl annotation features:
 * - Custom root function name
 * - Alias root function
 */
class RootDslTest : UnitSim() {

    @Test
    fun `RootDsl - mission function generates FleetCommand via custom root name`() = test {
        given {
            expect {
                FleetCommand(
                    commandName = "Patrol Alpha",
                    shipNames = listOf("Enterprise"),
                    shipAssignments = mapOf("Enterprise" to "Sector 001")
                )
            }

            // mission() is generated from @RootDsl(name = "mission") on MissionControl.command
            whenever {
                mission {
                    commandName = "Patrol Alpha"
                    shipNames("Enterprise")
                    shipAssignments("Enterprise" to "Sector 001")
                }
            }
        }
    }

    @Test
    fun `RootDsl - missionControl alias function generates FleetCommand`() = test {
        given {
            expect {
                FleetCommand(
                    commandName = "Patrol Beta",
                    shipNames = listOf("Voyager"),
                    shipAssignments = mapOf("Voyager" to "Delta Quadrant")
                )
            }

            // missionControl() is generated from @RootDsl(alias = "missionControl")
            whenever {
                missionControl {
                    commandName = "Patrol Beta"
                    shipNames("Voyager")
                    shipAssignments("Voyager" to "Delta Quadrant")
                }
            }
        }
    }
}
