package com.revakovskyi.analytics.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.revakovskyi.analytics.presentation.components.AnalyticsCard
import com.revakovskyi.analytics.presentation.components.AnalyticsParam
import com.revakovskyi.core.presentation.designsystem.components.TrackerScaffold
import com.revakovskyi.core.presentation.designsystem.components.TrackerToolbar
import com.revakovskyi.core.presentation.designsystem.theme.RunningTrackerTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun AnalyticsScreenRoot(
    viewModel: AnalyticsViewModel = koinViewModel(),
    onBackClick: () -> Unit,
) {

    AnalyticsScreen(
        state = viewModel.state,
        onAction = { action ->
            when (action) {
                AnalyticsAction.OnBackClick -> onBackClick()
            }
        }
    )

}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AnalyticsScreen(
    state: AnalyticsState?,
    onAction: (action: AnalyticsAction) -> Unit,
) {

    TrackerScaffold(
        topAppBar = {
            TrackerToolbar(
                showBackButton = true,
                title = stringResource(id = R.string.analytics),
                onBackClick = { onAction(AnalyticsAction.OnBackClick) }
            )
        }
    ) { padding ->

        if (state == null) {

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                CircularProgressIndicator()
            }

        } else {

            LazyVerticalGrid(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                items(state.params) { analyticsParam ->

                    AnalyticsCard(
                        title = stringResource(analyticsParam.paramNameResId),
                        value = analyticsParam.paramValue,
                    )

                }
            }

        }

    }

}


@Preview
@Composable
private fun ScreenPreview() {
    RunningTrackerTheme {
        AnalyticsScreen(
            state = AnalyticsState(
                listOf(
                    AnalyticsParam(R.string.total_run_distance, "200 km"),
                    AnalyticsParam(R.string.total_run_time, "20d 0h 15min"),
                    AnalyticsParam(R.string.fastest_ever_run, "143.9 km/h"),
                    AnalyticsParam(R.string.average_run_distance, "10.4 km"),
                    AnalyticsParam(R.string.average_run_pace, "07:10"),
                )
            ),
            onAction = {}
        )
    }
}
