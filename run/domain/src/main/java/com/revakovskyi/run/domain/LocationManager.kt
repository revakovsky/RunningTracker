package com.revakovskyi.run.domain

import com.revakovskyi.core.domain.location.LocationTimeStamp
import com.revakovskyi.core.domain.location.LocationWithAltitude
import com.revakovskyi.core.domain.stopwatch.Stopwatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.zip
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class LocationManager(
    private val locationObserver: LocationObserver,
    private val applicationScope: CoroutineScope,
) {

    private val _runData = MutableStateFlow(RunData())
    val runData = _runData.asStateFlow()

    private val _elapsedTime = MutableStateFlow(Duration.ZERO)
    val elapsedTime = _elapsedTime.asStateFlow()

    private val isObservingLocation = MutableStateFlow(false)

    private val _isTracking = MutableStateFlow(false)
    val isTracking = _isTracking.asStateFlow()

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


    init {
        observeElapsedTimeUpdates()
        observeLocationUpdates()
    }

    fun setIsTracking(isTracking: Boolean) {
        this._isTracking.value = isTracking
    }

    fun startObservingLocation() {
        isObservingLocation.value = true
    }

    fun stopObservingLocation() {
        isObservingLocation.value = false
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
            .onEach { locationTimeStamp -> updateRunData(locationTimeStamp) }
            .launchIn(applicationScope)
    }

    private fun updateRunData(locationTimeStamp: LocationTimeStamp) {
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
            )
        }
    }

    private fun getLocationsWithNewLocation(locationTimeStamp: LocationTimeStamp): List<List<LocationTimeStamp>> {
        val currentLocations = _runData.value.locations

        val lastLocationsList = if (currentLocations.isNotEmpty()) {
            currentLocations.last() + locationTimeStamp
        } else listOf(locationTimeStamp)

        return currentLocations.replaceLast(lastLocationsList)
    }

    fun finishRun() {
        stopObservingLocation()
        setIsTracking(isTracking = false)
        _elapsedTime.value = ZERO
        _runData.value = RunData()
    }

}


private fun <T> List<List<T>>.replaceLast(replacement: List<T>): List<List<T>> {
    return if (this.isEmpty()) listOf(replacement)
    else this.dropLast(1) + listOf(replacement)
}