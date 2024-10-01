package com.revakovskyi.analytics.presentation.di

import com.revakovskyi.analytics.presentation.AnalyticsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val analyticsPresentationModule = module {

    viewModelOf(::AnalyticsViewModel)

}
