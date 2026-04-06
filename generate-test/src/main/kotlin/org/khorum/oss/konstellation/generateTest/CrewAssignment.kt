package org.khorum.oss.konstellation.generateTest

import org.khorum.oss.konstellation.metaDsl.annotation.GeneratedDsl
import org.khorum.oss.konstellation.metaDsl.annotation.InjectDslMethod
import org.khorum.oss.konstellation.metaDsl.annotation.defaults.DefaultEnum

@GeneratedDsl
data class CrewAssignment(
    val name: String,
    @DefaultEnum("ENSIGN")
    val rank: Rank,
    @DefaultEnum(
        "BRIDGE",
        packageName = "org.khorum.oss.konstellation.generateTest",
        className = "CrewAssignment.Department"
    )
    val department: Department,
) {

    @Suppress("WRONG_ANNOTATION_TARGET")
    @InjectDslMethod
    fun designation(): String = "$name - $rank"

    enum class Rank { CAPTAIN, COMMANDER, LIEUTENANT, ENSIGN }
    enum class Department { BRIDGE, ENGINEERING, MEDICAL, SCIENCE }
}
