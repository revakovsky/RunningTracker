package com.revakovskyi.run.domain

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import com.revakovskyi.core.connectivity.domain.messaging.MessagingAction
import com.revakovskyi.core.domain.location.Location
import com.revakovskyi.core.domain.location.LocationWithAltitude
import com.revakovskyi.core.test.LocationObserverFake
import com.revakovskyi.core.test.MainCoroutineExtension
import com.revakovskyi.core.test.PhoneToWatchConnectorFake
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.math.roundToInt

class LocationManagerTest {

    companion object {
        @JvmField
        @RegisterExtension
        val mainCoroutineExtension = MainCoroutineExtension()
    }

    private lateinit var locationManager: LocationManager
    private lateinit var locationObserverFake: LocationObserverFake
    private lateinit var watchConnectorFake: PhoneToWatchConnectorFake

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var testScope: CoroutineScope

    @BeforeEach
    fun setUp() {
        locationObserverFake = LocationObserverFake()
        watchConnectorFake = PhoneToWatchConnectorFake()

        testDispatcher = mainCoroutineExtension.testDispatcher
        testScope = CoroutineScope(testDispatcher)

        locationManager = LocationManager(
            locationObserver = locationObserverFake,
            applicationScope = testScope,
            connectorToWatch = watchConnectorFake
        )
    }

    @Test
    fun testCombiningRunData() = runTest {
        locationManager.runData.test {
            skipItems(1)

            locationManager.startObservingLocation()
            locationManager.setIsTracking(true)

            val location1 = LocationWithAltitude(Location(1.0, 1.0), 1.0)
            locationObserverFake.trackLocation(location1)
            val emission1 = awaitItem()

            val location2 = LocationWithAltitude(Location(2.0, 2.0), 2.0)
            locationObserverFake.trackLocation(location2)
            val emission2 = awaitItem()

            watchConnectorFake.sendFromWatchToPhone(MessagingAction.HeartRateUpdate(123))
            val emission3 = awaitItem()

            watchConnectorFake.sendFromWatchToPhone(MessagingAction.HeartRateUpdate(124))
            val emission4 = awaitItem()

            testScope.cancel()

            assertThat(emission1.locations[0][0].locationWithAltitude).isEqualTo(location1)
            assertThat(emission2.locations[0][1].locationWithAltitude).isEqualTo(location2)
            assertThat(emission3.heartRates).isEqualTo(listOf(123))
            assertThat(emission4.heartRates).isEqualTo(listOf(123, 124))

            // The value 156.9 gotten by the calculating online in converter the distance between location1 and location2
            val expectedDistanceMeters = 156.9 * 1000L
            val tolerance = 0.03
            val lowerBound = (expectedDistanceMeters * (1 - tolerance)).roundToInt()
            val upperBound = (expectedDistanceMeters * (1 + tolerance)).roundToInt()
            assertThat(emission4.distanceMeters).isBetween(lowerBound, upperBound)

            assertThat(emission4.locations.first()).hasSize(2)
        }
    }

}