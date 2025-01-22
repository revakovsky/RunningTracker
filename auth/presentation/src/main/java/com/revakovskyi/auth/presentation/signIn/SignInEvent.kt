package com.revakovskyi.auth.presentation.signIn

import com.revakovskyi.core.peresentation.ui.UiText

sealed interface SignInEvent {

    data class Error(val error: UiText) : SignInEvent
    data object SignInSuccess : SignInEvent
    data object OnSignUpClick : SignInEvent

}
