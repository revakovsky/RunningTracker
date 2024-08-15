package com.revakovskyi.run.presentation.activeRun.permissions

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.revakovskyi.run.presentation.activeRun.ActiveRunAction

internal fun processPermissionLauncherResult(
    result: Map<String, @JvmSuppressWildcards Boolean>,
    context: Context,
    onAction: (action: ActiveRunAction) -> Unit,
) {
    val hasAccessCourseLocation = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    val hasAccessFineLocation = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
    val hasAccessNotification = if (Build.VERSION.SDK_INT >= 33) {
        result[Manifest.permission.POST_NOTIFICATIONS] == true
    } else true

    val activity = context as ComponentActivity
    val showLocationRationale = activity.shouldShowLocationPermissionRationale()
    val showNotificationRationale = activity.shouldShowNotificationPermissionRationale()

    onAction(
        ActiveRunAction.SubmitLocationPermission(
            hasAcceptedLocationPermission = hasAccessCourseLocation && hasAccessFineLocation,
            showLocationPermissionRationale = showLocationRationale
        )
    )
    onAction(
        ActiveRunAction.SubmitNotificationPermission(
            hasAcceptedNotificationPermission = hasAccessNotification,
            showNotificationPermissionRationale = showNotificationRationale
        )
    )
}

internal fun ActivityResultLauncher<Array<String>>.requestTrackerPermissions(context: Context) {
    val hasLocationPermission = context.hasLocationPermission()
    val hasNotificationPermission = context.hasNotificationPermission()

    when {
        !hasLocationPermission && !hasNotificationPermission -> launch(getLocationPermissionsArray() + getNotificationPermissionsArray())
        !hasLocationPermission -> launch(getLocationPermissionsArray())
        !hasNotificationPermission -> launch(getNotificationPermissionsArray())
    }
}

private fun getLocationPermissionsArray() = arrayOf(
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION,
)

private fun getNotificationPermissionsArray() =
    if (Build.VERSION.SDK_INT >= 33) arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    else arrayOf()
