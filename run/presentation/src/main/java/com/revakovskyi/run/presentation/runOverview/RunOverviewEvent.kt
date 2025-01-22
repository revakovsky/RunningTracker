package com.revakovskyi.run.presentation.runOverview

sealed interface RunOverviewEvent {

    data object OnAnalyticsClick : RunOverviewEvent
    data object OnStartRunClick : RunOverviewEvent
    data object OnLogOutClick : RunOverviewEvent

}
