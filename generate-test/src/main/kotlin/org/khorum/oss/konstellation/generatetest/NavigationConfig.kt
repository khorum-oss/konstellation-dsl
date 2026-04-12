package org.khorum.oss.konstellation.generatetest

import org.khorum.oss.konstellation.metaDsl.annotation.GeneratedDsl
import org.khorum.oss.konstellation.metaDsl.annotation.RootDsl
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard.DefaultEmptyList
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard.DefaultEmptyMap
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard.DefaultEmptyString
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard.DefaultFalse
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard.DefaultTrue
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard.DefaultZeroDouble
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard.DefaultZeroFloat
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard.DefaultZeroInt
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.state.standard.DefaultZeroLong

/**
 * Domain class demonstrating shorthand default state annotations.
 * These are equivalent to @DefaultState(DefaultStateType.X) but more concise.
 */
@RootDsl
@GeneratedDsl
data class NavigationConfig(
    @DefaultEmptyString
    val routeName: String,

    @DefaultZeroInt
    val waypointCount: Int,

    @DefaultZeroLong
    val distanceTraveled: Long,

    @DefaultZeroDouble
    val heading: Double,

    @DefaultZeroFloat
    val speed: Float,

    @DefaultFalse
    val autopilotEnabled: Boolean,

    @DefaultTrue
    val collisionAvoidance: Boolean,

    @DefaultEmptyList
    val waypoints: MutableList<String> = mutableListOf(),

    @DefaultEmptyMap
    val sectorData: MutableMap<String, String> = mutableMapOf()
)
