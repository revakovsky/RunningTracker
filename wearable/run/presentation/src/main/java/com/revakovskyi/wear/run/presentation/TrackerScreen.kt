package com.revakovskyi.wear.run.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val hasBodySensorPermission = result[Manifest.permission.BODY_SENSORS] == true
        onAction(TrackerAction.OnBodySensorPermissionResult(hasBodySensorPermission))
    }

    LaunchedEffect(key1 = true) {
        runPermissionLauncher(context, permissionLauncher)
    }


    if (state.isConnectedPhoneNearby) RunTrackPreview(state, onAction)
    else ConnectedExclamationPreview()

}


private fun runPermissionLauncher(
    context: Context,
    permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>,
) {
    val hasBodySensorPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.BODY_SENSORS
    ) == PackageManager.PERMISSION_GRANTED

    val hasNotificationPermission = if (Build.VERSION.SDK_INT >= 33) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else true

    val permissions = mutableListOf<String>()

    if (!hasBodySensorPermission) permissions.add(Manifest.permission.BODY_SENSORS)
    if (!hasNotificationPermission && Build.VERSION.SDK_INT >= 33) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    permissionLauncher.launch(permissions.toTypedArray())
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
