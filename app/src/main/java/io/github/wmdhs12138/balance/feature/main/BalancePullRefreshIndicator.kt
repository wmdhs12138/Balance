package io.github.wmdhs12138.balance.feature.main

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefreshIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.wmdhs12138.balance.R

/** 更直观的下拉刷新指示器。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalancePullRefreshIndicator(
    isRefreshing: Boolean,
    state: PullToRefreshState,
    strings: LocalizedStrings,
    modifier: Modifier = Modifier,
) {
    val distanceFraction = state.distanceFraction.coerceIn(0f, 1.5f)
    val transition = rememberInfiniteTransition(label = "pull-refresh-indicator")
    val refreshRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pull-refresh-rotation",
    )
    val pullBounce by transition.animateFloat(
        initialValue = 0f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pull-refresh-bounce",
    )
    val readyToRelease = distanceFraction >= 1f
    val text = when {
        isRefreshing -> strings.get(R.string.pull_refresh_refreshing)
        readyToRelease -> strings.get(R.string.pull_refresh_release)
        else -> strings.get(R.string.pull_refresh_pull)
    }

    Surface(
        modifier = modifier
            .pullToRefreshIndicator(
                state = state,
                isRefreshing = isRefreshing,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                threshold = PullToRefreshDefaults.PositionalThreshold,
            )
            .graphicsLayer {
                scaleX = 0.86f + 0.14f * distanceFraction.coerceAtMost(1f)
                scaleY = scaleX
                alpha = if (isRefreshing) 1f else (0.28f + 0.72f * distanceFraction).coerceIn(0f, 1f)
            },
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        tonalElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isRefreshing) Icons.Default.Refresh else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier
                    .rotate(if (isRefreshing) refreshRotation else 180f * distanceFraction.coerceAtMost(1f))
                    .graphicsLayer {
                        translationY = if (isRefreshing) 0f else pullBounce * distanceFraction.coerceAtMost(1f)
                    },
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
