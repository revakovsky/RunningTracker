package com.revakovskyi.run.location.di

import com.revakovskyi.run.domain.LocationObserver
import com.revakovskyi.run.location.AndroidLocationObserver
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val locationModule = module {

    singleOf(::AndroidLocationObserver).bind<LocationObserver>()

}