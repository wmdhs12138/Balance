package io.github.wmdhs12138.balance.feature.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.wmdhs12138.balance.core.balance.BalanceParserLabels
import io.github.wmdhs12138.balance.core.balance.ParserLabelSuggester
import io.github.wmdhs12138.balance.core.model.BalanceUnit
import io.github.wmdhs12138.balance.core.model.Provider
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
/** 处理BalanceUnitChips 方法。 */
fun BalanceUnitChips(
    selected: BalanceUnit,
    strings: LocalizedStrings,
    onSelected: (BalanceUnit) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        balanceUnitChoices.forEach { unit ->
            FilterChip(
                selected = selected == unit,
                onClick = { onSelected(unit) },
                label = { Text(unit.displayName(strings)) },
            )
        }
    }
}

val balanceUnitChoices = listOf(
    BalanceUnit.Auto,
    BalanceUnit.Cny,
    BalanceUnit.Usd,
    BalanceUnit.Points,
    BalanceUnit.Credits,
    BalanceUnit.Quota,
    BalanceUnit.Tokens,
    BalanceUnit.Unknown,
)

val parserLabels = ParserLabelSuggester.labels

@Composable
/** 处理ParserFilterChip 方法。 */
fun ParserFilterChip(
    label: String,
    selected: Boolean,
    recommended: Boolean = false,
    recommendedText: String = "",
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                if (recommended) {
                    "$label · $recommendedText"
                } else {
                    label
                },
            )
        },
        leadingIcon = {
            Icon(
                imageVector = if (label.usesApiKeyParserForForms) Icons.Default.Key else Icons.AutoMirrored.Filled.Login,
                contentDescription = null,
            )
        },
    )
}

val Provider.parserLabelForForms: String
    get() = when {
        balanceEndpointHint.equals("Sub2API", ignoreCase = true) -> "Sub2API"
        balanceEndpointHint.equals("NewAPI", ignoreCase = true) -> "NewAPI"
        balanceEndpointHint.equals("DeepSeek", ignoreCase = true) -> "DeepSeek"
        else -> "NewAPI"
    }

private val String.usesApiKeyParserForForms: Boolean
    get() = BalanceParserLabels.usesApiKey(this)
