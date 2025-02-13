package com.revakovskyi.run.presentation.runOverview

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.revakovskyi.core.peresentation.ui.ObserveAsEvents
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
import com.revakovskyi.run.presentation.runOverview.components.RunListItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun RunOverviewScreenRoot(
    viewModel: RunOverviewViewModel = koinViewModel(),
    onLogOutClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onStartRunClick: () -> Unit,
) {
    val context = LocalContext.current

    BackHandler { (context as ComponentActivity).finish() }

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            RunOverviewEvent.OnAnalyticsClick -> onAnalyticsClick()
            RunOverviewEvent.OnStartRunClick -> onStartRunClick()
            RunOverviewEvent.OnLogOutClick -> onLogOutClick()
        }
    }

    RunOverviewScreen(
        state = viewModel.state,
        onAction = viewModel::onAction
    )

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RunOverviewScreen(
    state: RunOverviewState,
    onAction: (action: RunOverviewAction) -> Unit,
) {
    val context = LocalContext.current

    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(state = topAppBarState)

    val analyticsIcon = AnalyticsIcon
    val logOutIcon = LogoutIcon

    val menuItems = remember {
        listOf(
            DropDownItem(icon = analyticsIcon, title = context.getString(R.string.analytics)),
            DropDownItem(icon = logOutIcon, title = context.getString(R.string.log_out)),
        )
    }

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
                menuItems = menuItems,
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
        },
    ) { padding ->

        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 16.dp)
        ) {
            items(
                items = state.runs,
                key = { it.id }
            ) { runUi ->

                RunListItem(
                    runUi = runUi,
                    onDeleteClick = { onAction(RunOverviewAction.DeleteRun(runUi)) },
                    modifier = Modifier.animateItem()
                )

            }
        }

    }

}


@Preview
@Composable
private fun RunOverviewActionScreenPreview() {
    RunningTrackerTheme {
        RunOverviewScreen(
            state = RunOverviewState(),
            onAction = {},
        )
    }
}
