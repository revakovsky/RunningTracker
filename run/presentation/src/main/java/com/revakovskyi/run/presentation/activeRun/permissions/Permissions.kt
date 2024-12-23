package com.revakovskyi.run.presentation.activeRun.permissions

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.revakovskyi.run.presentation.activeRun.ActiveRunAction

/**
 * Processes the result of a permission launcher and triggers the corresponding actions.
 *
 * @param result A map of permissions and their grant statuses.
 * @param context The current context, expected to be a `ComponentActivity`.
 * @param onAction A callback to handle actions related to location and notification permissions.
 */
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


/**
 * Requests the required permissions for the tracker functionality using an `ActivityResultLauncher`.
 *
 * @param context The current context, used to check existing permissions.
 */
internal fun ActivityResultLauncher<Array<String>>.requestTrackerPermissions(context: Context) {
    val hasLocationPermission = context.hasLocationPermission()
    val hasNotificationPermission = context.hasNotificationPermission()

    when {
        !hasLocationPermission && !hasNotificationPermission -> launch(getLocationPermissionsArray() + getNotificationPermissionsArray())
        !hasLocationPermission -> launch(getLocationPermissionsArray())
        !hasNotificationPermission -> launch(getNotificationPermissionsArray())
    }
}


/**
 * Provides an array of location permissions required by the tracker.
 *
 * @return An array containing `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION` permissions.
 */
private fun getLocationPermissionsArray() = arrayOf(
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION,
)


/**
 * Provides an array of notification permissions required by the tracker.
 *
 * @return An array containing `POST_NOTIFICATIONS` permission for Android 13 (API level 33) and above,
 *         or an empty array for earlier versions.
 */
private fun getNotificationPermissionsArray() =
    if (Build.VERSION.SDK_INT >= 33) arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    else arrayOf()
