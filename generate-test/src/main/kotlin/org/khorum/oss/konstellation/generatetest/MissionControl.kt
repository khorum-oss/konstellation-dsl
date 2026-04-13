package org.khorum.oss.konstellation.generatetest

import org.khorum.oss.konstellation.metaDsl.annotation.GeneratedDsl
import org.khorum.oss.konstellation.metaDsl.annotation.RootDsl

/**
 * Demonstrates @RootDsl at class level to generate root entry point functions
 * with custom names and aliases.
 *
 * This generates top-level functions:
 * - fun missionControl(block: MissionControlDslBuilder.() -> Unit): MissionControl
 */
@RootDsl(name = "missionControl")
@GeneratedDsl
data class MissionControl(
    val missionName: String,
    val command: FleetCommand
)
