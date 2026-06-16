package io.github.wmdhs12138.balance.feature.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import io.github.wmdhs12138.balance.R
import io.github.wmdhs12138.balance.core.model.AppLanguage
import io.github.wmdhs12138.balance.core.model.ThemeMode
import io.github.wmdhs12138.balance.ui.theme.AmberSeed
import io.github.wmdhs12138.balance.ui.theme.BlueSeed
import io.github.wmdhs12138.balance.ui.theme.EmeraldSeed
import io.github.wmdhs12138.balance.ui.theme.RoseSeed

@Composable
/** 处理ThemePanel 方法。 */
fun ThemePanel(
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
/** 处理LanguagePanel 方法。 */
fun LanguagePanel(
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

/** 处理preferenceValue 方法。 */
fun Color.preferenceValue(): Long = toArgb().toLong() and 0xFFFFFFFFL

/** 将颜色偏好值限制为 ARGB 无符号范围。 */
fun Long.normalizedSeedPreference(): Long = this and 0xFFFFFFFFL
