package io.github.wmdhs12138.balance.feature.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.wmdhs12138.balance.R

/** 首页胶囊式功能栏。 */
@Composable
fun MainActionBar(
    strings: LocalizedStrings,
    sortingMode: Boolean,
    onAdd: () -> Unit,
    onSort: () -> Unit,
    onSettings: () -> Unit,
    onDoneSorting: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(999.dp),
        color = if (sortingMode) {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.92f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.82f)
        },
        contentColor = if (sortingMode) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        tonalElevation = if (sortingMode) 3.dp else 1.dp,
    ) {
        if (sortingMode) {
            SortingActionBarContent(
                strings = strings,
                onDoneSorting = onDoneSorting,
            )
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ActionPillButton(
                    icon = Icons.Default.Add,
                    label = strings.get(R.string.action_add),
                    onClick = onAdd,
                    modifier = Modifier.weight(1f),
                )
                ActionPillButton(
                    icon = Icons.Default.SwapVert,
                    label = strings.get(R.string.action_sort),
                    onClick = onSort,
                    modifier = Modifier.weight(1f),
                )
                ActionPillButton(
                    icon = Icons.Default.Settings,
                    label = strings.get(R.string.settings_title),
                    onClick = onSettings,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** 排序模式下的提示与完成操作。 */
@Composable
private fun SortingActionBarContent(
    strings: LocalizedStrings,
    onDoneSorting: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = strings.get(R.string.sort_providers_hint),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onDoneSorting) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text(strings.get(R.string.action_done), fontWeight = FontWeight.SemiBold)
        }
    }
}

/** 胶囊功能栏中的单个操作按钮。 */
@Composable
private fun ActionPillButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
