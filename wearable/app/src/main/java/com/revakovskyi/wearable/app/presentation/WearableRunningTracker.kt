package com.revakovskyi.wearable.app.presentation

import android.app.Application
import com.revakovskyi.core.connectivity.data.di.coreConnectivityDataModule
import com.revakovskyi.wear.run.data.di.wearRunDataModule
import com.revakovskyi.wear.run.presentation.di.wearRunPresentationModule
import com.revakovskyi.wearable.app.presentation.di.appModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class WearableRunningTracker : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidLogger()
            androidContext(this@WearableRunningTracker)
            modules(
                appModule,
                wearRunPresentationModule,
                wearRunDataModule,
                coreConnectivityDataModule,
            )
        }
    }

}
