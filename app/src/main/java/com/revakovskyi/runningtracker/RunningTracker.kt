package com.revakovskyi.runningtracker

import android.app.Application
import com.revakovskyi.auth.data.di.authDataModule
import com.revakovskyi.auth.presentation.di.authViewModelModule
import com.revakovskyi.core.data.di.coreDataModule
import com.revakovskyi.runningtracker.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class RunningTracker : Application() {

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
            modules(
                appModule,
                authViewModelModule,
                authDataModule,
                coreDataModule,
            )
        }
    }

}