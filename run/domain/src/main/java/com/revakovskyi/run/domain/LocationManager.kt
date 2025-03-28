package com.revakovskyi.run.domain

import com.revakovskyi.core.connectivity.domain.messaging.MessagingAction
import com.revakovskyi.core.domain.location.LocationTimeStamp
import com.revakovskyi.core.domain.location.LocationWithAltitude
import com.revakovskyi.core.domain.stopwatch.Stopwatch
import com.revakovskyi.run.domain.wear.ConnectorToWatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.zip
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds

/**
 * Manages location tracking, heart rate monitoring, and run data aggregation during an active run.
 *
 * This class is responsible for:
 * - Observing user location at periodic intervals.
 * - Tracking elapsed time during the run.
 * - Collecting heart rate updates from a connected device (e.g., wearable).
 * - Calculating run statistics like distance and pace in real-time.
 * - Sending updates (e.g., elapsed time, distance) to a connected wearable device.
 *
 * @param locationObserver Observes the user's location at defined intervals.
 * @param applicationScope Coroutine scope for managing asynchronous operations.
 * @param connectorToWatch Manages communication with a connected wearable device.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocationManager(
    private val locationObserver: LocationObserver,
    private val applicationScope: CoroutineScope,
    private val connectorToWatch: ConnectorToWatch,
) {

    /***
     * Holds aggregated run data, such as distance, pace, and location history.
     */
    private val _runData = MutableStateFlow(RunData())
    val runData = _runData.asStateFlow()

    private val _elapsedTime = MutableStateFlow(Duration.ZERO)
    val elapsedTime = _elapsedTime.asStateFlow()

    private val isObservingLocation = MutableStateFlow(false)

    private val _isTracking = MutableStateFlow(false)
    val isTracking = _isTracking.asStateFlow()

    /***
     * Observes the user's current location or emits null if location updates are disabled.
     */
    val currentLocation: StateFlow<LocationWithAltitude?> = isObservingLocation
        .flatMapLatest { isObservingLocation ->
            if (isObservingLocation) locationObserver.observeLocation(1_000L)
            else flowOf()
        }
        .stateIn(
            applicationScope,
            SharingStarted.Lazily,
            null
        )

    /***
     * Tracks the user's heart rate updates from the wearable device.
     */
    private val heartRates = isTracking
        .flatMapLatest { isTracking ->
            if (isTracking) connectorToWatch.messagingActions
            else flowOf()
        }
        .filterIsInstance<MessagingAction.HeartRateUpdate>()
        .map { it.heartRate }
        .runningFold(initial = emptyList<Int>()) { currentHeartRates, newHeartRate ->
            currentHeartRates + newHeartRate
        }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )


    init {
        observeElapsedTimeUpdates()
        observeLocationUpdates()
        sendToWatchElapsedTimeUpdates()
        sendToWatchDistanceUpdates()
    }

    fun setIsTracking(isTracking: Boolean) {
        this._isTracking.value = isTracking
    }

    fun startObservingLocation() {
        isObservingLocation.value = true
        connectorToWatch.setIsTrackable(isTrackable = true)
    }

    fun stopObservingLocation() {
        isObservingLocation.value = false
        connectorToWatch.setIsTrackable(isTrackable = false)
    }

    fun finishRun() {
        stopObservingLocation()
        setIsTracking(isTracking = false)
        _elapsedTime.value = ZERO
        _runData.value = RunData()
    }

    private fun observeElapsedTimeUpdates() {
        _isTracking
            .onEach { isTracking ->
                if (!isTracking) {
                    val newList = buildList {
                        addAll(runData.value.locations)
                        add(emptyList<LocationTimeStamp>())
                    }.toList()
                    _runData.update {
                        it.copy(locations = newList)
                    }
                }
            }
            .flatMapLatest { isTracking ->
                if (isTracking) Stopwatch.timeAndEmit() else flowOf()
            }
            .onEach { duration -> _elapsedTime.value += duration }
            .launchIn(applicationScope)
    }

    /**
     * Observes location updates and processes them into run statistics.
     *
     * - Combines location updates with elapsed time to create `LocationTimeStamp` instances.
     * - Updates run data with new locations, heart rate data, and calculated statistics.
     */
    private fun observeLocationUpdates() {
        currentLocation
            .filterNotNull()
            .combineTransform(_isTracking) { locationWithAltitude, isTracking ->
                if (isTracking) emit(locationWithAltitude)
            }
            .zip(_elapsedTime) { locationWithAltitude, duration ->
                LocationTimeStamp(
                    locationWithAltitude = locationWithAltitude,
                    durationTimeStamp = duration
                )
            }
            .combine(heartRates) { locationTimeStamp, heartRates ->
                updateRunData(locationTimeStamp, heartRates)
            }
            .launchIn(applicationScope)
    }

    /**
     * Updates run data with the latest location, heart rates, and recalculated statistics.
     *
     * @param locationTimeStamp The latest location timestamp.
     * @param heartRates The list of heart rate readings.
     */
    private fun updateRunData(locationTimeStamp: LocationTimeStamp, heartRates: List<Int>) {
        _runData.update {
            val newLocationsList = getLocationsWithNewLocation(locationTimeStamp)
            val distanceMeters = LocationDataCalculator.getTotalDistanceMeters(newLocationsList)
            val distanceKm = distanceMeters / 1000.0
            val currentDuration = locationTimeStamp.durationTimeStamp
            val avgSecondPerKm = if (distanceKm == 0.0) 0
            else (currentDuration.inWholeSeconds / distanceKm).roundToInt()

            RunData(
                distanceMeters = distanceMeters,
                pace = avgSecondPerKm.seconds,
                locations = newLocationsList,
                heartRates = heartRates,
            )
        }
    }

    private fun getLocationsWithNewLocation(locationTimeStamp: LocationTimeStamp): List<List<LocationTimeStamp>> {
        val currentLocations = _runData.value.locations

        val lastLocationsList = if (currentLocations.isNotEmpty()) {
            currentLocations.last() + locationTimeStamp
        } else listOf(locationTimeStamp)

        return currentLocations.replaceLast(lastLocationsList.distinct())
    }

    private fun sendToWatchElapsedTimeUpdates() {
        elapsedTime
            .onEach { connectorToWatch.sendActionToWatch(MessagingAction.TimeUpdate(it)) }
            .launchIn(applicationScope)
    }

    private fun sendToWatchDistanceUpdates() {
        runData
            .map { it.distanceMeters }
            .distinctUntilChanged()
            .onEach { connectorToWatch.sendActionToWatch(MessagingAction.DistanceUpdate(it)) }
            .launchIn(applicationScope)
    }

}


private fun <T> List<List<T>>.replaceLast(replacement: List<T>): List<List<T>> {
    return if (this.isEmpty()) listOf(replacement)
    else this.dropLast(1) + listOf(replacement)
}