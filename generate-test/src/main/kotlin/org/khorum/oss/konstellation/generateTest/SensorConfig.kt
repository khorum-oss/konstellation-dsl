package org.khorum.oss.konstellation.generateTest

import org.khorum.oss.konstellation.metaDsl.annotation.DefaultState
import org.khorum.oss.konstellation.metaDsl.annotation.DefaultStateType
import org.khorum.oss.konstellation.metaDsl.annotation.GeneratedDsl

/**
 * Domain class demonstrating @DefaultState annotation usage for all DefaultStateType values.
 */
@GeneratedDsl
data class SensorConfig(
    @DefaultState(DefaultStateType.EMPTY_STRING)
    val sensorName: String,

    @DefaultState(DefaultStateType.ZERO_INT)
    val pollingIntervalMs: Int,

    @DefaultState(DefaultStateType.ZERO_LONG)
    val lastReadingTimestamp: Long,

    @DefaultState(DefaultStateType.ZERO_DOUBLE)
    val sensitivity: Double,

    @DefaultState(DefaultStateType.ZERO_FLOAT)
    val threshold: Float,

    @DefaultState(DefaultStateType.FALSE)
    val enabled: Boolean,

    @DefaultState(DefaultStateType.TRUE)
    val autoCalibrate: Boolean,

    @DefaultState(DefaultStateType.EMPTY_LIST)
    val readings: MutableList<String>,

    @DefaultState(DefaultStateType.EMPTY_MAP)
    val metadata: MutableMap<String, String>
)
