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

/**
 * The `RunningTracker` class coordinates tracking operations, including heart rate, distance,
 * and elapsed time, while interacting with a connected phone and exercise tracker.
 *
 * @property applicationScope The coroutine scope for launching asynchronous tasks.
 * @property connectorToPhone The communication client that handles messaging actions with the phone.
 * @property exerciseTracker The tracker responsible for monitoring heart rate and exercise status.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RunningTracker(
    private val applicationScope: CoroutineScope,
    private val connectorToPhone: ConnectorToPhone,
    private val exerciseTracker: ExerciseTracker,
) {

    private val _heartRate = MutableStateFlow(0)
    val heartRate = _heartRate.asStateFlow()

    /**
     * Tracks whether the exercise session is currently active.
     */
    private val _isTracking = MutableStateFlow(false)
    val isTracking = _isTracking.asStateFlow()

    /***
     * Indicates whether the exercise tracker is ready to monitor the session.
     */
    private val _isTrackable = MutableStateFlow(false)
    val isTrackable = _isTrackable.asStateFlow()

    /***
     * Tracks the distance covered during the session, as reported by the phone.
     */
    val distanceMeters: StateFlow<Int> = connectorToPhone
        .messagingActions
        .filterIsInstance<MessagingAction.DistanceUpdate>()
        .map { it.distanceMeters }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Lazily,
            initialValue = 0
        )

    /***
     * Tracks the elapsed time during the session, as reported by the phone.
     */
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


    fun setIsTracking(isTracking: Boolean) {
        _isTracking.value = isTracking
    }

    /**
     * Observes actions sent from the phone and updates the tracker state accordingly.
     */
    private fun observePhoneActions() {
        connectorToPhone
            .messagingActions
            .onEach { action -> handleMessagingAction(action) }
            .launchIn(applicationScope)
    }

    /**
     * Monitors connections to devices and prepares the exercise tracker when a device is connected.
     */
    private fun observeDeviceConnections() {
        connectorToPhone
            .connectedDevice
            .filterNotNull()
            .onEach { exerciseTracker.prepareExercise() }
            .launchIn(applicationScope)
    }

    /**
     * Subscribes to heart rate updates from the exercise tracker when tracking is active.
     * Updates the heart rate state and notifies the phone of the new value.
     */
    private fun observeHeartRateUpdates() {
        isTracking
            .flatMapLatest { isTracking ->
                if (isTracking) exerciseTracker.heartRate
                else emptyFlow()
            }
            .onEach { heartRate -> updateHeartRate(heartRate) }
            .launchIn(applicationScope)
    }

    /**
     * Handles specific messaging actions received from the phone.
     *
     * @param action The `MessagingAction` received from the phone.
     */
    private fun handleMessagingAction(action: MessagingAction) {
        when (action) {
            MessagingAction.Trackable -> _isTrackable.value = true
            MessagingAction.Untrackable -> _isTrackable.value = false
            else -> Unit
        }
    }

    /**
     * Updates the heart rate state and notifies the phone with the new value.
     *
     * @param heartRate The current heart rate to be updated.
     */
    private suspend fun updateHeartRate(heartRate: Int) {
        _heartRate.value = heartRate

        connectorToPhone.sendActionToPhone(
            action = MessagingAction.HeartRateUpdate(heartRate)
        )
    }

}