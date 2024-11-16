package com.revakovskyi.wear.run.presentation

import com.revakovskyi.core.peresentation.ui.UiText

sealed interface TrackerEvents {

    data object RunFinished : TrackerEvents
    data class Error(val message: UiText) : TrackerEvents

}
