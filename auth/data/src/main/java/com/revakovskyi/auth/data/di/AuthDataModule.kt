package com.revakovskyi.auth.data.di

import com.revakovskyi.auth.data.EmailPatternValidator
import com.revakovskyi.auth.domain.PatternValidator
import com.revakovskyi.auth.domain.UserDataValidator
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val authDataModule = module {

    single<PatternValidator> { EmailPatternValidator }
    singleOf(::UserDataValidator)

}