package com.revakovskyi.run.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class RunDto(
    val id: String,
    val lat: Double,
    val long: Double,
    val durationMillis: Long,
    val distanceMeters: Int,
    val dateTimeUtc: String,
    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val totalElevationMeters: Int,
    val mapPictureUrl: String?,
    val avgHeartRate: Int?,
    val maxHeartRate: Int?,
)
