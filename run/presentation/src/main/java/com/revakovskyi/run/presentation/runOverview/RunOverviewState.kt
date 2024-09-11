package com.revakovskyi.run.presentation.runOverview

import com.revakovskyi.run.presentation.runOverview.models.RunUi

data class RunOverviewState(
    val runs: List<RunUi> = emptyList(),
)
