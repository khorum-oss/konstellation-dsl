package org.khorum.oss.konstellation.generateTest

import org.khorum.oss.konstellation.metaDsl.annotation.defaults.DefaultValue
import org.khorum.oss.konstellation.metaDsl.annotation.GeneratedDsl

/**
 * Demonstrates @GeneratedDsl(name=...) for custom builder naming.
 * The generated builder will be named "ShieldDslBuilder" instead of "ShieldConfigDslBuilder".
 */
@GeneratedDsl(name = "Shield")
data class ShieldConfig(
    val frequency: Double,
    @DefaultValue("100")
    val strength: Int = 100,
    val modulation: String? = null
)
