package com.revakovskyi.run.presentation.runOverview

import com.revakovskyi.run.presentation.runOverview.models.RunUi

sealed interface RunOverviewAction {

    data object OnStartRunClick : RunOverviewAction
    data object OnLogOutClick : RunOverviewAction
    data object OnAnalyticsClick : RunOverviewAction
    data class DeleteRun(val run: RunUi) : RunOverviewAction

}
