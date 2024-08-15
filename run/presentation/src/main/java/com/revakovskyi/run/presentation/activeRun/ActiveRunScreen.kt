@file:OptIn(ExperimentalMaterial3Api::class)

package com.revakovskyi.run.presentation.activeRun

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
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
import com.revakovskyi.run.presentation.activeRun.service.ActiveRunService
import com.revakovskyi.run.presentation.util.hasLocationPermission
import com.revakovskyi.run.presentation.util.hasNotificationPermission
import com.revakovskyi.run.presentation.util.shouldShowLocationPermissionRationale
import com.revakovskyi.run.presentation.util.shouldShowNotificationPermissionRationale
import org.koin.androidx.compose.koinViewModel

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
    ) { result ->
        processPermissionLauncherResult(result, context, onAction)
    }

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
                onSnapshot = {},
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


private fun processPermissionLauncherResult(
    result: Map<String, @JvmSuppressWildcards Boolean>,
    context: Context,
    onAction: (action: ActiveRunAction) -> Unit,
) {
    val hasAccessCourseLocation = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    val hasAccessFineLocation = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
    val hasAccessNotification = if (Build.VERSION.SDK_INT >= 33) {
        result[Manifest.permission.POST_NOTIFICATIONS] == true
    } else true

    val activity = context as ComponentActivity
    val showLocationRationale = activity.shouldShowNotificationPermissionRationale()
    val showNotificationRationale = activity.shouldShowNotificationPermissionRationale()

    onAction(
        ActiveRunAction.SubmitLocationPermission(
            hasAcceptedLocationPermission = hasAccessCourseLocation && hasAccessFineLocation,
            showLocationPermissionRationale = showLocationRationale
        )
    )
    onAction(
        ActiveRunAction.SubmitNotificationPermission(
            hasAcceptedNotificationPermission = hasAccessNotification,
            showNotificationPermissionRationale = showNotificationRationale
        )
    )
}

private fun ActivityResultLauncher<Array<String>>.requestTrackerPermissions(context: Context) {
    val hasLocationPermission = context.hasLocationPermission()
    val hasNotificationPermission = context.hasNotificationPermission()

    when {
        !hasLocationPermission && !hasNotificationPermission -> launch(getLocationPermissionsArray() + getNotificationPermissionsArray())
        !hasLocationPermission -> launch(getLocationPermissionsArray())
        !hasNotificationPermission -> launch(getNotificationPermissionsArray())
    }
}

private fun getLocationPermissionsArray() = arrayOf(
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION,
)

private fun getNotificationPermissionsArray() =
    if (Build.VERSION.SDK_INT >= 33) arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    else arrayOf()


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
