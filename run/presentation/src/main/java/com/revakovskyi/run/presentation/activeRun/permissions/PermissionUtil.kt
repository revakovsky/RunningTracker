package com.revakovskyi.run.presentation.activeRun.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat

/**
 * Extension function to check if the app should show the rationale for requesting location permission.
 *
 * @return `true` if the rationale should be shown, `false` otherwise.
 */
fun ComponentActivity.shouldShowLocationPermissionRationale(): Boolean {
    return shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
}


/**
 * Extension function to check if the app should show the rationale for requesting notification permission.
 *
 * Note: This is applicable only for Android 13 (API level 33) and above.
 *
 * @return `true` if the rationale should be shown, `false` otherwise.
 */
fun ComponentActivity.shouldShowNotificationPermissionRationale(): Boolean {
    return Build.VERSION.SDK_INT >= 33 &&
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
}


/**
 * Private helper function to determine if a specific permission is granted.
 *
 * @param permission The name of the permission to check (e.g., `Manifest.permission.ACCESS_FINE_LOCATION`).
 * @return `true` if the permission is granted, `false` otherwise.
 */
private fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}


/**
 * Checks if the app has been granted location permission.
 *
 * @return `true` if the location permission is granted, `false` otherwise.
 */
fun Context.hasLocationPermission(): Boolean {
    return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
}


/**
 * Checks if the app has been granted notification permission.
 *
 * Note: For Android versions below 13, notification permission is considered granted by default.
 *
 * @return `true` if the notification permission is granted, `false` otherwise.
 */
fun Context.hasNotificationPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= 33) hasPermission(Manifest.permission.POST_NOTIFICATIONS)
    else true
}
