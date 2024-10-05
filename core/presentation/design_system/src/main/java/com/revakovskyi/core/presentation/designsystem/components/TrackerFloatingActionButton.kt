package com.revakovskyi.core.presentation.designsystem.components

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TrackerFloatingActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String? = null,
    iconSize: Dp = 25.dp,
    shouldChangeColor: Boolean = false,
    onClick: () -> Unit,
) {
    val density = LocalDensity.current

    val primaryColor = MaterialTheme.colorScheme.primary
    val inverseColor = MaterialTheme.colorScheme.error

    val buttonColor by remember(shouldChangeColor) {
        mutableStateOf(if (shouldChangeColor) inverseColor else primaryColor)
    }

    val pulseTransition = rememberInfiniteTransition(label = "")

    val pulseRadius by pulseTransition.animateFloat(
        label = "",
        initialValue = 0f,
        targetValue = with(density) { 100.dp.toPx() },
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 48.dp)
    ) {

        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .size(75.dp)
                .clip(CircleShape)
                .clickable { onClick() }
        ) {

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = buttonColor.copy(alpha = 0.3f),
                    radius = pulseRadius,
                    style = Stroke(width = 4.dp.toPx())
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(color = buttonColor)
                    .padding(12.dp)
            ) {

                Icon(
                    modifier = Modifier.size(iconSize),
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = if (shouldChangeColor) {
                        MaterialTheme.colorScheme.secondary
                    } else MaterialTheme.colorScheme.onPrimary,
                )

            }

        }

    }

}
