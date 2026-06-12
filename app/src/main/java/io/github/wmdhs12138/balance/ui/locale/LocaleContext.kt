package io.github.wmdhs12138.balance.ui.locale

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import io.github.wmdhs12138.balance.core.model.AppLanguage
import java.util.Locale

@Composable
fun ProvideLocalizedContext(
    language: AppLanguage,
    content: @Composable () -> Unit,
) {
    val baseContext = LocalContext.current
    val configuration = LocalConfiguration.current
    val localizedContext = remember(baseContext, configuration, language) {
        baseContext.localized(language)
    }

    CompositionLocalProvider(LocalContext provides localizedContext) {
        content()
    }
}

fun Context.localized(language: AppLanguage): Context {
    val locale = language.languageTag
        ?.let(Locale::forLanguageTag)
        ?: resources.configuration.locales[0]
        ?: LocaleList.getDefault()[0]
        ?: Locale.getDefault()
    val config = Configuration(resources.configuration)
    Locale.setDefault(locale)
    config.setLocale(locale)
    config.setLayoutDirection(locale)
    return createConfigurationContext(config)
}
