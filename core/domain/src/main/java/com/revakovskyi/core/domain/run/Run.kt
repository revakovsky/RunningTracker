package com.revakovskyi.core.domain.run

import com.revakovskyi.core.domain.location.Location
import java.time.ZonedDateTime
import kotlin.time.Duration
import kotlin.time.DurationUnit

data class Run(
    val id: String?,         // if a new run - id is null
    val duration: Duration,
    val dateTimeUtc: ZonedDateTime,
    val distanceInMeters: Int,
    val location: Location,
    val maxSpeedKmH: Double,
    val totalElevationMeter: Int,
    val mapPictureUrl: String?,
) {
    val avgSpeedKmH: Double
        get() = (distanceInMeters / 1000.0) / duration.toDouble(DurationUnit.HOURS)
}
