package com.revakovskyi.core.presentation.design_system_wear

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Typography
import com.revakovskyi.core.presentation.designsystem.theme.Poppins
import com.revakovskyi.core.presentation.designsystem.theme.TrackerBlack
import com.revakovskyi.core.presentation.designsystem.theme.TrackerGray
import com.revakovskyi.core.presentation.designsystem.theme.TrackerWhite

fun createTypography(): Typography =
    Typography(
        bodySmall = TextStyle(
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 20.sp,
            color = TrackerWhite,
            textAlign = TextAlign.Center,
        ),
        labelSmall = TextStyle(
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            lineHeight = 20.sp,
            color = TrackerGray,
            textAlign = TextAlign.Start,
        ),
        titleMedium = TextStyle(
            fontFamily = Poppins,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            lineHeight = 28.sp,
            color = TrackerBlack,
            textAlign = TextAlign.Center,
        ),
    )
