package com.revakovskyi.wear.run.presentation.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedIconButton
import com.revakovskyi.core.presentation.designsystem.theme.PauseIcon
import com.revakovskyi.core.presentation.designsystem.theme.StartIcon
import com.revakovskyi.wear.run.presentation.R

@Composable
fun ToggleRunButton(
    modifier: Modifier = Modifier,
    isRunActive: Boolean,
    onClick: () -> Unit,
) {

    OutlinedIconButton(
        onClick = onClick,
        modifier = modifier
    ) {

        if (isRunActive) {
            Icon(
                imageVector = PauseIcon,
                contentDescription = stringResource(R.string.pause_run),
                tint = MaterialTheme.colorScheme.onBackground
            )
        } else {
            Icon(
                imageVector = StartIcon,
                contentDescription = stringResource(R.string.start_run),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

    }

}
