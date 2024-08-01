package com.revakovskyi.run.presentation.activeRun

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revakovskyi.run.domain.RunningTracker
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber

class ActiveRunViewModel(
    private val runningTracker: RunningTracker,
) : ViewModel() {

    var state by mutableStateOf(ActiveRunState())
        private set

    private var eventChannel = Channel<ActiveRunEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _hasLocationPermission = MutableStateFlow(false)


    init {
        _hasLocationPermission
            .onEach { hasPermission ->
                if (hasPermission) runningTracker.startObservingLocation()
                else runningTracker.stopObservingLocation()
            }
            .launchIn(viewModelScope)


        // TEST
        runningTracker.currentLocation
            .onEach { locationWithAltitude ->
                Timber.d("New location - $locationWithAltitude")
            }
            .launchIn(viewModelScope)
    }


    fun onAction(action: ActiveRunAction) {
        when (action) {
            ActiveRunAction.OnDismissDialog -> Unit /*TODO*/
            ActiveRunAction.OnFinishRunClick -> Unit /*TODO*/
            ActiveRunAction.OnResumeRunClick -> Unit /*TODO*/
            ActiveRunAction.OnToggleRunClick -> Unit /*TODO*/
            is ActiveRunAction.SubmitLocationPermission -> submitLocationPermission(action)
            is ActiveRunAction.SubmitNotificationPermission -> submitNotificationPermission(action)
            ActiveRunAction.DismissRationaleDialog -> dismissRationaleDialog()
            else -> Unit
        }
    }

    private fun submitLocationPermission(action: ActiveRunAction.SubmitLocationPermission) {
        _hasLocationPermission.value = action.hasAcceptedLocationPermission
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

}