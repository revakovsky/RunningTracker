package com.revakovskyi.auth.presentation.di

import com.revakovskyi.auth.presentation.signUp.SignUpViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val authViewModelModule = module {

    viewModelOf(::SignUpViewModel)

}