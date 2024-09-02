package com.revakovskyi.run.presentation.runOverview.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.revakovskyi.run.presentation.R
import com.revakovskyi.run.presentation.runOverview.models.RunUi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RunListItem(
    runUi: RunUi,
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit,
) {
    var showDeleteButton by remember { mutableStateOf(false) }

    Box(modifier = Modifier) {

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier
                .clip(RoundedCornerShape(15.dp))
                .background(MaterialTheme.colorScheme.surface)
                .combinedClickable(
                    onClick = {},
                    onLongClick = { showDeleteButton = true }
                )
                .padding(16.dp),
        ) {

            MapImageSection(imageUrl = runUi.mapPictureUrl)

            RunningTimeSection(duration = runUi.duration)

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )

            RunningDateSection(dateTime = runUi.dateTime)

            DataGridSection(runUi = runUi)

        }

        DropdownMenu(
            expanded = showDeleteButton,
            onDismissRequest = { showDeleteButton = false }
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(id = R.string.delete),
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                onClick = {
                    showDeleteButton = false
                    onDeleteClick()
                }
            )
        }

    }

}
