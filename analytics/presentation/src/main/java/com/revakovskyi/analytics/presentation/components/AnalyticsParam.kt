package com.revakovskyi.analytics.presentation.components

import androidx.annotation.StringRes

data class AnalyticsParam(
    @StringRes val paramNameResId: Int,
    val paramValue: String,
)
