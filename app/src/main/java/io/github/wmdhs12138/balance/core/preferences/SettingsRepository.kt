package io.github.wmdhs12138.balance.core.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.wmdhs12138.balance.core.model.AppLanguage
import io.github.wmdhs12138.balance.core.model.ThemeMode
import io.github.wmdhs12138.balance.ui.locale.localized
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            themeMode = prefs[THEME_MODE]?.let(ThemeMode::valueOf) ?: ThemeMode.System,
            dynamicColor = prefs[DYNAMIC_COLOR] ?: true,
            seedColor = prefs[SEED_COLOR] ?: DEFAULT_SEED_COLOR,
            language = AppLanguage.fromPreference(prefs[LANGUAGE]),
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[THEME_MODE] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[DYNAMIC_COLOR] = enabled }
    }

    suspend fun setSeedColor(color: Long) {
        context.dataStore.edit { it[SEED_COLOR] = color }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[LANGUAGE] = language.preferenceValue }
    }

    suspend fun localizedString(resId: Int, vararg args: Any): String {
        val language = preferences.first().language
        val localizedContext = context.localized(language)
        return localizedContext.getString(resId, *args)
    }

    private companion object {
        const val DEFAULT_SEED_COLOR = 0xFF006C4FL
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val SEED_COLOR = longPreferencesKey("seed_color")
        val LANGUAGE = stringPreferencesKey("language")
    }
}
