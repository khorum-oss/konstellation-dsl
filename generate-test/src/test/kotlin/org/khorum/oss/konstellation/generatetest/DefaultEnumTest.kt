package org.khorum.oss.konstellation.generatetest

import org.khorum.oss.geordi.UnitSim
import org.junit.jupiter.api.Test

class DefaultEnumTest : UnitSim() {

    @Test
    fun `DefaultEnum - inferred type default is applied when not set`() = test {
        given {
            expect {
                CrewAssignment(
                    name = "Wesley Crusher",
                    rank = CrewAssignment.Rank.ENSIGN,
                    department = CrewAssignment.Department.BRIDGE
                )
            }

            whenever {
                CrewAssignmentDslBuilder().apply {
                    name = "Wesley Crusher"
                    // rank not set — uses @DefaultEnum("ENSIGN") inferred from property type
                    // department not set — uses @DefaultEnum("BRIDGE") with explicit package/class
                }.build()
            }
        }
    }

    @Test
    fun `DefaultEnum - default can be overridden`() = test {
        given {
            expect {
                CrewAssignment(
                    name = "Geordi La Forge",
                    rank = CrewAssignment.Rank.LIEUTENANT,
                    department = CrewAssignment.Department.ENGINEERING
                )
            }

            whenever {
                CrewAssignmentDslBuilder().apply {
                    name = "Geordi La Forge"
                    rank = CrewAssignment.Rank.LIEUTENANT
                    department = CrewAssignment.Department.ENGINEERING
                }.build()
            }
        }
    }

    @Test
    fun `DefaultEnum - partial override keeps other defaults`() = test {
        given {
            expect {
                CrewAssignment(
                    name = "Data",
                    rank = CrewAssignment.Rank.COMMANDER,
                    department = CrewAssignment.Department.BRIDGE
                )
            }

            whenever {
                CrewAssignmentDslBuilder().apply {
                    name = "Data"
                    rank = CrewAssignment.Rank.COMMANDER
                    // department not set — uses default BRIDGE
                }.build()
            }
        }
    }
}
