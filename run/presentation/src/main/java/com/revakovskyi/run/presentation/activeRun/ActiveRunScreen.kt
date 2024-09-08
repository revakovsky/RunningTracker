@file:OptIn(ExperimentalMaterial3Api::class)

package com.revakovskyi.run.presentation.activeRun

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.revakovskyi.core.presentation.designsystem.components.ActionButton
import com.revakovskyi.core.presentation.designsystem.components.OutlinedActionButton
import com.revakovskyi.core.presentation.designsystem.components.TrackerDialog
import com.revakovskyi.core.presentation.designsystem.components.TrackerFloatingActionButton
import com.revakovskyi.core.presentation.designsystem.components.TrackerScaffold
import com.revakovskyi.core.presentation.designsystem.components.TrackerToolbar
import com.revakovskyi.core.presentation.designsystem.theme.RunningTrackerTheme
import com.revakovskyi.core.presentation.designsystem.theme.StartIcon
import com.revakovskyi.core.presentation.designsystem.theme.StopIcon
import com.revakovskyi.run.presentation.R
import com.revakovskyi.run.presentation.activeRun.components.RunDataCard
import com.revakovskyi.run.presentation.activeRun.maps.TrackerMap
import com.revakovskyi.run.presentation.activeRun.permissions.hasLocationPermission
import com.revakovskyi.run.presentation.activeRun.permissions.hasNotificationPermission
import com.revakovskyi.run.presentation.activeRun.permissions.processPermissionLauncherResult
import com.revakovskyi.run.presentation.activeRun.permissions.requestTrackerPermissions
import com.revakovskyi.run.presentation.activeRun.permissions.shouldShowLocationPermissionRationale
import com.revakovskyi.run.presentation.activeRun.permissions.shouldShowNotificationPermissionRationale
import com.revakovskyi.run.presentation.activeRun.service.ActiveRunService
import org.koin.androidx.compose.koinViewModel
import java.io.ByteArrayOutputStream

@Composable
fun ActiveRunScreenRoot(
    viewModel: ActiveRunViewModel = koinViewModel(),
    onServiceToggle: (shouldServiceRun: Boolean) -> Unit,
) {

    ActiveRunScreen(
        state = viewModel.state,
        onServiceToggle = onServiceToggle,
        onAction = viewModel::onAction
    )

}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ActiveRunScreen(
    state: ActiveRunState,
    onServiceToggle: (shouldServiceRun: Boolean) -> Unit,
    onAction: (action: ActiveRunAction) -> Unit,
) {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result -> processPermissionLauncherResult(result, context, onAction) }

    LaunchedEffect(key1 = true) {
        val activity = context as ComponentActivity
        val showLocationRationale = activity.shouldShowLocationPermissionRationale()
        val showNotificationRationale = activity.shouldShowNotificationPermissionRationale()

        onAction(
            ActiveRunAction.SubmitLocationPermission(
                hasAcceptedLocationPermission = context.hasLocationPermission(),
                showLocationPermissionRationale = showLocationRationale
            )
        )
        onAction(
            ActiveRunAction.SubmitNotificationPermission(
                hasAcceptedNotificationPermission = context.hasNotificationPermission(),
                showNotificationPermissionRationale = showNotificationRationale
            )
        )

        if (!showLocationRationale && !showNotificationRationale) {
            permissionLauncher.requestTrackerPermissions(context)
        }
    }

    LaunchedEffect(key1 = state.shouldTrack) {
        if (context.hasNotificationPermission() && state.shouldTrack && !ActiveRunService.isServiceActive) {
            onServiceToggle(true)
        }
    }

    LaunchedEffect(key1 = state.isRunFinished) {
        if (state.isRunFinished) onServiceToggle(false)
    }

    TrackerScaffold(
        withGradient = false,
        topAppBar = {
            TrackerToolbar(
                showBackButton = true,
                title = stringResource(R.string.active_run),
                onBackClick = { onAction(ActiveRunAction.OnBackClick) }
            )
        },
        floatingActionButton = {
            TrackerFloatingActionButton(
                icon = if (state.shouldTrack) StopIcon else StartIcon,
                onClick = { onAction(ActiveRunAction.OnToggleRunClick) },
                iconSize = 20.dp,
                contentDescription = stringResource(
                    if (state.shouldTrack) R.string.pause_a_run
                    else R.string.start_a_new_run
                )
            )
        },
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {

            TrackerMap(
                isRunFinished = state.isRunFinished,
                currentLocation = state.currentLocation,
                locations = state.runData.locations,
                onSnapshot = { bitmap ->
                    val stream = ByteArrayOutputStream()
                    stream.use {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, it)
                    }
                    onAction(ActiveRunAction.OnRunProcessed(stream.toByteArray()))
                },
            )

            RunDataCard(
                elapsedTime = state.elapsedTime,
                runData = state.runData,
                modifier = Modifier
                    .padding(16.dp)
                    .padding(paddingValues)
                    .fillMaxWidth()
            )

        }
    }

    if (!state.shouldTrack && state.hasStartedRunning) {
        TrackerDialog(
            title = stringResource(R.string.running_is_paused),
            description = stringResource(R.string.resume_or_finish_the_workout),
            onDismiss = { onAction(ActiveRunAction.OnResumeRunClick) },
            primaryButton = {
                ActionButton(
                    text = stringResource(R.string.resume),
                    isLoading = false,
                    onClick = { onAction(ActiveRunAction.OnResumeRunClick) },
                    modifier = Modifier.weight(1f)
                )
            },
            secondaryButton = {
                OutlinedActionButton(
                    text = stringResource(R.string.finish),
                    isLoading = state.isSavingRun,
                    onClick = { onAction(ActiveRunAction.OnFinishRunClick) },
                    modifier = Modifier.weight(1f)
                )
            }
        )
    }

    if (state.showLocationRationale || state.showNotificationRationale) {
        TrackerDialog(
            title = stringResource(R.string.permission_is_required),
            description = stringResource(
                id = when {
                    state.showLocationRationale && state.showNotificationRationale -> R.string.location_notification_rationale
                    state.showLocationRationale -> R.string.location_rationale
                    else -> R.string.notification_rationale
                }
            ),
            onDismiss = { /*Normal dismissing not allowed for the permissions*/ },
            primaryButton = {
                OutlinedActionButton(
                    text = stringResource(R.string.okay),
                    isLoading = false,
                    onClick = {
                        onAction(ActiveRunAction.DismissRationaleDialog)
                        permissionLauncher.requestTrackerPermissions(context)
                    }
                )
            }
        )
    }

}


@Preview
@Composable
private fun ActiveRunScreenPreview() {
    RunningTrackerTheme {
        ActiveRunScreen(
            state = ActiveRunState(),
            onServiceToggle = {},
            onAction = {}
        )
    }
}
