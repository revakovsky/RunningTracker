package com.revakovskyi.run.presentation.runOverview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revakovskyi.core.domain.auth.SessionStorage
import com.revakovskyi.core.domain.run.RunRepository
import com.revakovskyi.core.domain.syncing.SyncRunScheduler
import com.revakovskyi.run.presentation.runOverview.mapper.toRunUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

class RunOverviewViewModel(
    private val runRepository: RunRepository,
    private val syncRunScheduler: SyncRunScheduler,
    private val applicationScope: CoroutineScope,
    private val sessionStorage: SessionStorage,
) : ViewModel() {

    var state by mutableStateOf(RunOverviewState())
        private set

    private var eventChannel = Channel<RunOverviewEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        launchScheduledFetchingSync()
        getRunsFromLocalDb()
        fetchRunsFromRemoteDb()
    }

    fun onAction(action: RunOverviewAction) {
        when (action) {
            is RunOverviewAction.DeleteRun -> deleteRun(action.run.id)
            RunOverviewAction.OnLogOutClick -> logOut()
            RunOverviewAction.OnAnalyticsClick -> eventChannel.trySend(RunOverviewEvent.OnAnalyticsClick)
            RunOverviewAction.OnStartRunClick -> eventChannel.trySend(RunOverviewEvent.OnStartRunClick)
        }
    }

    private fun launchScheduledFetchingSync() {
        viewModelScope.launch {
            syncRunScheduler.scheduleSync(
                syncType = SyncRunScheduler.SyncType.FetchRuns(interval = 30.minutes)
            )
        }
    }

    private fun getRunsFromLocalDb() {
        runRepository.getRuns()
            .onEach { runs ->
                val runsUi = runs.map { it.toRunUi() }
                state = state.copy(runs = runsUi)
            }
            .launchIn(viewModelScope)
    }

    private fun fetchRunsFromRemoteDb() {
        viewModelScope.launch {
            runRepository.syncPendingRuns()
            runRepository.fetchRuns()
        }
    }

    private fun deleteRun(runId: String) {
        viewModelScope.launch {
            runRepository.deleteRun(id = runId)
        }
    }

    private fun logOut() {
        eventChannel.trySend(RunOverviewEvent.OnLogOutClick)

        applicationScope.launch {
            syncRunScheduler.cancelAllSyncs()
            runRepository.deleteAllRuns()
            runRepository.logOut()
            sessionStorage.set(null)
        }
    }

}
