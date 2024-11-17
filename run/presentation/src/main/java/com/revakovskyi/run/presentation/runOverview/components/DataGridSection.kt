package com.revakovskyi.run.presentation.runOverview.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.revakovskyi.run.presentation.R
import com.revakovskyi.run.presentation.runOverview.models.RunDataUi
import com.revakovskyi.run.presentation.runOverview.models.RunUi
import kotlin.math.max

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DataGridSection(
    runUi: RunUi,
    modifier: Modifier = Modifier,
) {
    val runDataUiList = listOf(
        RunDataUi(
            paramName = stringResource(id = R.string.distance),
            value = runUi.distance
        ),
        RunDataUi(
            paramName = stringResource(id = R.string.pace),
            value = runUi.pace
        ),
        RunDataUi(
            paramName = stringResource(id = R.string.avg_speed),
            value = runUi.avgSpeed
        ),
        RunDataUi(
            paramName = stringResource(id = R.string.max_speed),
            value = runUi.maxSpeed
        ),
        RunDataUi(
            paramName = stringResource(id = R.string.total_elevation),
            value = runUi.totalElevation
        ),
        RunDataUi(
            paramName = stringResource(id = R.string.avg_heart_rate),
            value = runUi.avgHeartRate
        ),
        RunDataUi(
            paramName = stringResource(id = R.string.max_heart_rate),
            value = runUi.maxHeartRate
        ),
    )

    var maxWidth by remember { mutableIntStateOf(0) }
    val maxWidthDp = with(LocalDensity.current) { maxWidth.toDp() }

    FlowRow(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        runDataUiList.forEach { runDataUi ->
            DataGridCell(
                runDataUi = runDataUi,
                modifier = Modifier
                    .defaultMinSize(minWidth = maxWidthDp)
                    .onSizeChanged {
                        maxWidth = max(maxWidth, it.width)
                    }
            )
        }
    }

}


@Composable
private fun DataGridCell(
    runDataUi: RunDataUi,
    modifier: Modifier = Modifier,
) {

    Column(modifier = modifier) {

        Text(
            text = runDataUi.paramName,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = runDataUi.value,
            style = MaterialTheme.typography.titleSmall
        )

    }

}