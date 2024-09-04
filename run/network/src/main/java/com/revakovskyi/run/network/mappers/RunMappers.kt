package com.revakovskyi.run.network.mappers

import com.revakovskyi.core.domain.location.Location
import com.revakovskyi.core.domain.run.Run
import com.revakovskyi.run.network.dto.RunDto
import com.revakovskyi.run.network.dto.RunRequest
import java.time.Instant
import java.time.ZoneId
import kotlin.time.Duration.Companion.milliseconds

fun RunDto.toRun(): Run {
    return Run(
        id = id,
        duration = durationMillis.milliseconds,
        dateTimeUtc = Instant.parse(dateTimeUtc).atZone(ZoneId.of("UTC")),
        distanceInMeters = distanceMeters,
        location = Location(lat, long),
        maxSpeedKmH = maxSpeedKmh,
        totalElevationMeter = totalElevationMeters,
        mapPictureUrl = mapPictureUrl,
    )
}

fun Run.toRunRequest(): RunRequest {
    return RunRequest(
        id = id!!,
        lat = location.latitude,
        long = location.longitude,
        durationMillis = duration.inWholeMilliseconds,
        distanceMeters = distanceInMeters,
        epochMillis = dateTimeUtc.toEpochSecond() * 1_000L,
        avgSpeedKmh = avgSpeedKmH,
        maxSpeedKmh = maxSpeedKmH,
        totalElevationMeters = totalElevationMeter,
    )
}
