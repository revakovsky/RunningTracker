package com.revakovskyi.wear.run.presentation.utils

import com.revakovskyi.core.peresentation.ui.UiText
import com.revakovskyi.wear.run.domain.ExerciseError
import com.revakovskyi.wear.run.presentation.R

fun ExerciseError.toUiText(): UiText? {
    return when (this) {
        ExerciseError.ONGOING_OWN_EXERCISE,
        ExerciseError.ONGOING_OTHER_EXERCISE, -> UiText.StringResource(R.string.error_ongoing_exercise)
        ExerciseError.EXERCISE_ALREADY_ENDED -> UiText.StringResource(R.string.error_exercise_ended)
        ExerciseError.UNKNOWN -> UiText.StringResource(com.revakovskyi.core.peresentation.ui.R.string.error_unknown)
        ExerciseError.TRACKING_NOT_SUPPORTED -> null
    }
}
