package io.github.wmdhs12138.balance.feature.main

import io.github.wmdhs12138.balance.R
import io.github.wmdhs12138.balance.core.model.BalanceStatus
import io.github.wmdhs12138.balance.core.model.BalanceUnit
import io.github.wmdhs12138.balance.core.model.Provider
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/** 处理secondaryStatusLabel 方法。 */
fun Provider.secondaryStatusLabel(strings: LocalizedStrings): String? {
    return if (hasDisplayBalance && status != BalanceStatus.Ready) {
        lastErrorText?.friendlyErrorLabel(strings) ?: status.label(strings)
    } else {
        null
    }
}

val Provider.hasDisplayBalance: Boolean
    get() = !balanceText.isNullOrBlank()

/** 处理friendlyErrorLabel 方法。 */
fun String.friendlyErrorLabel(strings: LocalizedStrings): String {
    val normalized = lowercase(Locale.US)
    return when {
        "saved login data can no longer be decrypted" in normalized ||
            "401" in normalized ||
            "auth missing" in normalized ||
            "login expired" in normalized ||
            "login required" in normalized ||
            "token missing" in normalized ||
            "token not found" in normalized ||
            "api key required" in normalized ||
            "api key or login required" in normalized -> strings.get(R.string.status_login_required)
        "403" in normalized || "forbidden" in normalized -> strings.get(R.string.status_forbidden)
        "404" in normalized -> strings.get(R.string.status_interface_not_found)
        "timed out" in normalized ||
            "timeout" in normalized ||
            "read timed out" in normalized ||
            "connect timed out" in normalized -> strings.get(R.string.status_timeout)
        "returned html" in normalized ||
            "parser label" in normalized ||
            "parser mismatch" in normalized -> strings.get(R.string.status_parser_mismatch)
        "unknownhost" in normalized ||
            "unable to resolve host" in normalized ||
            "network" in normalized ||
            "ssl" in normalized -> strings.get(R.string.status_network_error)
        else -> strings.get(R.string.status_failed)
    }
}

/** 处理displayBalanceText 方法。 */
fun Provider.displayBalanceText(): String? {
    val text = balanceText ?: return null
    val displayUnit = if (balanceUnitOverride == BalanceUnit.Auto) balanceUnit else balanceUnitOverride
    return text.normalizeBalanceText(displayUnit)
}

/** 处理normalizeBalanceText 方法。 */
fun String.normalizeBalanceText(unit: BalanceUnit = BalanceUnit.Auto): String {
    val trimmed = trim()
    val numericValue = Regex("""[-+]?\d+(?:\.\d+)?""").find(trimmed.replace(",", ""))?.value?.toDoubleOrNull()
    return when (unit) {
        BalanceUnit.Cny -> numericValue?.let { String.format(Locale.US, "¥ %.2f", it) } ?: trimmed
        BalanceUnit.Usd -> numericValue?.let { String.format(Locale.US, "$ %.2f", it) } ?: trimmed
        BalanceUnit.Points -> numericValue?.formatFlexibleNumber()?.let { "$it 积分" } ?: trimmed
        BalanceUnit.Credits -> numericValue?.formatFlexibleNumber()?.let { "$it Credits" } ?: trimmed
        BalanceUnit.Quota -> numericValue?.formatFlexibleNumber()?.let { "$it 额度" } ?: trimmed
        BalanceUnit.Tokens -> numericValue?.formatFlexibleNumber()?.let { "$it Tokens" } ?: trimmed
        BalanceUnit.Auto,
        BalanceUnit.Unknown,
        -> {
            val match = Regex("""^([¥￥$])\s*([-+]?\d+(?:\.\d+)?)$""").matchEntire(trimmed)
                ?: return trimmed
            val symbol = if (match.groupValues[1] == "￥") "¥" else match.groupValues[1]
            val value = match.groupValues[2].toDoubleOrNull() ?: return trimmed
            String.format(Locale.US, "%s %.2f", symbol, value)
        }
    }
}

/** 处理formatFlexibleNumber 方法。 */
private fun Double.formatFlexibleNumber(): String {
    return if (this % 1.0 == 0.0) String.format(Locale.US, "%.0f", this) else String.format(Locale.US, "%.2f", this)
}

/** 处理localizedText 方法。 */
fun UiMessage.localizedText(strings: LocalizedStrings): String {
    return strings.get(resId, *args.toTypedArray())
}

/** 处理lastRefreshLabel 方法。 */
fun Provider.lastRefreshLabel(strings: LocalizedStrings): String {
    val successAt = lastSuccessAtMillis
    val attemptAt = lastAttemptAtMillis
    return when {
        status == BalanceStatus.Ready && successAt != null -> strings.get(R.string.last_success_at, successAt.formatDateTime())
        status != BalanceStatus.Ready && attemptAt != null -> strings.get(R.string.last_attempt_failed_at, attemptAt.formatDateTime())
        successAt != null -> strings.get(R.string.last_success_at, successAt.formatDateTime())
        else -> strings.get(R.string.last_refresh_never)
    }
}

/** 处理compactRefreshLabel 方法。 */
fun Provider.compactRefreshLabel(strings: LocalizedStrings): String {
    val timestamp = when {
        status == BalanceStatus.Ready -> lastSuccessAtMillis
        else -> lastAttemptAtMillis ?: lastSuccessAtMillis
    } ?: return strings.get(R.string.last_never_short)
    return DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(timestamp))
}

/** 处理formatDateTime 方法。 */
fun Long.formatDateTime(): String {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(this))
}
