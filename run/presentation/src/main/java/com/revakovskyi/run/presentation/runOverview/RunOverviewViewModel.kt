package com.revakovskyi.run.presentation.runOverview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revakovskyi.core.domain.run.RunRepository
import com.revakovskyi.core.domain.syncing.SyncRunScheduler
import com.revakovskyi.run.presentation.runOverview.mapper.toRunUi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

class RunOverviewViewModel(
    private val runRepository: RunRepository,
    private val syncRunScheduler: SyncRunScheduler,
) : ViewModel() {

    var state by mutableStateOf(RunOverviewState())
        private set

    init {
        launchScheduledFetchingSync()
        getRunsFromLocalDb()
        fetchRunsFromRemoteDb()
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

    fun onAction(action: RunOverviewAction) {
        when (action) {
            RunOverviewAction.OnAnalyticsClick -> Unit      // processed on the screen
            RunOverviewAction.OnLogOutClick -> Unit         //TODO: add logic
            RunOverviewAction.OnStartRunClick -> Unit       //TODO: add logic
            is RunOverviewAction.DeleteRun -> deleteRun(action.run.id)
        }
    }

    private fun deleteRun(runId: String) {
        viewModelScope.launch {
            runRepository.deleteRun(id = runId)
        }
    }

}
