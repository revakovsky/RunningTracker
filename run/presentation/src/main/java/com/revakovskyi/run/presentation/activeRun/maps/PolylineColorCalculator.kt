package com.revakovskyi.run.presentation.activeRun.maps

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.revakovskyi.core.domain.location.LocationTimeStamp
import kotlin.math.abs

private const val MIN_RUN_SPEED = 5.0
private const val MAX_RUN_SPEED = 20.0

object PolylineColorCalculator {

    fun locationsToColor(location1: LocationTimeStamp, location2: LocationTimeStamp): Color {
        val distanceMeters = location1.locationWithAltitude.location.distanceTo(
            location2.locationWithAltitude.location
        )
        val timeDiff = abs((location2.durationTimeStamp - location1.durationTimeStamp).inWholeSeconds)
        val speedKmh = (distanceMeters / timeDiff) * 3.6

        return interpolateColor(
            speedKmh = speedKmh,
            colorStart = Color.Green,
            colorMid = Color.Yellow,
            colorEnd = Color.Red
        )
    }

    private fun interpolateColor(
        speedKmh: Double,
        colorStart: Color,
        colorMid: Color,
        colorEnd: Color,
    ): Color {
        val ratio = ((speedKmh - MIN_RUN_SPEED) / (MAX_RUN_SPEED - MIN_RUN_SPEED)).coerceIn(0.0..1.0)
        val colorInt = if (ratio <= 0.5) {
            val midRatio = ratio / 0.5
            ColorUtils.blendARGB(colorStart.toArgb(), colorMid.toArgb(), midRatio.toFloat())
        } else {
            val midToEndRatio = (ratio - 0.5) / 0.5
            ColorUtils.blendARGB(colorMid.toArgb(), colorEnd.toArgb(), midToEndRatio.toFloat())
        }
        return Color(colorInt)
    }

}