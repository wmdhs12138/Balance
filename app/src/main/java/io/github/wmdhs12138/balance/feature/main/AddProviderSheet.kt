package io.github.wmdhs12138.balance.feature.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.wmdhs12138.balance.R
import io.github.wmdhs12138.balance.core.balance.ParserLabelSuggester
import io.github.wmdhs12138.balance.core.model.BalanceUnit
import io.github.wmdhs12138.balance.core.net.UrlNormalizer
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
/** 渲染新增服务商面板 方法。 */
fun AddProviderSheet(
    strings: LocalizedStrings,
    onDismiss: () -> Unit,
    onResolveTitle: (String, (String) -> Unit) -> Unit,
    onSubmit: (String, String, String?, BalanceUnit) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var parserLabel by remember { mutableStateOf("NewAPI") }
    var balanceUnitOverride by remember { mutableStateOf(BalanceUnit.Auto) }
    var parserEdited by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }
    var resolvedTitle by remember { mutableStateOf("") }
    val displayName = customName.ifBlank { resolvedTitle }
    val normalizedUrl = UrlNormalizer.normalize(url)
    val suggestedParserLabel = remember(url) { ParserLabelSuggester.suggest(url) }
    val showUrlError = url.isNotBlank() && normalizedUrl == null

    LaunchedEffect(url) {
        resolvedTitle = ""
        val candidateUrl = url.trim()
        if (customName.isBlank() && normalizedUrl != null) {
            delay(700)
            if (customName.isBlank()) {
                onResolveTitle(candidateUrl) { title ->
                    resolvedTitle = title
                }
            }
        }
    }

    LaunchedEffect(suggestedParserLabel) {
        if (!parserEdited) {
            parserLabel = suggestedParserLabel
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(strings.get(R.string.add_provider_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = displayName,
                onValueChange = {
                    customName = it
                },
                label = { Text(strings.get(R.string.field_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text(strings.get(R.string.field_provider_url)) },
                isError = showUrlError,
                supportingText = {
                    when {
                        showUrlError -> Text(strings.get(R.string.field_provider_url_invalid))
                        normalizedUrl != null -> Text(strings.get(R.string.field_provider_url_normalized, normalizedUrl.origin))
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(strings.get(R.string.field_parser), style = MaterialTheme.typography.labelLarge)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                parserLabels.forEach { label ->
                    ParserFilterChip(
                        label = label,
                        selected = parserLabel == label,
                        recommended = label == suggestedParserLabel && normalizedUrl != null,
                        recommendedText = strings.get(R.string.parser_recommended),
                        onClick = {
                            parserEdited = true
                            parserLabel = label
                        },
                    )
                }
            }
            Text(strings.get(R.string.field_balance_unit), style = MaterialTheme.typography.labelLarge)
            BalanceUnitChips(
                selected = balanceUnitOverride,
                strings = strings,
                onSelected = { balanceUnitOverride = it },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) { Text(strings.get(R.string.action_cancel)) }
                Button(
                    enabled = normalizedUrl != null,
                    onClick = { onSubmit(displayName, normalizedUrl?.displayUrl.orEmpty(), parserLabel, balanceUnitOverride) },
                ) {
                    Text(strings.get(R.string.action_add))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
