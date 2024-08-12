package com.revakovskyi.run.presentation.activeRun.maps

import androidx.compose.ui.graphics.Color
import com.revakovskyi.core.domain.location.Location

data class PolylineUi(
    val location1: Location,
    val location2: Location,
    val color: Color,
)
