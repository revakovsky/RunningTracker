package com.revakovskyi.runningtracker

import android.app.Application
import com.revakovskyi.auth.data.di.authDataModule
import com.revakovskyi.auth.presentation.di.authViewModelModule
import com.revakovskyi.core.data.di.coreDataModule
import com.revakovskyi.core.database.di.databaseModule
import com.revakovskyi.run.data.di.runDataModule
import com.revakovskyi.run.location.di.locationModule
import com.revakovskyi.run.network.di.networkModule
import com.revakovskyi.run.presentation.di.runPresentationModule
import com.revakovskyi.runningtracker.di.appModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import timber.log.Timber

class RunningTracker : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()

        initTimber()
        initKoin()
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initKoin() {
        startKoin {
            androidLogger()
            androidContext(this@RunningTracker)
            workManagerFactory()
            modules(
                appModule,
                authViewModelModule,
                authDataModule,
                coreDataModule,
                runPresentationModule,
                locationModule,
                databaseModule,
                networkModule,
                runDataModule,
            )
        }
    }

}