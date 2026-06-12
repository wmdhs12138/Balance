package io.github.wmdhs12138.balance.feature.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.wmdhs12138.balance.BuildConfig
import io.github.wmdhs12138.balance.R
import io.github.wmdhs12138.balance.core.model.AppLanguage
import io.github.wmdhs12138.balance.core.model.ThemeMode
import io.github.wmdhs12138.balance.ui.theme.AmberSeed
import io.github.wmdhs12138.balance.ui.theme.BlueSeed
import io.github.wmdhs12138.balance.ui.theme.EmeraldSeed
import io.github.wmdhs12138.balance.ui.theme.RoseSeed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    uiState: MainUiState,
    strings: LocalizedStrings,
    onDismiss: () -> Unit,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onSeedColorChanged: (Long) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    onClearLocalData: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showClearConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(strings.get(R.string.settings_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }
            item {
                ThemePanel(
                    strings = strings,
                    themeMode = uiState.themeMode,
                    dynamicColor = uiState.dynamicColor,
                    seedColor = uiState.seedColor,
                    onThemeModeChanged = onThemeModeChanged,
                    onDynamicColorChanged = onDynamicColorChanged,
                    onSeedColorChanged = onSeedColorChanged,
                )
            }
            item {
                LanguagePanel(
                    strings = strings,
                    language = uiState.language,
                    onLanguageChanged = onLanguageChanged,
                )
            }
            item {
                LocalDataSection(
                    strings = strings,
                    onClearLocalData = { showClearConfirm = true },
                )
            }
            item {
                AppInfoSection(strings)
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(strings.get(R.string.clear_local_data_title)) },
            text = { Text(strings.get(R.string.clear_local_data_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirm = false
                        onClearLocalData()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(strings.get(R.string.action_clear))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text(strings.get(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun ThemePanel(
    strings: LocalizedStrings,
    themeMode: ThemeMode,
    dynamicColor: Boolean,
    seedColor: Long,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onSeedColorChanged: (Long) -> Unit,
) {
    SettingsSection(title = strings.get(R.string.settings_appearance)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = themeMode == mode,
                        onClick = { onThemeModeChanged(mode) },
                        label = { Text(mode.displayName(strings)) },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(strings.get(R.string.settings_dynamic_color))
                Switch(checked = dynamicColor, onCheckedChange = onDynamicColorChanged)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(EmeraldSeed, BlueSeed, RoseSeed, AmberSeed).forEach { color ->
                    ColorSwatch(
                        color = color,
                        selected = seedColor.normalizedSeedPreference() == color.preferenceValue(),
                        onClick = { onSeedColorChanged(color.preferenceValue()) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LanguagePanel(
    strings: LocalizedStrings,
    language: AppLanguage,
    onLanguageChanged: (AppLanguage) -> Unit,
) {
    SettingsSection(title = strings.get(R.string.settings_language)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                AppLanguage.entries.forEach { option ->
                    FilterChip(
                        selected = language == option,
                        onClick = { onLanguageChanged(option) },
                        label = { Text(option.displayName(strings)) },
                    )
                }
            }
        }
    }
}

private fun Color.preferenceValue(): Long = toArgb().toLong() and 0xFFFFFFFFL

private fun Long.normalizedSeedPreference(): Long {
    val legacyPackedValues = mapOf(
        EmeraldSeed.value.toLong() to EmeraldSeed.preferenceValue(),
        BlueSeed.value.toLong() to BlueSeed.preferenceValue(),
        RoseSeed.value.toLong() to RoseSeed.preferenceValue(),
        AmberSeed.value.toLong() to AmberSeed.preferenceValue(),
    )
    return legacyPackedValues[this] ?: (this and 0xFFFFFFFFL)
}

@Composable
private fun AppInfoSection(strings: LocalizedStrings) {
    SettingsSection(title = strings.get(R.string.settings_app_information)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AppInfoRow(strings.get(R.string.app_info_name), strings.get(R.string.app_name))
            AppInfoRow(strings.get(R.string.app_info_version), BuildConfig.VERSION_NAME)
            AppInfoRow(strings.get(R.string.app_info_license), "MIT")
            AppInfoRow(strings.get(R.string.app_info_storage), strings.get(R.string.app_info_storage_value))
            AppInfoRow("POWERBY", "VIBE CODING")
            Text(
                text = strings.get(R.string.app_description),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun LocalDataSection(
    strings: LocalizedStrings,
    onClearLocalData: () -> Unit,
) {
    SettingsSection(title = strings.get(R.string.settings_local_data)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = strings.get(R.string.local_data_description),
                style = MaterialTheme.typography.bodySmall,
            )
            TextButton(
                onClick = onClearLocalData,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text(strings.get(R.string.action_clear_local_data))
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
fun AppInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = color,
        contentColor = Color.White,
        tonalElevation = if (selected) 6.dp else 0.dp,
        modifier = Modifier
            .width(36.dp)
            .height(36.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (selected) Text("✓", fontWeight = FontWeight.Bold)
        }
    }
}
