package org.khorum.oss.konstellation.generateTest

import org.khorum.oss.konstellation.metaDsl.annotation.DefaultValue
import org.khorum.oss.konstellation.metaDsl.annotation.GeneratedDsl
import org.khorum.oss.konstellation.metaDsl.annotation.ListDsl
import org.khorum.oss.konstellation.metaDsl.annotation.MapDsl

/**
 * Demonstrates @ListDsl for list property configuration with constraints and transformations,
 * and @MapDsl for map property configuration with constraints.
 */
@GeneratedDsl
data class FleetCommand(
    val commandName: String,

    // @ListDsl: minSize, maxSize, uniqueElements, sorted
    @ListDsl(minSize = 1, maxSize = 10, uniqueElements = true, sorted = true)
    val shipNames: List<String>,

    // @ListDsl: withVararg only (no provider)
    @ListDsl(withProvider = false)
    val priorities: List<Int>? = null,

    // @ListDsl: withProvider only (no vararg)
    @ListDsl(withVararg = false)
    val objectives: List<String>? = null,

    // @MapDsl: minSize, maxSize
    @MapDsl(minSize = 1, maxSize = 5)
    val shipAssignments: Map<String, String>,

    // @MapDsl: withVararg only
    @MapDsl(withProvider = false)
    val sectorCodes: Map<String, Int>? = null,

    // @MapDsl: withProvider only
    @MapDsl(withVararg = false)
    val commandCodes: Map<String, String>? = null,

    @DefaultValue("1")
    val priority: Int = 1
)
