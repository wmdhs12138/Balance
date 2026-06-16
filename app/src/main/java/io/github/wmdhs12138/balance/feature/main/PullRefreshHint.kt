package io.github.wmdhs12138.balance.feature.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.wmdhs12138.balance.R

/** 使用与真实下拉刷新相同风格的首次提示。 */
@Composable
fun PullRefreshHint(
    visible: Boolean,
    strings: LocalizedStrings,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "pull-refresh-hint")
    val pullProgress by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pull-refresh-hint-progress",
    )
    val alpha by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pull-refresh-hint-alpha",
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(260)),
        exit = fadeOut(tween(260)),
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier
                .graphicsLayer {
                    translationY = -22f + 30f * pullProgress
                    scaleX = 0.86f + 0.14f * pullProgress
                    scaleY = scaleX
                },
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            tonalElevation = 4.dp,
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .alpha(alpha),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = 180f * pullProgress
                        translationY = 5f * pullProgress
                    },
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = strings.get(R.string.pull_refresh_hint),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
