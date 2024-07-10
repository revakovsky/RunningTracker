package com.revakovskyi.core.data.di

import com.revakovskyi.core.data.auth.EncryptedSessionStorage
import com.revakovskyi.core.data.network.HttpClientFactory
import com.revakovskyi.core.domain.auth.SessionStorage
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {

    single<HttpClient> {
        HttpClientFactory(sessionStorage = get()).build()
    }

    singleOf(::EncryptedSessionStorage).bind<SessionStorage>()

}
