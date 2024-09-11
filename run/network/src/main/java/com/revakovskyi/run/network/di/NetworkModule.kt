package com.revakovskyi.run.network.di

import com.revakovskyi.core.domain.run.RemoteRunDataSource
import com.revakovskyi.run.network.KtorRemoteRunDataSource
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {

    singleOf(::KtorRemoteRunDataSource).bind<RemoteRunDataSource>()

}