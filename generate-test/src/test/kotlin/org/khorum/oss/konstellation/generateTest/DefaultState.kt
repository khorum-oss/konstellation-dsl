package org.khorum.oss.konstellation.generateTest

import org.khorum.oss.geordi.UnitSim
import kotlin.test.Test

class DefaultState : UnitSim() {

    @Test
    fun `default empty states`() = test {
        given {
            expect {
                NavigationConfig(
                    routeName = "",
                    waypointCount = 0,
                    distanceTraveled = 0L,
                    heading = 0.0,
                    speed =  0.0f,
                    autopilotEnabled = false,
                    collisionAvoidance = true,
                    waypoints = mutableListOf(),
                    sectorData = mutableMapOf()
                )
            }

            whenever {
                navigationConfig {

                }
            }
        }
    }
}