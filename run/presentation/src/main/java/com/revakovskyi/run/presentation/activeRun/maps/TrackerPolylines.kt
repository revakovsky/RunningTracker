package com.revakovskyi.run.presentation.activeRun.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Polyline
import com.revakovskyi.core.domain.location.LocationTimeStamp

@Composable
fun TrackerPolylines(
    locations: List<List<LocationTimeStamp>>,
) {
    val polylines = remember(key1 = locations) {
        locations.map {
            it.zipWithNext { stamp1, stamp2 ->
                PolylineUi(
                    location1 = stamp1.locationWithAltitude.location,
                    location2 = stamp2.locationWithAltitude.location,
                    color = PolylineColorCalculator.locationsToColor(stamp1, stamp2)
                )
            }
        }
    }

    polylines.forEach { polyline ->
        polyline.forEach { polylineUi ->
            Polyline(
                points = listOf(
                    LatLng(polylineUi.location1.latitude, polylineUi.location1.longitude),
                    LatLng(polylineUi.location2.latitude, polylineUi.location2.longitude),
                ),
                color = polylineUi.color,
                jointType = JointType.BEVEL
            )
        }
    }

}