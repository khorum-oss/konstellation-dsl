package org.khorum.oss.konstellation.generateTest

import org.khorum.oss.konstellation.metaDsl.annotation.GeneratedDsl
import org.khorum.oss.konstellation.metaDsl.annotation.RootDsl

/**
 * Demonstrates @RootDsl on a property to generate root entry point functions
 * with custom names and aliases.
 *
 * This generates top-level functions:
 * - fun mission(block: FleetCommandDslBuilder.() -> Unit): FleetCommand
 * - fun missionControl(block: FleetCommandDslBuilder.() -> Unit): FleetCommand
 */
@GeneratedDsl
data class MissionControl(
    val missionName: String,

    @RootDsl(name = "mission", alias = "missionControl")
    val command: FleetCommand
)
