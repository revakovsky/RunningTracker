package com.revakovskyi.wear.run.domain

import com.revakovskyi.core.connectivity.domain.messaging.MessagingAction
import com.revakovskyi.wear.run.domain.phone.ConnectorToPhone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class RunningTracker(
    private val applicationScope: CoroutineScope,
    private val connectorToPhone: ConnectorToPhone,
    private val exerciseTracker: ExerciseTracker,
) {

    private val _heartRate = MutableStateFlow(0)
    val heartRate = _heartRate.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    private val isTracking = _isTracking.asStateFlow()

    private val _isTrackable = MutableStateFlow(false)
    val isTrackable = _isTrackable.asStateFlow()

    val distanceMeters: StateFlow<Int> = connectorToPhone
        .messagingActions
        .filterIsInstance<MessagingAction.DistanceUpdate>()
        .map { it.distanceMeters }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Lazily,
            initialValue = 0
        )

    val elapsedTime: StateFlow<Duration> = connectorToPhone
        .messagingActions
        .filterIsInstance<MessagingAction.TimeUpdate>()
        .map { it.elapsedDuration }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Lazily,
            initialValue = Duration.ZERO
        )


    init {
        observePhoneActions()
        observeDeviceConnections()
        observeHeartRateUpdates()
    }


    private fun observePhoneActions() {
        connectorToPhone
            .messagingActions
            .onEach { action -> handleMessagingAction(action) }
            .launchIn(applicationScope)
    }

    private fun observeDeviceConnections() {
        connectorToPhone
            .connectedDevice
            .filterNotNull()
            .onEach { exerciseTracker.prepareExercise() }
            .launchIn(applicationScope)
    }

    private fun observeHeartRateUpdates() {
        isTracking
            .flatMapLatest { isTracking ->
                if (isTracking) exerciseTracker.heartRate
                else emptyFlow()
            }
            .onEach { heartRate -> updateHeartRate(heartRate) }
            .launchIn(applicationScope)
    }

    private fun handleMessagingAction(action: MessagingAction) {
        when (action) {
            MessagingAction.Trackable -> _isTrackable.value = true
            MessagingAction.Untrackable -> _isTrackable.value = false
            else -> Unit
        }
    }

    private suspend fun updateHeartRate(heartRate: Int) {
        _heartRate.value = heartRate

        connectorToPhone.sendActionToPhone(
            action = MessagingAction.HeartRateUpdate(heartRate)
        )
    }

    fun setIsTracking(isTracking: Boolean) {
        _isTracking.value = isTracking
    }

}