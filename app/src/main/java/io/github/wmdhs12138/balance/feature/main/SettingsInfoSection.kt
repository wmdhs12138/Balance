package io.github.wmdhs12138.balance.feature.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.wmdhs12138.balance.BuildConfig
import io.github.wmdhs12138.balance.R

@Composable
/** 处理AppInfoSection 方法。 */
fun AppInfoSection(strings: LocalizedStrings) {
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
/** 处理LocalDataSection 方法。 */
fun LocalDataSection(
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
