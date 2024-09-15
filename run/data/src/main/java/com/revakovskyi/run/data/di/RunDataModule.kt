package com.revakovskyi.run.data.di

import com.revakovskyi.run.data.CreateRunWorker
import com.revakovskyi.run.data.DeleteRunWorker
import com.revakovskyi.run.data.FetchRunsWorker
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.dsl.module

val runDataModule = module {

    workerOf(::CreateRunWorker)
    workerOf(::FetchRunsWorker)
    workerOf(::DeleteRunWorker)

}