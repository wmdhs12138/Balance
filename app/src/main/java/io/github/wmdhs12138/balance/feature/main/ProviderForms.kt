package io.github.wmdhs12138.balance.feature.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.wmdhs12138.balance.R
import io.github.wmdhs12138.balance.core.balance.ParserLabelSuggester
import io.github.wmdhs12138.balance.core.model.BalanceStatus
import io.github.wmdhs12138.balance.core.model.BalanceUnit
import io.github.wmdhs12138.balance.core.model.Provider
import io.github.wmdhs12138.balance.core.net.UrlNormalizer
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
/** 渲染服务商设置面板 方法。 */
fun ProviderSettingsSheet(
    provider: Provider,
    strings: LocalizedStrings,
    onDismiss: () -> Unit,
    onLogin: () -> Unit,
    onApiKey: () -> Unit,
    onSubmit: (String, String?, BalanceUnit) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(provider.name) }
    var parserLabel by remember { mutableStateOf(provider.parserLabelForForms) }
    var balanceUnitOverride by remember { mutableStateOf(provider.balanceUnitOverride) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val credentialActionText = strings.get(if (provider.usesApiKey) R.string.action_api_key else R.string.action_login)
    val credentialActionIcon = if (provider.usesApiKey) Icons.Default.Key else Icons.AutoMirrored.Filled.Login

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = provider.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = provider.baseUrl,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            SettingsSection(title = strings.get(R.string.provider_section_connection)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppInfoRow(strings.get(R.string.field_parser), provider.parserLabelForCard)
                    Button(
                        onClick = if (provider.usesApiKey) onApiKey else onLogin,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(imageVector = credentialActionIcon, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(credentialActionText)
                    }
                }
            }

            SettingsSection(title = strings.get(R.string.provider_section_edit)) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(strings.get(R.string.field_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            strings.get(R.string.field_parser),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            parserLabels.forEach { label ->
                                ParserFilterChip(
                                    label = label,
                                    selected = parserLabel == label,
                                    onClick = { parserLabel = label },
                                )
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            strings.get(R.string.field_balance_unit),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        BalanceUnitChips(
                            selected = balanceUnitOverride,
                            strings = strings,
                            onSelected = { balanceUnitOverride = it },
                        )
                    }
                }
            }

            SettingsSection(title = strings.get(R.string.provider_section_status)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppInfoRow(
                        strings.get(R.string.status_ready),
                        provider.status.label(strings),
                        allowValueWrap = true,
                    )
                    AppInfoRow(
                        strings.get(R.string.provider_last_success),
                        provider.lastSuccessAtMillis?.formatDateTime() ?: strings.get(R.string.last_refresh_never),
                        allowValueWrap = true,
                    )
                    AppInfoRow(
                        strings.get(R.string.provider_last_attempt),
                        provider.lastAttemptAtMillis?.formatDateTime() ?: strings.get(R.string.last_refresh_never),
                        allowValueWrap = true,
                    )
                    provider.lastErrorText?.takeIf { provider.status != BalanceStatus.Ready }?.let { details ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                strings.get(R.string.provider_error_details),
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Text(
                                details,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 8,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onDelete != null) {
                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) {
                        Text(strings.get(R.string.action_delete))
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) { Text(strings.get(R.string.action_cancel)) }
                    Button(
                        enabled = name.isNotBlank(),
                        onClick = { onSubmit(name, parserLabel, balanceUnitOverride) },
                    ) {
                        Text(strings.get(R.string.action_save))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(strings.get(R.string.delete_provider_title)) },
            text = { Text(strings.get(R.string.delete_provider_message, provider.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(strings.get(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(strings.get(R.string.action_cancel)) }
            },
        )
    }
}
