package org.khorum.oss.konstellation.generatetest

import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.generatetest.nested.Version
import org.junit.jupiter.api.Test

/**
 * Tests for @DefaultValue annotation:
 * - String defaults
 * - Int defaults (literal, not string)
 * - Class reference defaults (e.g., Version.V1)
 */
class DefaultValueTest : UnitSim() {

    @Test
    fun `DefaultValue - string default is applied when not set`() = test {
        given {
            expect {
                StarShip(
                    name = "Test Ship",
                    commanderNames = listOf("Commander"),
                    crewMap = mapOf("CAPTAIN" to Passenger(name = "Captain", rank = Passenger.Rank.CAPTAIN)),
                    defaultString = "DEFAULT",
                    version = Version.V1
                )
            }

            whenever {
                starShip {
                    name = "Test Ship"
                    commanderNames("Commander")
                    crewMap {
                        passenger("CAPTAIN") {
                            name = "Captain"
                            rank = Passenger.Rank.CAPTAIN
                        }
                    }
                    // defaultString and version not set — use @DefaultValue defaults
                }
            }
        }
    }

    @Test
    fun `DefaultValue - string default can be overridden`() = test {
        given {
            expect {
                StarShip(
                    name = "Test Ship",
                    commanderNames = listOf("Commander"),
                    crewMap = mapOf("CAPTAIN" to Passenger(name = "Captain", rank = Passenger.Rank.CAPTAIN)),
                    defaultString = "CUSTOM"
                )
            }

            whenever {
                starShip {
                    name = "Test Ship"
                    commanderNames("Commander")
                    crewMap {
                        passenger("CAPTAIN") {
                            name = "Captain"
                            rank = Passenger.Rank.CAPTAIN
                        }
                    }
                    defaultString = "CUSTOM"
                }
            }
        }
    }

    @Test
    fun `DefaultValue - int default is applied as literal`() = test {
        given {
            expect {
                FleetCommand(
                    commandName = "Test",
                    shipNames = listOf("Enterprise"),
                    shipAssignments = mapOf("Enterprise" to "Sector 001"),
                    priority = 1 // default from @DefaultValue("1")
                )
            }

            whenever {
                val builder = FleetCommandDslBuilder()
                builder.commandName = "Test"
                builder.shipNames("Enterprise")
                builder.shipAssignments("Enterprise" to "Sector 001")
                // priority not set — uses @DefaultValue("1") = 1 (int literal)
                builder.build()
            }
        }
    }

    @Test
    fun `DefaultValue - int default can be overridden`() = test {
        given {
            expect {
                FleetCommand(
                    commandName = "Test",
                    shipNames = listOf("Enterprise"),
                    shipAssignments = mapOf("Enterprise" to "Sector 001"),
                    priority = 5
                )
            }

            whenever {
                val builder = FleetCommandDslBuilder()
                builder.commandName = "Test"
                builder.shipNames("Enterprise")
                builder.shipAssignments("Enterprise" to "Sector 001")
                builder.priority = 5
                builder.build()
            }
        }
    }

    @Test
    fun `DefaultValue - class reference default Version V1 is applied`() = test {
        given {
            expect {
                StarShip(
                    name = "Test Ship",
                    commanderNames = listOf("Commander"),
                    crewMap = mapOf("CAPTAIN" to Passenger(name = "Captain", rank = Passenger.Rank.CAPTAIN)),
                    version = Version.V1
                )
            }

            whenever {
                starShip {
                    name = "Test Ship"
                    commanderNames("Commander")
                    crewMap {
                        passenger("CAPTAIN") {
                            name = "Captain"
                            rank = Passenger.Rank.CAPTAIN
                        }
                    }
                    // version not set — uses @DefaultValue("Version.V1", packageName=..., className="Version")
                }
            }
        }
    }
}
