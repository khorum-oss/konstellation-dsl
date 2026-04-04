package org.khorum.oss.konstellation.generateTest

import org.khorum.oss.geordi.UnitSim
import org.junit.jupiter.api.Test

/**
 * Tests for @RootDsl annotation features:
 * - Class-level @RootDsl with custom name
 */
class RootDslTest : UnitSim() {

    @Test
    fun `RootDsl - missionControl function generates MissionControl via custom root name`() = test {
        given {
            expect {
                MissionControl(
                    missionName = "Patrol Alpha",
                    command = FleetCommand(
                        commandName = "Alpha Command",
                        shipNames = listOf("Enterprise"),
                        shipAssignments = mapOf("Enterprise" to "Sector 001")
                    )
                )
            }

            // missionControl() is generated from @RootDsl(name = "missionControl") on MissionControl class
            whenever {
                missionControl {
                    missionName = "Patrol Alpha"
                    command {
                        commandName = "Alpha Command"
                        shipNames("Enterprise")
                        shipAssignments("Enterprise" to "Sector 001")
                    }
                }
            }
        }
    }
}
