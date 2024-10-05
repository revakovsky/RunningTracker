package com.revakovskyi.run.presentation.activeRun

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revakovskyi.core.domain.location.Location
import com.revakovskyi.core.domain.run.Run
import com.revakovskyi.core.domain.run.RunRepository
import com.revakovskyi.core.domain.util.Result
import com.revakovskyi.core.peresentation.ui.UiText
import com.revakovskyi.core.peresentation.ui.asUiText
import com.revakovskyi.run.domain.LocationDataCalculator
import com.revakovskyi.run.domain.LocationManager
import com.revakovskyi.run.presentation.R
import com.revakovskyi.run.presentation.activeRun.service.ActiveRunService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime

class ActiveRunViewModel(
    private val locationManager: LocationManager,
    private val runRepository: RunRepository,
) : ViewModel() {

    var state by mutableStateOf(
        ActiveRunState(
            shouldTrack = ActiveRunService.isServiceActive && locationManager.isTracking.value,
            hasStartedRunning = ActiveRunService.isServiceActive
        )
    )
        private set

    private var eventChannel = Channel<ActiveRunEvent>()
    val events = eventChannel.receiveAsFlow()

    private val hasLocationPermission = MutableStateFlow(false)

    private val shouldTrack = snapshotFlow { state.shouldTrack }
        .stateIn(viewModelScope, SharingStarted.Lazily, state.shouldTrack)

    private val isTracking = combine(
        shouldTrack,
        hasLocationPermission
    ) { shouldTrack, hasLocationPermission -> shouldTrack && hasLocationPermission }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)


    init {
        observeLocationPermission()
        observeTrackingStatus()
        observeLocationUpdates()
        observeRunDataUpdates()
        observeElapsedTimeUpdates()
    }

    private fun observeLocationPermission() {
        hasLocationPermission
            .onEach { hasPermission ->
                if (hasPermission) locationManager.startObservingLocation()
                else locationManager.stopObservingLocation()
            }
            .launchIn(viewModelScope)
    }

    private fun observeTrackingStatus() {
        isTracking
            .onEach { isTracking -> locationManager.setIsTracking(isTracking) }
            .launchIn(viewModelScope)
    }

    private fun observeLocationUpdates() {
        locationManager
            .currentLocation
            .onEach { locationWithAltitude ->
                state = state.copy(currentLocation = locationWithAltitude?.location)
            }
            .launchIn(viewModelScope)
    }

    private fun observeRunDataUpdates() {
        locationManager
            .runData
            .onEach { runData -> state = state.copy(runData = runData) }
            .launchIn(viewModelScope)
    }

    private fun observeElapsedTimeUpdates() {
        locationManager
            .elapsedTime
            .onEach { duration -> state = state.copy(elapsedTime = duration) }
            .launchIn(viewModelScope)
    }


    fun onAction(action: ActiveRunAction) {
        when (action) {
            ActiveRunAction.OnDismissDialog -> Unit
            ActiveRunAction.OnFinishRunClick -> finishCurrentRun()
            ActiveRunAction.OnResumeRunClick -> resumeRun()
            ActiveRunAction.OnToggleRunClick -> toggleRun()
            is ActiveRunAction.SubmitLocationPermission -> submitLocationPermission(action)
            is ActiveRunAction.SubmitNotificationPermission -> submitNotificationPermission(action)
            ActiveRunAction.DismissRationaleDialog -> dismissRationaleDialog()
            ActiveRunAction.OnBackClick -> stopTracking()
            is ActiveRunAction.OnSaveCurrentRun -> saveCurrentRun(action.mapPictureBytes)
        }
    }

    private fun finishCurrentRun() {
        state = state.copy(isRunFinished = true, isSavingRun = true)
    }

    private fun resumeRun() {
        state = state.copy(shouldTrack = true)
    }

    private fun toggleRun() {
        if (state.currentLocation != null) {
            state = state.copy(
                hasStartedRunning = true,
                shouldTrack = !state.shouldTrack
            )
        } else {
            viewModelScope.launch {
                eventChannel.send(
                    ActiveRunEvent.Error(
                        UiText.StringResource(R.string.turn_on_location)
                    )
                )
            }
        }
    }

    private fun submitLocationPermission(action: ActiveRunAction.SubmitLocationPermission) {
        hasLocationPermission.value = action.hasAcceptedLocationPermission
        state = state.copy(
            showLocationRationale = action.showLocationPermissionRationale
        )
    }

    private fun submitNotificationPermission(action: ActiveRunAction.SubmitNotificationPermission) {
        state = state.copy(
            showNotificationRationale = action.showNotificationPermissionRationale
        )
    }

    private fun dismissRationaleDialog() {
        state = state.copy(
            showLocationRationale = false,
            showNotificationRationale = false,
        )
    }

    private fun stopTracking() {
        state = state.copy(shouldTrack = false)
    }

    private fun saveCurrentRun(mapPictureBytes: ByteArray) {
        if (isInvalidRun()) {
            state = state.copy(isSavingRun = false)
            return
        }

        viewModelScope.launch {
            val run = finishCreatingRun()
            locationManager.finishRun()
            handleRunSaving(run, mapPictureBytes)
            state = state.copy(isSavingRun = false)
        }
    }

    private fun isInvalidRun(): Boolean {
        val locations = state.runData.locations
        return locations.isEmpty() || locations.first().size <= 1
    }

    private fun finishCreatingRun(): Run {
        val locations = state.runData.locations
        return Run(
            id = null,
            duration = state.elapsedTime,
            dateTimeUtc = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")),
            distanceInMeters = state.runData.distanceMeters,
            location = state.currentLocation ?: Location(0.0, 0.0),
            maxSpeedKmH = LocationDataCalculator.getMaxSpeedKmh(locations),
            totalElevationMeter = LocationDataCalculator.getTotalElevationMeter(locations),
            mapPictureUrl = null
        )
    }

    private suspend fun handleRunSaving(run: Run, mapPictureBytes: ByteArray) {
        when (val result = runRepository.upsertRun(run, mapPictureBytes)) {
            is Result.Error -> eventChannel.send(ActiveRunEvent.Error(result.error.asUiText()))
            is Result.Success -> eventChannel.send(ActiveRunEvent.RunSaved)
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (!ActiveRunService.isServiceActive) locationManager.stopObservingLocation()
    }

}