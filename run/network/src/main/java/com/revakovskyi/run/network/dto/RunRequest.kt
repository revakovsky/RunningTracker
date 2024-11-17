package com.revakovskyi.run.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class RunRequest(
    val id: String,
    val lat: Double,
    val long: Double,
    val durationMillis: Long,
    val distanceMeters: Int,
    val epochMillis: Long,
    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val totalElevationMeters: Int,
    val avgHeartRate: Int?,
    val maxHeartRate: Int?,
)
