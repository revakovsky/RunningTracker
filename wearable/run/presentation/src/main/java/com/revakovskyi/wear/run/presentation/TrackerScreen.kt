package com.revakovskyi.wear.run.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.revakovskyi.core.peresentation.ui.formatted
import com.revakovskyi.core.peresentation.ui.toFormattedHeartRate
import com.revakovskyi.core.peresentation.ui.toFormattedKm
import com.revakovskyi.core.presentation.design_system_wear.RunningTrackerWearTheme
import com.revakovskyi.core.presentation.designsystem.theme.ExclamationMarkIcon
import com.revakovskyi.core.presentation.designsystem.theme.FinishIcon
import com.revakovskyi.wear.run.presentation.components.ToggleRunButton
import com.revakovskyi.wear.run.presentation.components.TrackerDataCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun TrackerScreenRoot(
    viewModel: TrackerViewModel = koinViewModel(),
) {

    TrackerScreen(
        state = viewModel.state,
        onAction = viewModel::onAction
    )

}


@Composable
private fun TrackerScreen(
    state: TrackerState,
    onAction: (action: TrackerAction) -> Unit,
) {

    if (state.isConnectedPhoneNearby) RunTrackPreview(state, onAction)
    else ConnectedExclamationPreview()

}

@Composable
private fun RunTrackPreview(
    state: TrackerState,
    onAction: (action: TrackerAction) -> Unit,
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        TrackedRunningDetailsRow(state)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = state.elapsedDuration.formatted(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TrackerButtonsRow(state, onAction)

    }

}

@Composable
private fun TrackedRunningDetailsRow(state: TrackerState) {

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {

        TrackerDataCard(
            title = stringResource(R.string.heart_rate),
            value = if (state.canTrackHeartRate) state.heartTate.toFormattedHeartRate()
                    else stringResource(R.string.unsupported),
            textColor = if (state.canTrackHeartRate) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        TrackerDataCard(
            title = stringResource(R.string.distance),
            value = (state.distanceMeters / 1000.0).toFormattedKm(),
            modifier = Modifier.weight(1f)
        )

    }

}


@Composable
private fun TrackerButtonsRow(
    state: TrackerState,
    onAction: (action: TrackerAction) -> Unit,
) {

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {

        if (state.isTrackable) {

            ToggleRunButton(
                isRunActive = state.isRunActive,
                onClick = { onAction(TrackerAction.OnToggleRunClick) }
            )

            if (!state.isRunActive && state.hasStartedRunning) {

                FilledTonalIconButton(
                    onClick = { onAction(TrackerAction.OnFinishRunClick) },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                ) {
                    Icon(
                        imageVector = FinishIcon,
                        contentDescription = stringResource(R.string.finish_run),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

            }

        } else {

            Text(
                text = stringResource(R.string.open_the_new_run_screen),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

        }

    }

}


@Composable
private fun ConnectedExclamationPreview() {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Icon(
            imageVector = ExclamationMarkIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.this_app_requires),
            style = MaterialTheme.typography.bodySmall,
        )

    }

}


@WearPreviewDevices
@Composable
private fun TrackerScreenPreview() {
    RunningTrackerWearTheme {
        TrackerScreen(
            state = TrackerState(
                isConnectedPhoneNearby = true,
                isRunActive = false,
                isTrackable = true,
                hasStartedRunning = true,
                distanceMeters = 10_150,
                canTrackHeartRate = true,
                heartTate = 120
            ),
            onAction = {}
        )
    }
}
