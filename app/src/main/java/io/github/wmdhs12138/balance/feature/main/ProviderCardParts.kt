package io.github.wmdhs12138.balance.feature.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.wmdhs12138.balance.core.model.BalanceStatus
import io.github.wmdhs12138.balance.core.balance.BalanceParserLabels
import io.github.wmdhs12138.balance.core.model.Provider
import kotlinx.coroutines.delay

@Composable
/** 处理ProviderStatusBar 方法。 */
fun ProviderStatusBar(
    status: BalanceStatus,
    hasCachedBalance: Boolean,
) {
    val stale = hasCachedBalance && status != BalanceStatus.Ready
    val color = when (status) {
        BalanceStatus.Ready -> Color(0xFF1B7F4D)
        BalanceStatus.NeedsLogin -> if (stale) Color(0xFFC28A2C).copy(alpha = 0.62f) else Color(0xFFB26A00)
        BalanceStatus.Forbidden,
        BalanceStatus.NotFound,
        BalanceStatus.Timeout,
        BalanceStatus.NetworkError,
        BalanceStatus.ParserMismatch,
        BalanceStatus.Failed,
        -> if (stale) MaterialTheme.colorScheme.outline.copy(alpha = 0.78f) else Color(0xFFBA1A1A)
        BalanceStatus.NotConnected -> MaterialTheme.colorScheme.outline
    }
    Box(
        modifier = Modifier
            .size(width = 4.dp, height = 64.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(color),
    )
}

@Composable
/** 处理AnimatedBalanceLabel 方法。 */
fun AnimatedBalanceLabel(
    provider: Provider,
    strings: LocalizedStrings,
) {
    val normalizedBalance = provider.displayBalanceText()
    var lastAnimatedBalance by remember(provider.id) { mutableStateOf(normalizedBalance) }
    var balancePulse by remember(provider.id) { mutableStateOf(false) }
    val pulseColor by animateColorAsState(
        targetValue = if (balancePulse && provider.hasDisplayBalance) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(
            durationMillis = MotionDurationMedium,
            easing = FastOutSlowInEasing,
        ),
        label = "balance-change-state-layer",
    )

    LaunchedEffect(normalizedBalance) {
        if (!lastAnimatedBalance.isNullOrBlank() &&
            !normalizedBalance.isNullOrBlank() &&
            lastAnimatedBalance != normalizedBalance
        ) {
            balancePulse = true
            delay(MotionDurationMedium.toLong())
            balancePulse = false
        }
        lastAnimatedBalance = normalizedBalance
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(pulseColor)
            .padding(horizontal = 2.dp, vertical = 1.dp),
    ) {
        AnimatedContent(
            targetState = BalanceLabelState(
                text = provider.displayBalanceOrStatusText(strings),
                hasBalance = provider.hasDisplayBalance,
            ),
            transitionSpec = {
                // Material Design 3 emphasized fade-through: outgoing content fades quickly,
                // incoming content appears slightly scaled with emphasized easing.
                (
                    fadeIn(
                        animationSpec = tween(
                            durationMillis = MotionDurationShort,
                            delayMillis = MotionDurationShort,
                            easing = LinearOutSlowInEasing,
                        ),
                    ) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(
                            durationMillis = MotionDurationMedium,
                            delayMillis = MotionDurationShort,
                            easing = FastOutSlowInEasing,
                        ),
                    )
                    ).togetherWith(
                        fadeOut(
                            animationSpec = tween(
                                durationMillis = MotionDurationShort,
                                easing = FastOutSlowInEasing,
                            ),
                        ) + scaleOut(
                            targetScale = 0.98f,
                            animationSpec = tween(
                                durationMillis = MotionDurationShort,
                                easing = FastOutSlowInEasing,
                            ),
                        ),
                    ).using(
                        SizeTransform(
                            clip = false,
                            sizeAnimationSpec = { _, _ ->
                                tween(
                                    durationMillis = MotionDurationMedium,
                                    easing = FastOutSlowInEasing,
                                )
                            },
                        ),
                    )
            },
            label = "balance-label-change",
        ) { state ->
            Text(
                text = state.text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = if (state.hasBalance) FontWeight.Bold else FontWeight.SemiBold,
                color = if (state.hasBalance) {
                    Color.Unspecified
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** 处理MotionDurationShort 常量。 */
const val MotionDurationShort = 90
/** 处理MotionDurationMedium 常量。 */
const val MotionDurationMedium = 300

/** BalanceLabelState 数据结构。 */
data class BalanceLabelState(
    val text: AnnotatedString,
    val hasBalance: Boolean,
)

@Composable
/** 处理ProviderActionButton 方法。 */
fun ProviderActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String,
    content: @Composable () -> Unit,
) {
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(34.dp)
            .semantics { this.contentDescription = contentDescription },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f),
        contentColor = contentColor,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
/** 处理displayBalanceOrStatusText 方法。 */
fun Provider.displayBalanceOrStatusText(strings: LocalizedStrings): AnnotatedString {
    val balance = displayBalanceText()
    return if (!balance.isNullOrBlank()) {
        balance.withCompactCurrencySymbol()
    } else {
        AnnotatedString(lastErrorText?.friendlyErrorLabel(strings) ?: status.label(strings))
    }
}

@Composable
/** 处理withCompactCurrencySymbol 方法。 */
fun String.withCompactCurrencySymbol(): AnnotatedString {
    val match = Regex("^([¥$])\\s+(.+)$").matchEntire(this) ?: return AnnotatedString(this)
    return buildAnnotatedString {
        pushStyle(
            SpanStyle(
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = FontWeight.SemiBold,
                baselineShift = BaselineShift(0.08f),
            ),
        )
        append(match.groupValues[1])
        pop()
        append(" ")
        append(match.groupValues[2])
    }
}

@Composable
/** 处理ProviderParserTag 方法。 */
fun ProviderParserTag(
    label: String,
    status: BalanceStatus,
    hasCachedBalance: Boolean,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.74f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(6.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            StatusDot(
                status = status,
                hasCachedBalance = hasCachedBalance,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
            )
        }
    }
}

val Provider.parserLabelForCard: String
    get() = BalanceParserLabels.normalize(balanceEndpointHint)

val Provider.usesApiKey: Boolean
    get() = parserLabelForCard.usesApiKeyParser

private val String.usesApiKeyParser: Boolean
    get() = BalanceParserLabels.usesApiKey(this)

@Composable
/** 处理StatusDot 方法。 */
private fun StatusDot(
    status: BalanceStatus,
    hasCachedBalance: Boolean,
    modifier: Modifier = Modifier,
) {
    val stale = hasCachedBalance && status != BalanceStatus.Ready
    val color = when (status) {
        BalanceStatus.Ready -> Color(0xFF1B7F4D)
        BalanceStatus.NeedsLogin -> if (stale) Color(0xFFC28A2C).copy(alpha = 0.62f) else Color(0xFFB26A00)
        BalanceStatus.Forbidden,
        BalanceStatus.NotFound,
        BalanceStatus.Timeout,
        BalanceStatus.NetworkError,
        BalanceStatus.ParserMismatch,
        -> if (stale) MaterialTheme.colorScheme.outline.copy(alpha = 0.78f) else Color(0xFFBA1A1A)
        BalanceStatus.Failed -> if (stale) MaterialTheme.colorScheme.outline.copy(alpha = 0.78f) else Color(0xFFBA1A1A)
        BalanceStatus.NotConnected -> MaterialTheme.colorScheme.outline
    }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
            .size(if (stale) 5.dp else 6.dp),
    )
}
