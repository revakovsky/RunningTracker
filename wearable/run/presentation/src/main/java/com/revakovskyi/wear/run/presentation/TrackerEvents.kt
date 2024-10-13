package com.revakovskyi.wear.run.presentation

sealed interface TrackerEvents {

    data object RunFinished : TrackerEvents

}
