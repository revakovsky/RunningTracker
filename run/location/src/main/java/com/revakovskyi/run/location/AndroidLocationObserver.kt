package com.revakovskyi.run.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.revakovskyi.core.domain.location.LocationWithAltitude
import com.revakovskyi.run.domain.LocationObserver
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@SuppressLint("MissingPermission")
class AndroidLocationObserver(
    private val context: Context,
) : LocationObserver {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Observes the user's location at regular intervals.
     *
     * @param interval The time interval (in milliseconds) for location updates.
     * @return A `Flow` emitting `LocationWithAltitude` objects representing the user's location.
     */
    override fun observeLocation(interval: Long): Flow<LocationWithAltitude> {
        return callbackFlow {
            checkGpsAndNetworkEnabled()

            if (locationPermissionsNotGranted()) close()
            else {
                fetchTheLastKnownLocation()
                val locationCallback = defineLocationCallback()
                requestLocationUpdates(interval, locationCallback)

                awaitClose { client.removeLocationUpdates(locationCallback) }
            }
        }
    }

    /**
     * Checks if GPS and Network providers are enabled and waits until they are.
     */
    private suspend fun checkGpsAndNetworkEnabled() {
        val locationManager = context.getSystemService<LocationManager>()!!
        var isGpsEnabled = false
        var isNetworkEnabled = false

        while (!isGpsEnabled && !isNetworkEnabled) {
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isGpsEnabled && !isNetworkEnabled) delay(3000)
        }
    }

    /**
     * Checks if the required location permissions are granted.
     *
     * @return `true` if permissions are not granted, `false` otherwise.
     */
    private fun locationPermissionsNotGranted(): Boolean =
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED

    private fun ProducerScope<LocationWithAltitude>.fetchTheLastKnownLocation() {
        client.lastLocation.addOnSuccessListener { location ->
            location?.let { trySend(it.toLocationWithAltitude()) }
        }
    }

    private fun ProducerScope<LocationWithAltitude>.defineLocationCallback(): LocationCallback =
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)

                result.locations.lastOrNull()?.let { location ->
                    trySend(location.toLocationWithAltitude())
                }
            }
        }

    /**
     * Requests location updates from the `FusedLocationProviderClient`.
     *
     * @param interval The interval (in milliseconds) for location updates.
     * @param locationCallback The callback to handle location updates.
     */
    private fun requestLocationUpdates(
        interval: Long,
        locationCallback: LocationCallback,
    ) {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval).build()
        client.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

}