package com.revakovskyi.core.data.di

import com.revakovskyi.core.data.auth.EncryptedSessionStorage
import com.revakovskyi.core.data.network.HttpClientFactory
import com.revakovskyi.core.data.run.OfflineFirstRunRepository
import com.revakovskyi.core.domain.auth.SessionStorage
import com.revakovskyi.core.domain.run.RunRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {

    /**
     * Provides an instance of [HttpClient], configured with:
     * - [CIO] as the HTTP engine. Can be replaced for any other engine including testing one
     * - A [SessionStorage] implementation for session management.
     */
    single<HttpClient> {
        HttpClientFactory(sessionStorage = get()).build(CIO.create())
    }

    singleOf(::EncryptedSessionStorage).bind<SessionStorage>()

    singleOf(::OfflineFirstRunRepository).bind<RunRepository>()

}
