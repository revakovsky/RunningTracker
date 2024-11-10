package com.revakovskyi.wear.run.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revakovskyi.wear.run.domain.ExerciseTracker
import com.revakovskyi.wear.run.domain.phone.PhoneConnector
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TrackerViewModel(
    private val exerciseTracker: ExerciseTracker,
    private val phoneConnector: PhoneConnector,
) : ViewModel() {

    var state by mutableStateOf(TrackerState())
        private set

    private var eventChannel = Channel<TrackerEvents>()
    val events = eventChannel.receiveAsFlow()

    private val hasBodySensorPermission = MutableStateFlow(false)


    init {
        observeConnectedPhone()
    }

    private fun observeConnectedPhone() {
        phoneConnector
            .connectedDevice
            .filterNotNull()
            .onEach { deviceNode ->
                state = state.copy(isConnectedPhoneNearby = deviceNode.isNearby)
            }
            .launchIn(viewModelScope)
    }


    fun onAction(action: TrackerAction) {
        when (action) {
            is TrackerAction.OnBodySensorPermissionResult -> handleBodySensorPermissionRequest(action.isGranted)
            TrackerAction.OnFinishRunClick -> Unit
            TrackerAction.OnToggleRunClick -> Unit
        }
    }

    private fun handleBodySensorPermissionRequest(isGranted: Boolean) {
        hasBodySensorPermission.value = isGranted

        if (isGranted) {
            viewModelScope.launch {
                val isHeartRateTrackingSupported = exerciseTracker.isHeartRateTrackingSupported()
                state = state.copy(canTrackHeartRate = isHeartRateTrackingSupported)
            }
        }
    }

}