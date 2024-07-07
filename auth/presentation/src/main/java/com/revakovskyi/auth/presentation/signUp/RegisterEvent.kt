package com.revakovskyi.auth.presentation.signUp

import com.revakovskyi.core.peresentation.ui.UiText

sealed interface RegisterEvent {

    data object RegistrationSuccess : RegisterEvent
    data class Error(val error: UiText) : RegisterEvent

}
