package io.github.wmdhs12138.balance.feature.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.rotate
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
import io.github.wmdhs12138.balance.R
import io.github.wmdhs12138.balance.core.model.BalanceStatus
import io.github.wmdhs12138.balance.core.model.Provider
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProviderCard(
    provider: Provider,
    strings: LocalizedStrings,
    refreshing: Boolean,
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

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongPress,
            ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        provider.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    ProviderParserTag(
                        label = provider.parserLabelForCard,
                        status = provider.status,
                        hasCachedBalance = provider.hasDisplayBalance,
                    )
                }
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
                if (provider.usesApiKey) {
                    ProviderActionButton(
                        onClick = onApiKey,
                        contentDescription = strings.get(R.string.action_api_key),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                } else {
                    ProviderActionButton(
                        onClick = onLogin,
                        contentDescription = strings.get(R.string.action_login),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Login,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
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
                    AnimatedBalanceLabel(
                        provider = provider,
                        strings = strings,
                    )
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

@Composable
private fun AnimatedBalanceLabel(
    provider: Provider,
    strings: LocalizedStrings,
) {
    val normalizedBalance = provider.balanceText?.normalizeBalanceText()
    var lastAnimatedBalance by remember(provider.id) { mutableStateOf(normalizedBalance) }
    var balancePulse by remember(provider.id) { mutableStateOf(false) }
    val pulseColor by animateColorAsState(
        targetValue = if (balancePulse && provider.hasDisplayBalance) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 420),
        label = "balance-change-pulse",
    )

    LaunchedEffect(normalizedBalance) {
        if (!lastAnimatedBalance.isNullOrBlank() &&
            !normalizedBalance.isNullOrBlank() &&
            lastAnimatedBalance != normalizedBalance
        ) {
            balancePulse = true
            delay(460)
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
                (
                    fadeIn(animationSpec = tween(durationMillis = 180)) +
                        slideInVertically(
                            animationSpec = tween(durationMillis = 260),
                            initialOffsetY = { it / 3 },
                        )
                    ).togetherWith(
                        fadeOut(animationSpec = tween(durationMillis = 120)) +
                            slideOutVertically(
                                animationSpec = tween(durationMillis = 180),
                                targetOffsetY = { -it / 4 },
                            ),
                    ).using(SizeTransform(clip = false))
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

private data class BalanceLabelState(
    val text: AnnotatedString,
    val hasBalance: Boolean,
)

@Composable
private fun ProviderActionButton(
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
private fun Provider.displayBalanceOrStatusText(strings: LocalizedStrings): AnnotatedString {
    val balance = balanceText?.normalizeBalanceText()
    return if (!balance.isNullOrBlank()) {
        balance.withCompactCurrencySymbol()
    } else {
        AnnotatedString(lastErrorText?.friendlyErrorLabel(strings) ?: status.label(strings))
    }
}

@Composable
private fun String.withCompactCurrencySymbol(): AnnotatedString {
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
private fun ProviderParserTag(
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

private val Provider.parserLabelForCard: String
    get() = when {
        balanceEndpointHint.equals("Sub2API", ignoreCase = true) -> "Sub2API"
        balanceEndpointHint.equals("NewAPI", ignoreCase = true) -> "NewAPI"
        balanceEndpointHint.equals("Account Summary", ignoreCase = true) -> "Account Summary"
        balanceEndpointHint.equals("DeepSeek", ignoreCase = true) -> "DeepSeek"
        else -> "NewAPI"
    }

private val Provider.usesApiKey: Boolean
    get() = parserLabelForCard.usesApiKeyParser

private val String.usesApiKeyParser: Boolean
    get() = equals("Account Summary", ignoreCase = true) || equals("DeepSeek", ignoreCase = true)

@Composable
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
