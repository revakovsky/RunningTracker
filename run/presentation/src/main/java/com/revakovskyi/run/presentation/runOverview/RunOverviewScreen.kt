@file:OptIn(ExperimentalMaterial3Api::class)

package com.revakovskyi.run.presentation.runOverview

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.revakovskyi.core.presentation.designsystem.components.TrackerFloatingActionButton
import com.revakovskyi.core.presentation.designsystem.components.TrackerScaffold
import com.revakovskyi.core.presentation.designsystem.components.TrackerToolbar
import com.revakovskyi.core.presentation.designsystem.components.util.DropDownItem
import com.revakovskyi.core.presentation.designsystem.theme.AnalyticsIcon
import com.revakovskyi.core.presentation.designsystem.theme.LogoIcon
import com.revakovskyi.core.presentation.designsystem.theme.LogoutIcon
import com.revakovskyi.core.presentation.designsystem.theme.RunIcon
import com.revakovskyi.core.presentation.designsystem.theme.RunningTrackerTheme
import com.revakovskyi.core.presentation.designsystem.theme.TrackerGreen
import com.revakovskyi.run.presentation.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun RunOverviewScreenRoot(
    viewModel: RunOverviewViewModel = koinViewModel(),
    onStartRunClick: () -> Unit,
) {

    RunOverviewScreen(
        onAction = { action ->
            when (action) {
                RunOverviewAction.OnStartRunClick -> onStartRunClick()
                else -> viewModel.onAction(action)
            }
        }
    )

}


@Composable
private fun RunOverviewScreen(
    onAction: (action: RunOverviewAction) -> Unit,
) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = topAppBarState
    )

    TrackerScaffold(
        topAppBar = {
            TrackerToolbar(
                showBackButton = false,
                title = stringResource(R.string.running_tracker),
                scrollBehavior = scrollBehavior,
                startContent = {
                    Icon(
                        imageVector = LogoIcon,
                        contentDescription = null,
                        tint = TrackerGreen,
                        modifier = Modifier.size(32.dp)
                    )
                },
                menuItems = listOf(
                    DropDownItem(icon = AnalyticsIcon, title = stringResource(R.string.analytics)),
                    DropDownItem(icon = LogoutIcon, title = stringResource(R.string.log_out)),
                ),
                onMenuItemClick = { itemIndex ->
                    when (itemIndex) {
                        0 -> onAction(RunOverviewAction.OnAnalyticsClick)
                        1 -> onAction(RunOverviewAction.OnLogOutClick)
                        else -> Unit
                    }
                }
            )
        },
        floatingActionButton = {
            TrackerFloatingActionButton(
                icon = RunIcon,
                contentDescription = stringResource(R.string.start_a_new_run),
                onClick = { onAction(RunOverviewAction.OnStartRunClick) }
            )
        }
    ) { padding ->

    }

}


@Preview
@Composable
private fun RunOverviewActionScreenPreview() {
    RunningTrackerTheme {
        RunOverviewScreen(
            onAction = {}
        )
    }
}
