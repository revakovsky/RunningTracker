package com.revakovskyi.core.domain.location

import kotlin.time.Duration

data class LocationTimeStamp(
    val locationWithAltitude: LocationWithAltitude,
    val durationTimeStamp: Duration,
)
