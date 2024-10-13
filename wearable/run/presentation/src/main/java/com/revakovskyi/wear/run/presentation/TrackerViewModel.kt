package com.revakovskyi.wear.run.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class TrackerViewModel : ViewModel() {

    var state by mutableStateOf(TrackerState())
        private set

    private var eventChannel = Channel<TrackerEvents>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: TrackerAction) {
        when (action) {
            TrackerAction.OnFinishRunClick -> { /*TODO*/ }
            TrackerAction.OnToggleRunClick -> { /*TODO*/ }
        }
    }

}