package com.revakovskyi.run.presentation.activeRun

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revakovskyi.core.peresentation.ui.formatted
import com.revakovskyi.core.peresentation.ui.toFormattedKm
import com.revakovskyi.core.peresentation.ui.toFormattedPace
import com.revakovskyi.core.presentation.designsystem.theme.TrackerWhite
import com.revakovskyi.run.domain.RunData
import com.revakovskyi.run.presentation.R
import kotlin.time.Duration

@Composable
fun RunDataCard(
    elapsedTime: Duration,
    runData: RunData,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        RunDataItem(
            title = stringResource(R.string.duration),
            value = elapsedTime.formatted(),
            valueFontSize = 32.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Absolute.SpaceAround
        ) {
            val distanceKm = runData.distanceMeters / 1000.0

            RunDataItem(
                title = stringResource(R.string.distance),
                value = distanceKm.toFormattedKm(),
                modifier = Modifier.defaultMinSize(minWidth = 75.dp)
            )

            RunDataItem(
                title = stringResource(R.string.pace),
                value = elapsedTime.toFormattedPace(distanceKm),
                modifier = Modifier.defaultMinSize(minWidth = 75.dp)
            )

        }

    }

}


@Composable
private fun RunDataItem(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    valueFontSize: TextUnit = 16.sp,
) {

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = valueFontSize,
                color = TrackerWhite
            )
        )

    }

}
