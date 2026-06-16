package io.github.wmdhs12138.balance.feature.main

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.wmdhs12138.balance.R
import io.github.wmdhs12138.balance.core.model.AppLanguage
import io.github.wmdhs12138.balance.core.model.BalanceStatus
import io.github.wmdhs12138.balance.core.model.BalanceUnit
import io.github.wmdhs12138.balance.core.model.ThemeMode
import io.github.wmdhs12138.balance.ui.locale.localized

@Composable
/** 记住本地化字符串访问器 方法。 */
fun rememberLocalizedStrings(language: AppLanguage): LocalizedStrings {
    val context = LocalContext.current
    val localizedContext = remember(context, language) {
        context.localized(language)
    }
    return remember(localizedContext) {
        LocalizedStrings(localizedContext)
    }
}

/** LocalizedStrings 类。 */
class LocalizedStrings(
    private val context: Context,
) {
    /** 处理get 方法。 */
    fun get(@StringRes resId: Int, vararg args: Any): String {
        return if (args.isEmpty()) {
            context.getString(resId)
        } else {
            context.getString(resId, *args)
        }
    }
}

/** 转换为显示标签 方法。 */
fun BalanceStatus.label(strings: LocalizedStrings): String {
    return when (this) {
        BalanceStatus.NotConnected -> strings.get(R.string.status_not_connected)
        BalanceStatus.NeedsLogin -> strings.get(R.string.status_login_required)
        BalanceStatus.Ready -> strings.get(R.string.status_ready)
        BalanceStatus.Forbidden -> strings.get(R.string.status_forbidden)
        BalanceStatus.NotFound -> strings.get(R.string.status_interface_not_found)
        BalanceStatus.Timeout -> strings.get(R.string.status_timeout)
        BalanceStatus.NetworkError -> strings.get(R.string.status_network_error)
        BalanceStatus.ParserMismatch -> strings.get(R.string.status_parser_mismatch)
        BalanceStatus.Failed -> strings.get(R.string.status_failed)
    }
}

/** 转换为显示名称 方法。 */
fun ThemeMode.displayName(strings: LocalizedStrings): String {
    return when (this) {
        ThemeMode.System -> strings.get(R.string.theme_system)
        ThemeMode.Light -> strings.get(R.string.theme_light)
        ThemeMode.Dark -> strings.get(R.string.theme_dark)
    }
}

/** 转换为显示名称 方法。 */
fun AppLanguage.displayName(strings: LocalizedStrings): String {
    return when (this) {
        AppLanguage.System -> strings.get(R.string.language_system)
        AppLanguage.English -> strings.get(R.string.language_english)
        AppLanguage.ChineseSimplified -> strings.get(R.string.language_chinese_simplified)
        AppLanguage.Russian -> strings.get(R.string.language_russian)
        AppLanguage.French -> strings.get(R.string.language_french)
        AppLanguage.German -> strings.get(R.string.language_german)
        AppLanguage.Japanese -> strings.get(R.string.language_japanese)
    }
}

/** 转换为显示名称 方法。 */
fun BalanceUnit.displayName(strings: LocalizedStrings): String {
    return when (this) {
        BalanceUnit.Auto -> strings.get(R.string.balance_unit_auto)
        BalanceUnit.Cny -> strings.get(R.string.balance_unit_cny)
        BalanceUnit.Usd -> strings.get(R.string.balance_unit_usd)
        BalanceUnit.Points -> strings.get(R.string.balance_unit_points)
        BalanceUnit.Credits -> strings.get(R.string.balance_unit_credits)
        BalanceUnit.Quota -> strings.get(R.string.balance_unit_quota)
        BalanceUnit.Tokens -> strings.get(R.string.balance_unit_tokens)
        BalanceUnit.Unknown -> strings.get(R.string.balance_unit_unknown)
    }
}
