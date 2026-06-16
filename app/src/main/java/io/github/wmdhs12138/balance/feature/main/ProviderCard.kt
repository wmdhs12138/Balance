package io.github.wmdhs12138.balance.feature.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.wmdhs12138.balance.R
import io.github.wmdhs12138.balance.core.model.Provider

/** 渲染服务商卡片。 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProviderCard(
    provider: Provider,
    strings: LocalizedStrings,
    refreshing: Boolean,
    sortingMode: Boolean,
    isDragging: Boolean = false,
    dragHandleModifier: Modifier = Modifier,
    onRefresh: () -> Unit,
    onLogin: () -> Unit,
    onApiKey: () -> Unit,
    onLongPress: () -> Unit,
) {
    val refreshTransition = rememberInfiniteTransition(label = "provider-refresh")
    val refreshRotation by refreshTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "provider-refresh-rotation",
    )
    val dragElevation by animateDpAsState(
        targetValue = if (isDragging) 10.dp else 1.dp,
        animationSpec = tween(durationMillis = 180),
        label = "provider-drag-elevation",
    )
    val dragScale by animateFloatAsState(
        targetValue = if (isDragging) 1.025f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "provider-drag-scale",
    )

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isDragging) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.92f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = dragElevation,
            focusedElevation = dragElevation,
            hoveredElevation = dragElevation,
            pressedElevation = dragElevation,
        ),
        modifier = if (sortingMode) {
            Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = dragScale
                    scaleY = dragScale
                }
                .then(dragHandleModifier)
        } else {
            Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongPress,
                )
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (sortingMode) {
                IconButton(
                    onClick = {},
                ) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = strings.get(R.string.action_sort),
                    )
                }
            } else {
                ProviderStatusBar(
                    status = provider.status,
                    hasCachedBalance = provider.hasDisplayBalance,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = provider.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    ProviderParserTag(
                        label = provider.parserLabelForCard,
                        status = provider.status,
                        hasCachedBalance = provider.hasDisplayBalance,
                    )
                    if (!sortingMode) {
                        ProviderActionButton(
                            onClick = onRefresh,
                            enabled = !refreshing,
                            contentDescription = strings.get(R.string.action_refresh_provider),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(if (refreshing) refreshRotation else 0f),
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                    ) {
                        AnimatedBalanceLabel(provider = provider, strings = strings)
                        if (!sortingMode) {
                            provider.secondaryStatusLabel(strings)?.let { label ->
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.72f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    Text(
                        text = provider.compactRefreshLabel(strings),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
