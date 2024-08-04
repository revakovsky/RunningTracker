package com.revakovskyi.run.domain

import com.revakovskyi.core.domain.location.LocationTimeStamp
import kotlin.math.roundToInt

object LocationDataCalculator {

    fun getTotalDistanceMeters(locations: List<List<LocationTimeStamp>>): Int {
        return locations
            .sumOf { locationPerLine ->
                locationPerLine.zipWithNext { location1, location2 ->
                    location1.locationWithAltitude.location.distanceTo(
                        location2.locationWithAltitude.location
                    )
                }
                    .sum()
                    .roundToInt()
            }
    }

}