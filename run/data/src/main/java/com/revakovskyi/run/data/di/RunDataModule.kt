package com.revakovskyi.run.data.di

import com.revakovskyi.core.domain.syncing.SyncRunScheduler
import com.revakovskyi.run.data.SyncRunWorkerScheduler
import com.revakovskyi.run.data.connectivity.PhoneToWatchConnector
import com.revakovskyi.run.data.workers.CreateRunWorker
import com.revakovskyi.run.data.workers.DeleteRunWorker
import com.revakovskyi.run.data.workers.FetchRunsWorker
import com.revakovskyi.run.domain.wear.WatchConnector
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val runDataModule = module {

    workerOf(::CreateRunWorker)
    workerOf(::FetchRunsWorker)
    workerOf(::DeleteRunWorker)

    singleOf(::SyncRunWorkerScheduler).bind<SyncRunScheduler>()
    singleOf(::PhoneToWatchConnector).bind<WatchConnector>()

}
