package com.revakovskyi.run.presentation.activeRun

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revakovskyi.run.domain.LocationManager
import com.revakovskyi.run.presentation.activeRun.service.ActiveRunService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

class ActiveRunViewModel(
    private val locationManager: LocationManager,
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
            ActiveRunAction.OnDismissDialog -> Unit /*TODO*/
            ActiveRunAction.OnFinishRunClick -> Unit /*TODO*/
            ActiveRunAction.OnResumeRunClick -> resumeRun()
            ActiveRunAction.OnToggleRunClick -> toggleRun()
            is ActiveRunAction.SubmitLocationPermission -> submitLocationPermission(action)
            is ActiveRunAction.SubmitNotificationPermission -> submitNotificationPermission(action)
            ActiveRunAction.DismissRationaleDialog -> dismissRationaleDialog()
            ActiveRunAction.OnBackClick -> stopTracking()
        }
    }

    private fun resumeRun() {
        state = state.copy(shouldTrack = true)
    }

    private fun toggleRun() {
        state = state.copy(
            hasStartedRunning = true,
            shouldTrack = !state.shouldTrack
        )
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

    override fun onCleared() {
        super.onCleared()
        if (!ActiveRunService.isServiceActive) locationManager.stopObservingLocation()
    }

}