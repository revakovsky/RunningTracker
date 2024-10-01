package com.revakovskyi.analytics.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revakovskyi.analytics.domain.AnalyticsRepository
import com.revakovskyi.analytics.presentation.mapper.toAnalyticsDashboardValues
import com.revakovskyi.analytics.presentation.models.AnalyticsParam
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val analyticsRepository: AnalyticsRepository,
) : ViewModel() {

    var state by mutableStateOf<AnalyticsState?>(null)
        private set

    init {
        viewModelScope.launch {
            val analyticsValues = analyticsRepository.getAnalyticsValues().toAnalyticsDashboardValues()

            state = AnalyticsState(
                listOf(
                    AnalyticsParam(R.string.total_run_distance, analyticsValues.totalRunDistance),
                    AnalyticsParam(R.string.total_run_time, analyticsValues.totalRunTime),
                    AnalyticsParam(R.string.fastest_ever_run, analyticsValues.fastestEverRun),
                    AnalyticsParam(R.string.average_run_distance, analyticsValues.avgDistancePerRun),
                    AnalyticsParam(R.string.average_run_pace, analyticsValues.avgPacePerRun),
                )
            )
        }
    }

}
