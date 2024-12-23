package com.revakovskyi.core.domain.location

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Represents a geographic location defined by its latitude and longitude.
 *
 * @property latitude The latitude of the location in degrees.
 * @property longitude The longitude of the location in degrees.
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
) {

    /**
     * Calculates the great-circle distance (in meters) between this location and another location
     * using the Haversine formula. This formula accounts for the curvature of the Earth to provide
     * an accurate distance between two points on the globe.
     *
     * @param other The other [Location] to calculate the distance to.
     * @return The distance between the two locations in meters as a [Float].
     */
    fun distanceTo(other: Location): Float {
        val latitudeDistance = Math.toRadians(other.latitude - latitude)
        val longitudeDistance = Math.toRadians(other.longitude - longitude)

        val a = sin(latitudeDistance / 2) * sin(latitudeDistance / 2) +
                cos(Math.toRadians(latitude)) * cos(Math.toRadians(other.latitude)) *
                sin(longitudeDistance / 2) * sin(longitudeDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_METERS * c.toFloat()
    }


    companion object {
        private const val EARTH_RADIUS_METERS = 6_371_000
    }

}
