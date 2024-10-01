package com.revakovskyi.analytics.presentation

sealed interface AnalyticsAction {

    data object OnBackClick : AnalyticsAction

}
