package io.github.wmdhs12138.balance.core.balance

import io.github.wmdhs12138.balance.core.net.UrlNormalizer

object ParserLabelSuggester {
    const val DEFAULT = "NewAPI"
    const val SUB2API = "Sub2API"
    const val ACCOUNT_SUMMARY = "Account Summary"
    const val DEEPSEEK = "DeepSeek"

    val labels = listOf(DEFAULT, SUB2API, ACCOUNT_SUMMARY, DEEPSEEK)

    fun suggest(rawUrl: String): String {
        val normalized = UrlNormalizer.normalize(rawUrl) ?: return DEFAULT
        val host = normalized.host
        return when {
            host == "platform.deepseek.com" || host.endsWith(".deepseek.com") -> DEEPSEEK
            host.contains("sub2", ignoreCase = true) -> SUB2API
            host.contains("account", ignoreCase = true) || host.contains("summary", ignoreCase = true) -> ACCOUNT_SUMMARY
            else -> DEFAULT
        }
    }
}
