package com.revakovskyi.run.presentation.di

import com.revakovskyi.run.presentation.activeRun.ActiveRunViewModel
import com.revakovskyi.run.presentation.runOverview.RunOverviewViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val runViewModelModule = module {

    viewModelOf(::RunOverviewViewModel)
    viewModelOf(::ActiveRunViewModel)

}