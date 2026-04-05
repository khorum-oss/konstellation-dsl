package org.khorum.oss.konstellation.generateTest

import org.khorum.oss.konstellation.generateTest.nested.Version
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.DefaultValue
import org.khorum.oss.konstellation.metaDsl.annotation.DeprecatedDsl
import org.khorum.oss.konstellation.metaDsl.annotation.DslAlias
import org.khorum.oss.konstellation.metaDsl.annotation.DslDescription
import org.khorum.oss.konstellation.metaDsl.annotation.GeneratedDsl
import org.khorum.oss.konstellation.metaDsl.annotation.ListDsl
import org.khorum.oss.konstellation.metaDsl.annotation.MapDsl
import org.khorum.oss.konstellation.metaDsl.annotation.RootDsl
import org.khorum.oss.konstellation.metaDsl.annotation.TransientDsl
import org.khorum.oss.konstellation.metaDsl.annotation.ValidateDsl

@RootDsl
@GeneratedDsl(
    debug = true
)
data class StarShip(
    val name: String,
    val commanderNames: List<String>,
    val crewMap: Map<String, Passenger>,
    val description: String? = null,
    // @DslAlias: generates an additional accessor function with the alias name
    @DslAlias(names = ["active"])
    val activated: Boolean? = null,

    // @DeprecatedDsl: marks generated accessors as @Deprecated
    @DeprecatedDsl(message = "Use 'activated' instead", replaceWith = "activated")
    val docked: Boolean? = null,
    val capacity: Int? = null,
    val coordinates: SpaceTime? = null,
    val stardate: Stardate? = null,
    val notes: List<String>? = null,
    val passengers: List<Passenger>? = null,
    val areaCodes: Map<String, String>? = null,
    val roomMap: Map<String, Passenger>? = null,
    @DefaultValue("DEFAULT")
    val defaultString: String = "DEFAULT",
    @DefaultValue("Version.V1", packageName = "org.khorum.oss.konstellation.generateTest.nested", className = "Version")
    val version: Version = Version.V1,

    // -- Annotation metadata examples --

    // @DslDescription: adds KDoc to the generated builder property
    @DslDescription("Maximum warp speed the ship can achieve")
    val maxWarpSpeed: Float? = null,

    // @ValidateDsl: generates a require() check in the build() method
    @ValidateDsl(expression = "it?.let { v -> v > 0 } ?: true", message = "hullIntegrity must be positive")
    val hullIntegrity: Int? = null,

    // @TransientDsl: excluded from DSL builder generation entirely
    @TransientDsl(reason = "Internal tracking only")
    val internalTrackingId: String? = null,

    // @ListDsl/@MapDsl examples - demonstrating different accessor configurations

    // Default: both vararg and provider functions generated
    // Generates: aliases(vararg items: String) and aliases(provider: () -> List<String>)
    val aliases: List<String>? = null,

    // Only vararg function generated (no provider)
    // Generates: only tags(vararg items: String)
    @ListDsl(withProvider = false)
    val tags: List<String>? = null,

    // Only provider function generated (no vararg)
    // Generates: only metadata(provider: () -> Map<String, String>)
    @MapDsl(withVararg = false)
    val metadata: Map<String, String>? = null,

    // Neither function generated (direct property assignment only)
    // No accessor functions generated - must set directly: builder.systemCodes = listOf(...)
    @ListDsl(withVararg = false, withProvider = false)
    val systemCodes: List<Int>? = null
)
