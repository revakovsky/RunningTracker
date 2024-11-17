package com.revakovskyi.core.database.mappers

import com.revakovskyi.core.database.entity.RunEntity
import com.revakovskyi.core.domain.location.Location
import com.revakovskyi.core.domain.run.Run
import org.bson.types.ObjectId
import java.time.Instant
import java.time.ZoneId
import kotlin.time.Duration.Companion.milliseconds

fun RunEntity.toRun(): Run {
    return Run(
        id = id,
        duration = durationMillis.milliseconds,
        dateTimeUtc = Instant.parse(dateTimeUtc).atZone(ZoneId.of("UTC")),
        distanceInMeters = distanceMeters,
        location = Location(latitude, longitude),
        maxSpeedKmH = maxSpeedKmh,
        totalElevationMeter = totalElevationMeters,
        mapPictureUrl = mapPictureUrl,
        avgHeartRate = avgHeartRate,
        maxHeartRate = maxHeartRate,
    )
}


fun Run.toEntity(): RunEntity {
    return RunEntity(
        id = id ?: ObjectId().toHexString(),
        latitude = location.latitude,
        longitude = location.longitude,
        durationMillis = duration.inWholeMilliseconds,
        distanceMeters = distanceInMeters,
        dateTimeUtc = dateTimeUtc.toInstant().toString(),
        avgSpeedKmh = avgSpeedKmH,
        maxSpeedKmh = maxSpeedKmH,
        totalElevationMeters = totalElevationMeter,
        mapPictureUrl = mapPictureUrl,
        avgHeartRate = avgHeartRate,
        maxHeartRate = maxHeartRate,
    )
}
