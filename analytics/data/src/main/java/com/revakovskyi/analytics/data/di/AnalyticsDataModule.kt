package com.revakovskyi.analytics.data.di

import com.revakovskyi.analytics.data.RoomAnalyticsRepository
import com.revakovskyi.analytics.domain.AnalyticsRepository
import com.revakovskyi.core.database.RunDatabase
import com.revakovskyi.core.database.dao.AnalyticsDao
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val analyticsDataModule = module {

    singleOf(::RoomAnalyticsRepository).bind<AnalyticsRepository>()

    single<AnalyticsDao> {
        get<RunDatabase>().analyticsDao
    }

}