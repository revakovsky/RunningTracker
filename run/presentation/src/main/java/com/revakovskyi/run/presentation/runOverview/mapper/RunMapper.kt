package com.revakovskyi.run.presentation.runOverview.mapper

import com.revakovskyi.core.domain.run.Run
import com.revakovskyi.core.peresentation.ui.formatted
import com.revakovskyi.core.peresentation.ui.toFormattedHeartRate
import com.revakovskyi.core.peresentation.ui.toFormattedKm
import com.revakovskyi.core.peresentation.ui.toFormattedKmH
import com.revakovskyi.core.peresentation.ui.toFormattedMeters
import com.revakovskyi.core.peresentation.ui.toFormattedPace
import com.revakovskyi.run.presentation.runOverview.models.RunUi
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Run.toRunUi(): RunUi {
    val dateTimeInLocalZone = dateTimeUtc.withZoneSameInstant(ZoneId.systemDefault())
    val formattedDateTime = DateTimeFormatter
        .ofPattern("MMM dd, yyyy - hh:mma")
        .format(dateTimeInLocalZone)
    val distanceKm = distanceInMeters / 1000.0

    return RunUi(
        id = id!!,
        duration = duration.formatted(),
        dateTime = formattedDateTime,
        distance = distanceKm.toFormattedKm(),
        avgSpeed = avgSpeedKmH.toFormattedKmH(),
        maxSpeed = maxSpeedKmH.toFormattedKmH(),
        pace = duration.toFormattedPace(distanceKm),
        totalElevation = totalElevationMeter.toFormattedMeters(),
        mapPictureUrl = mapPictureUrl,
        avgHeartRate = avgHeartRate.toFormattedHeartRate(),
        maxHeartRate = maxHeartRate.toFormattedHeartRate(),
    )
}
