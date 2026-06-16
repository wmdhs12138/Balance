package io.github.wmdhs12138.balance.core.preferences

import io.github.wmdhs12138.balance.core.model.AppLanguage
import io.github.wmdhs12138.balance.core.model.ThemeMode

/** UserPreferences 数据结构。 */
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.System,
    val dynamicColor: Boolean = true,
    val seedColor: Long = 0xFF006C4FL,
    val language: AppLanguage = AppLanguage.System,
    val pullRefreshHintShown: Boolean = false,
)
