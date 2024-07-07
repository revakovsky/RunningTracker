package com.revakovskyi.core.data.di

import com.revakovskyi.core.data.network.HttpClientFactory
import io.ktor.client.HttpClient
import org.koin.dsl.module

val coreDataModule = module {

    single<HttpClient> {
        HttpClientFactory().build()
    }

}
