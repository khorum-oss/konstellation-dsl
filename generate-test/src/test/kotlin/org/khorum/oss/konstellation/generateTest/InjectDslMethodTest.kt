package org.khorum.oss.konstellation.generateTest

import org.khorum.oss.geordi.UnitSim
import org.junit.jupiter.api.Test

class InjectDslMethodTest : UnitSim() {

    @Test
    fun `InjectDslMethod - injected method is callable on the builder`() = test {
        given {
            expect { "Picard - CAPTAIN" }

            whenever {
                CrewAssignmentDslBuilder().apply {
                    name = "Picard"
                    rank = CrewAssignment.Rank.CAPTAIN
                }.designation()
            }
        }
    }

    @Test
    fun `InjectDslMethod - injected method uses builder property values`() = test {
        given {
            expect { "Riker - COMMANDER" }

            whenever {
                CrewAssignmentDslBuilder().apply {
                    name = "Riker"
                    rank = CrewAssignment.Rank.COMMANDER
                    department = CrewAssignment.Department.BRIDGE
                }.designation()
            }
        }
    }

    @Test
    fun `InjectDslMethod - injected method reflects default property values`() = test {
        given {
            expect { "Wesley - ENSIGN" }

            whenever {
                CrewAssignmentDslBuilder().apply {
                    name = "Wesley"
                    // rank defaults to ENSIGN via @DefaultEnum
                }.designation()
            }
        }
    }
}
