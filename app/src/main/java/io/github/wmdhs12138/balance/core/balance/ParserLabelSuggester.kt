package io.github.wmdhs12138.balance.core.balance

import io.github.wmdhs12138.balance.core.net.UrlNormalizer

/** ParserLabelSuggester 单例对象。 */
object ParserLabelSuggester {
    /** 处理DEFAULT 常量。 */
    const val DEFAULT = "NewAPI"
    /** 处理SUB2API 常量。 */
    const val SUB2API = "Sub2API"
    /** 处理DEEPSEEK 常量。 */
    const val DEEPSEEK = "DeepSeek"

    val labels = listOf(DEFAULT, SUB2API, DEEPSEEK)

    /** 处理suggest 方法。 */
    fun suggest(rawUrl: String): String {
        val normalized = UrlNormalizer.normalize(rawUrl) ?: return DEFAULT
        val host = normalized.host
        return when {
            host == "platform.deepseek.com" || host.endsWith(".deepseek.com") -> DEEPSEEK
            host.contains("sub2", ignoreCase = true) -> SUB2API
            else -> DEFAULT
        }
    }
}
