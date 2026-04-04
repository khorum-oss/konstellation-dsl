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

    // ── @DefaultFalse boolean tests ─────────────────────────────────────

    @Test
    fun `@DefaultFalse property defaults to false when no accessor called`() = test {
        given {
            expect { false }
            whenever { navigationConfig { }.autopilotEnabled }
        }
    }

    @Test
    fun `@DefaultFalse accessor with no arg defaults to false`() = test {
        given {
            expect { false }
            whenever {
                navigationConfig {
                    autopilotEnabled()
                }.autopilotEnabled
            }
        }
    }

    @Test
    fun `@DefaultFalse accessor with true enables the flag`() = test {
        given {
            expect { true }
            whenever {
                navigationConfig {
                    autopilotEnabled(true)
                }.autopilotEnabled
            }
        }
    }

    @Test
    fun `@DefaultFalse accessor with explicit false keeps it false`() = test {
        given {
            expect { false }
            whenever {
                navigationConfig {
                    autopilotEnabled(false)
                }.autopilotEnabled
            }
        }
    }

    // ── @DefaultTrue boolean tests ──────────────────────────────────────

    @Test
    fun `@DefaultTrue property defaults to true when no accessor called`() = test {
        given {
            expect { true }
            whenever { navigationConfig { }.collisionAvoidance }
        }
    }

    @Test
    fun `@DefaultTrue accessor with no arg defaults to true`() = test {
        given {
            expect { true }
            whenever {
                navigationConfig {
                    collisionAvoidance()
                }.collisionAvoidance
            }
        }
    }

    @Test
    fun `@DefaultTrue accessor with false disables the flag`() = test {
        given {
            expect { false }
            whenever {
                navigationConfig {
                    collisionAvoidance(false)
                }.collisionAvoidance
            }
        }
    }

    @Test
    fun `@DefaultTrue accessor with explicit true keeps it true`() = test {
        given {
            expect { true }
            whenever {
                navigationConfig {
                    collisionAvoidance(true)
                }.collisionAvoidance
            }
        }
    }

    // ── Combined boolean state tests ────────────────────────────────────

    @Test
    fun `both booleans set to true`() = test {
        given {
            expect {
                NavigationConfig(
                    routeName = "",
                    waypointCount = 0,
                    distanceTraveled = 0L,
                    heading = 0.0,
                    speed = 0.0f,
                    autopilotEnabled = true,
                    collisionAvoidance = true,
                    waypoints = mutableListOf(),
                    sectorData = mutableMapOf()
                )
            }
            whenever {
                navigationConfig {
                    autopilotEnabled(true)
                    collisionAvoidance(true)
                }
            }
        }
    }

    @Test
    fun `both booleans set to false`() = test {
        given {
            expect {
                NavigationConfig(
                    routeName = "",
                    waypointCount = 0,
                    distanceTraveled = 0L,
                    heading = 0.0,
                    speed = 0.0f,
                    autopilotEnabled = false,
                    collisionAvoidance = false,
                    waypoints = mutableListOf(),
                    sectorData = mutableMapOf()
                )
            }
            whenever {
                navigationConfig {
                    autopilotEnabled(false)
                    collisionAvoidance(false)
                }
            }
        }
    }

    @Test
    fun `@DefaultFalse toggled on then off remains false`() = test {
        given {
            expect { false }
            whenever {
                navigationConfig {
                    autopilotEnabled(true)
                    autopilotEnabled(false)
                }.autopilotEnabled
            }
        }
    }

    @Test
    fun `@DefaultTrue toggled off then on remains true`() = test {
        given {
            expect { true }
            whenever {
                navigationConfig {
                    collisionAvoidance(false)
                    collisionAvoidance(true)
                }.collisionAvoidance
            }
        }
    }

    // ── SensorConfig @DefaultState(TRUE/FALSE) verbose form ─────────────

    @Test
    fun `SensorConfig @DefaultState TRUE defaults to true`() = test {
        given {
            expect { true }
            whenever {
                val builder = SensorConfigDslBuilder()
                builder.build().autoCalibrate
            }
        }
    }

    @Test
    fun `SensorConfig @DefaultState FALSE defaults to false`() = test {
        given {
            expect { false }
            whenever {
                val builder = SensorConfigDslBuilder()
                builder.build().enabled
            }
        }
    }

    @Test
    fun `SensorConfig @DefaultState TRUE can be set to false`() = test {
        given {
            expect { false }
            whenever {
                val builder = SensorConfigDslBuilder()
                builder.autoCalibrate(false)
                builder.build().autoCalibrate
            }
        }
    }

    @Test
    fun `SensorConfig @DefaultState FALSE can be set to true`() = test {
        given {
            expect { true }
            whenever {
                val builder = SensorConfigDslBuilder()
                builder.enabled(true)
                builder.build().enabled
            }
        }
    }
}