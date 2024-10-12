package com.revakovskyi.core.presentation.design_system_wear

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme

val WearableColorScheme = createColorScheme()
val WearableTypography = createTypography()

@Composable
fun RunningTrackerWearTheme(
    content: @Composable () -> Unit,
) {

    MaterialTheme(
        colorScheme = WearableColorScheme,
        typography = WearableTypography,
    ) {
        content()
    }

}