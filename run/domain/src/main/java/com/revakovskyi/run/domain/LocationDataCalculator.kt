package com.revakovskyi.run.domain

import com.revakovskyi.core.domain.location.LocationTimeStamp
import kotlin.math.roundToInt
import kotlin.time.DurationUnit

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

    fun getMaxSpeedKmh(locations: List<List<LocationTimeStamp>>): Double {
        return locations.maxOf { locationSet ->
            locationSet.zipWithNext { location1, location2 ->
                val distance = location1.locationWithAltitude.location.distanceTo(
                    other = location2.locationWithAltitude.location
                )
                val hoursDifference = (location2.durationTimeStamp - location1.durationTimeStamp)
                    .toDouble(DurationUnit.HOURS)

                if (hoursDifference == 0.0) 0.0
                else (distance / 1000) / hoursDifference
            }.maxOrNull() ?: 0.0
        }
    }

    fun getTotalElevationMeter(locations: List<List<LocationTimeStamp>>): Int {
        return locations.sumOf { locationSet ->
            locationSet.zipWithNext { location1, location2 ->
                val altitude1 = location1.locationWithAltitude.altitude
                val altitude2 = location2.locationWithAltitude.altitude
                (altitude2 - altitude1).coerceAtLeast(0.0)
            }.sum().roundToInt()
        }
    }

}