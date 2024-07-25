package com.revakovskyi.run.presentation.activeRun

sealed interface ActiveRunAction {

    data object OnToggleRunClick : ActiveRunAction
    data object OnFinishRunClick : ActiveRunAction
    data object OnResumeRunClick : ActiveRunAction
    data object OnBackClick : ActiveRunAction
    data object OnDismissDialog : ActiveRunAction

}
