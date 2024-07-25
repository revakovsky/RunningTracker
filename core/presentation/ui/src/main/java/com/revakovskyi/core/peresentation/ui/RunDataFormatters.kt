package com.revakovskyi.core.peresentation.ui

import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.time.Duration

fun Duration.formatted(): String {
    val totalSeconds = inWholeSeconds
    val hours = String.format("%02d", totalSeconds / 3600)
    val minutes = String.format("%02d", (totalSeconds % 3600) / 60)
    val seconds = String.format("%02d", (totalSeconds % 60))

    return "$hours:$minutes:$seconds"
}


fun Double.toFormattedKm(): String {
    return "${this.roundToDecimal(2)} km"
}

private fun Double.roundToDecimal(decimalCount: Int): Double {
    val factor = 10f.pow(decimalCount)
    return round(this * factor) / factor
}


fun Duration.toFormattedPace(distanceKm: Double): String {
    return if (this == Duration.ZERO || distanceKm <= 0.0) "—"
    else {
        val secondsPerKm = (this.inWholeSeconds / distanceKm).roundToInt()
        val averagePacePerMinute = secondsPerKm / 60
        val averagePavePerSeconds = String.format("%02d", secondsPerKm % 60)
        "$averagePacePerMinute:$averagePavePerSeconds / km"
    }
}
