package com.revakovskyi.auth.presentation.signUp

import com.revakovskyi.core.peresentation.ui.UiText

sealed interface SignUpEvent {

    data object RegistrationSuccess : SignUpEvent
    data class Error(val error: UiText) : SignUpEvent

}
