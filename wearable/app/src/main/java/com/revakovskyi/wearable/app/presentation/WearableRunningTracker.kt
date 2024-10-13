package com.revakovskyi.wearable.app.presentation

import android.app.Application
import com.revakovskyi.wear.run.presentation.di.wearPresentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class WearableRunningTracker : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidLogger()
            androidContext(this@WearableRunningTracker)
            modules(wearPresentationModule)
        }
    }

}
