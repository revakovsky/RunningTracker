package com.revakovskyi.run.presentation.activeRun

import com.revakovskyi.core.peresentation.ui.UiText

sealed interface ActiveRunEvent {

    data class Error(val error: UiText) : ActiveRunEvent
    data object RunSaved : ActiveRunEvent
    data object OnBackClick : ActiveRunEvent

}
