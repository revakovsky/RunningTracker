package com.revakovskyi.run.presentation.activeRun

sealed interface ActiveRunAction {

    data object OnToggleRunClick : ActiveRunAction
    data object OnFinishRunClick : ActiveRunAction
    data object OnResumeRunClick : ActiveRunAction
    data object OnBackClick : ActiveRunAction
    data object OnDismissDialog : ActiveRunAction

    data class SubmitLocationPermission(
        val hasAcceptedLocationPermission: Boolean,
        val showLocationPermissionRationale: Boolean,
    ) : ActiveRunAction

    data class SubmitNotificationPermission(
        val hasAcceptedNotificationPermission: Boolean,
        val showNotificationPermissionRationale: Boolean,
    ) : ActiveRunAction

    data object DismissRationaleDialog : ActiveRunAction

}
