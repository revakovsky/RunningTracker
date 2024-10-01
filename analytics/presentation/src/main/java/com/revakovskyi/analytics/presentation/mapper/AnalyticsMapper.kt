package com.revakovskyi.analytics.presentation.mapper

import com.revakovskyi.analytics.domain.AnalyticsValues
import com.revakovskyi.analytics.presentation.models.AnalyticsDashboardValues
import com.revakovskyi.core.peresentation.ui.formatted
import com.revakovskyi.core.peresentation.ui.toFormattedKm
import com.revakovskyi.core.peresentation.ui.toFormattedKmH
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

fun Duration.toFormattedTotalTime(): String {
    val days = toLong(DurationUnit.DAYS)
    val hours = toLong(DurationUnit.HOURS) % 24
    val minutes = toLong(DurationUnit.MINUTES) % 60
    return "${days}d ${hours}h ${minutes}m"
}

fun AnalyticsValues.toAnalyticsDashboardValues(): AnalyticsDashboardValues =
    AnalyticsDashboardValues(
        totalRunDistance = (totalRunDistance / 1000.0).toFormattedKm(),
        totalRunTime = totalRunTime.toFormattedTotalTime(),
        fastestEverRun = fastestEverRun.toFormattedKmH(),
        avgDistancePerRun = (avgDistancePerRun / 1000.0).toFormattedKm(),
        avgPacePerRun = avgPacePerRun.seconds.formatted()
    )