package com.revakovskyi.wearable.app.presentation.di

import com.revakovskyi.wearable.app.presentation.WearableRunningTracker
import kotlinx.coroutines.CoroutineScope
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module {

    single<CoroutineScope> {
        (androidApplication() as WearableRunningTracker).applicationScope
    }

}
