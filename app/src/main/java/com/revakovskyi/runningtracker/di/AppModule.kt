package com.revakovskyi.runningtracker.di

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.revakovskyi.runningtracker.RunningTracker
import com.revakovskyi.runningtracker.presentation.MainViewModel
import kotlinx.coroutines.CoroutineScope
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {

    /**
     * Provides a singleton instance of EncryptedSharedPreferences.
     *
     * EncryptedSharedPreferences ensures that the data stored in the preferences is encrypted
     * using the AndroidX Security library.
     *
     * @return EncryptedSharedPreferences instance configured with AES256 encryption schemes for both keys and values.
     */
    single<SharedPreferences> {
        EncryptedSharedPreferences(
            context = androidApplication(),
            fileName = "Auth_prefs",
            masterKey = MasterKey(context = androidApplication()),
            prefKeyEncryptionScheme = EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            prefValueEncryptionScheme = EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    single<CoroutineScope> {
        (androidApplication() as RunningTracker).applicationScope
    }

    viewModelOf(::MainViewModel)

}