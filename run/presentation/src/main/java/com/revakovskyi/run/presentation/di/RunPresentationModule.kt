package com.revakovskyi.run.presentation.di

import com.revakovskyi.run.domain.RunningTracker
import com.revakovskyi.run.presentation.activeRun.ActiveRunViewModel
import com.revakovskyi.run.presentation.runOverview.RunOverviewViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val runPresentationModule = module {

    viewModelOf(::RunOverviewViewModel)
    viewModelOf(::ActiveRunViewModel)

    singleOf(::RunningTracker)

}