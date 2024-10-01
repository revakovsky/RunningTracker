package com.revakovskyi.analytics.domain

import kotlin.time.Duration

data class AnalyticsValues(
    val totalRunDistance: Int = 0,
    val totalRunTime: Duration = Duration.ZERO,
    val fastestEverRun: Double = 0.0,
    val avgDistancePerRun: Double = 0.0,
    val avgPacePerRun: Double = 0.0,
)
