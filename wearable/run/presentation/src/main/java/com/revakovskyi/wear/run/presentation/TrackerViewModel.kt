package com.revakovskyi.wear.run.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revakovskyi.core.connectivity.domain.messaging.MessagingAction
import com.revakovskyi.core.domain.util.EmptyDataResult
import com.revakovskyi.core.domain.util.Result
import com.revakovskyi.core.notification.ActiveRunService
import com.revakovskyi.wear.run.domain.ExerciseError
import com.revakovskyi.wear.run.domain.ExerciseTracker
import com.revakovskyi.wear.run.domain.RunningTracker
import com.revakovskyi.wear.run.domain.phone.ConnectorToPhone
import com.revakovskyi.wear.run.presentation.utils.toUiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class TrackerViewModel(
    private val exerciseTracker: ExerciseTracker,
    private val connectorToPhone: ConnectorToPhone,
    private val runningTracker: RunningTracker,
) : ViewModel() {

    var state by mutableStateOf(
        TrackerState(
            hasStartedRunning = ActiveRunService.isServiceActive.value,
            isRunActive = ActiveRunService.isServiceActive.value && runningTracker.isTracking.value,
            isTrackable = ActiveRunService.isServiceActive.value,
        )
    )
        private set

    private var _events = Channel<TrackerEvents>()
    val events = _events.receiveAsFlow()

    private val hasBodySensorPermission = MutableStateFlow(false)

    /**
     * Determines if tracking is active based on the state of the tracker.
     */
    private val isTracking: StateFlow<Boolean> = snapshotFlow {
        state.isRunActive && state.isTrackable && state.isConnectedPhoneNearby
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = false
    )

    private val isAmbientMode = snapshotFlow { state.isAmbientMode }


    init {
        observeConnectedPhone()
        observeIsTrackable()
        syncExerciseTracker()
        observeHeartRateSupported()
        observeHeartRate()
        observeRunDistance()
        observeRunElapsedTime()
        observePhoneActions()
    }


    fun onAction(action: TrackerAction, triggeredOnWatch: Boolean = true) {
        if (triggeredOnWatch) sendActionToPhone(action)

        when (action) {
            is TrackerAction.OnBodySensorPermissionResult -> handleBodySensorPermissionRequest(action.isGranted)
            TrackerAction.OnFinishRunClick -> finishRun()
            TrackerAction.OnToggleRunClick -> markRunAsActive(isActive = !state.isRunActive)
            is TrackerAction.OnEnterAmbientMode -> enterAmbientMode(action.burnInProtectionRequired)
            TrackerAction.OnExitAmbientMode -> exitAmbientMode()
        }
    }

    private fun observeConnectedPhone() {
        connectorToPhone
            .connectedDevice
            .filterNotNull()
            .onEach { deviceNode ->
                state = state.copy(isConnectedPhoneNearby = deviceNode.isNearby)
            }
            .combine(isTracking) { _, isTracking ->
                if (!isTracking) connectorToPhone.sendActionToPhone(MessagingAction.ConnectionRequest)
            }
            .launchIn(viewModelScope)
    }

    private fun observeIsTrackable() {
        runningTracker
            .isTrackable
            .onEach { isTrackable ->
                state = state.copy(isTrackable = isTrackable)
            }
            .launchIn(viewModelScope)
    }

    /**
     * Synchronizes the tracker state with the exercise tracker to handle start, pause, or resume actions.
     */
    private fun syncExerciseTracker() {
        isTracking
            .onEach { isTracking ->
                val result = setActionToExerciseTracker(isTracking)
                handleTrackerActionResult(result)

                if (isTracking) state = state.copy(hasStartedRunning = true)

                runningTracker.setIsTracking(isTracking)
            }
            .launchIn(viewModelScope)
    }

    /**
     * Determines and executes the appropriate action (start, pause, resume) on the exercise tracker.
     *
     * @param isTracking Whether the tracker is currently active.
     * @return Result indicating success or failure of the action.
     */
    private suspend fun setActionToExerciseTracker(isTracking: Boolean) = when {
        isTracking && !state.hasStartedRunning -> exerciseTracker.startExercise()
        isTracking && state.hasStartedRunning -> exerciseTracker.resumeExercise()
        !isTracking && state.hasStartedRunning -> exerciseTracker.pauseExercise()
        else -> Result.Success(Unit)
    }

    private suspend fun handleTrackerActionResult(result: EmptyDataResult<ExerciseError>) {
        if (result is Result.Error) {
            result.error.toUiText()?.let {
                _events.send(TrackerEvents.Error(it))
            }
        }
    }

    private fun observeHeartRateSupported() {
        viewModelScope.launch {
            val isHeartRateTrackingSupported = exerciseTracker.isHeartRateTrackingSupported()
            state = state.copy(canTrackHeartRate = isHeartRateTrackingSupported)
        }
    }

    private fun observeHeartRate() {
        isAmbientMode
            .flatMapLatest { isAmbientMode ->
                if (isAmbientMode) {
                    runningTracker
                        .heartRate
                        .sample(10.seconds)
                } else runningTracker.heartRate
            }
            .onEach { state = state.copy(heartRate = it) }
            .launchIn(viewModelScope)
    }

    private fun observeRunDistance() {
        runningTracker
            .distanceMeters
            .onEach { state = state.copy(distanceMeters = it) }
            .launchIn(viewModelScope)
    }

    private fun observeRunElapsedTime() {
        isAmbientMode
            .flatMapLatest { isAmbientMode ->
                if (isAmbientMode) {
                    runningTracker
                        .elapsedTime
                        .sample(10.seconds)
                } else runningTracker.elapsedTime
            }
            .onEach { state = state.copy(elapsedDuration = it) }
            .launchIn(viewModelScope)
    }

    private fun observePhoneActions() {
        connectorToPhone
            .messagingActions
            .onEach { action -> handlePhoneAction(action) }
            .launchIn(viewModelScope)
    }

    private fun handlePhoneAction(action: MessagingAction) {
        when (action) {
            MessagingAction.Finish -> onAction(TrackerAction.OnFinishRunClick, triggeredOnWatch = false)
            MessagingAction.Pause -> markRunAsActive(isActive = false)
            MessagingAction.StartOrResume -> markRunAsActive(isActive = true)
            MessagingAction.Trackable -> setTrackableValue(isTrackable = true)
            MessagingAction.Untrackable -> setTrackableValue(isTrackable = false)
            else -> Unit
        }
    }

    private fun markRunAsActive(isActive: Boolean) {
        if (state.isTrackable) state = state.copy(isRunActive = isActive)
    }

    private fun setTrackableValue(isTrackable: Boolean) {
        state = state.copy(isTrackable = isTrackable)
    }

    private fun sendActionToPhone(action: TrackerAction) {
        viewModelScope.launch {
            val messagingAction = convertTrackerActionToMessagingAction(action)

            messagingAction?.let {
                val result = connectorToPhone.sendActionToPhone(it)
                if (result is Result.Error) println("Running Tracker error: ${result.error}")
            }
        }
    }

    private fun convertTrackerActionToMessagingAction(action: TrackerAction): MessagingAction? =
        when (action) {
            TrackerAction.OnFinishRunClick -> MessagingAction.Finish
            TrackerAction.OnToggleRunClick -> chooseToggleMessagingAction()
            is TrackerAction.OnBodySensorPermissionResult -> null
            else -> null
        }

    private fun chooseToggleMessagingAction(): MessagingAction =
        if (state.isRunActive) MessagingAction.Pause
        else MessagingAction.StartOrResume

    private fun handleBodySensorPermissionRequest(isGranted: Boolean) {
        hasBodySensorPermission.value = isGranted
        if (isGranted) observeHeartRateSupported()
    }

    private fun finishRun() {
        viewModelScope.launch {
            exerciseTracker.stopExercise()
            _events.send(TrackerEvents.RunFinished)
            resetState()
        }
    }

    private fun resetState() {
        state = state.copy(
            elapsedDuration = Duration.ZERO,
            distanceMeters = 0,
            heartRate = 0,
            hasStartedRunning = false,
            isRunActive = false,
        )
    }

    private fun enterAmbientMode(burnInProtectionRequired: Boolean) {
        state = state.copy(
            isAmbientMode = true,
            burnInProtectionRequired = burnInProtectionRequired,
        )
    }

    private fun exitAmbientMode() {
        state = state.copy(isAmbientMode = false)
    }

}