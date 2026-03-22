package org.khorum.oss.konstellation.generateTest

import org.khorum.oss.konstellation.metaDsl.annotation.GeneratedDsl
import org.khorum.oss.konstellation.metaDsl.annotation.PrivateDslProperty
import org.khorum.oss.konstellation.metaDsl.annotation.PublicDslProperty

@GeneratedDsl(
    withListGroup = true,
    withMapGroup = "SINGLE"
)
data class Passenger(
    val name: String,
    val rank: Rank,
    @PublicDslProperty(restrictSetter = true, wrapInFunction = true)
    val origin: String? = null,
    @PublicDslProperty(wrapInFunction = true)
    val destination: String? = null,
    @PrivateDslProperty(restrictSetter = false, wrapInFunction = false)
    private val aliases: List<String>? = null,
    @PrivateDslProperty(restrictSetter = false)
    private val age: Int? = null
) {

    enum class Rank {
        CAPTAIN,
        CREWMEMBER,
        COMMANDER,
        LIEUTENANT_COMMANDER,
        FIRST_OFFICER,
        SECOND_OFFICER,
        CIVILIAN
    }
}
