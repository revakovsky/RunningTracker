package com.revakovskyi.wear.run.data.di

import com.revakovskyi.wear.run.data.HealthServicesExerciseTracker
import com.revakovskyi.wear.run.data.connectivity.WatchToPhoneConnector
import com.revakovskyi.wear.run.domain.ExerciseTracker
import com.revakovskyi.wear.run.domain.RunningTracker
import com.revakovskyi.wear.run.domain.phone.ConnectorToPhone
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val wearRunDataModule = module {

    singleOf(::HealthServicesExerciseTracker).bind<ExerciseTracker>()
    singleOf(::WatchToPhoneConnector).bind<ConnectorToPhone>()
    singleOf(::RunningTracker)

}
