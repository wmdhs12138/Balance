package io.github.wmdhs12138.balance.core.balance

/** 余额解析器标签定义。 */
object BalanceParserLabels {
    /** NewAPI 解析器标签。 */
    const val NEW_API = "NewAPI"
    /** Sub2API 解析器标签。 */
    const val SUB2_API = "Sub2API"
    /** DeepSeek 解析器标签。 */
    const val DEEP_SEEK = "DeepSeek"

    /** 当前支持的解析器标签列表。 */
    val supported = listOf(NEW_API, SUB2_API, DEEP_SEEK)

    /** 将空值或未知标签回退到默认解析器。 */
    fun normalize(label: String?): String = when {
        label.equals(DEEP_SEEK, ignoreCase = true) -> DEEP_SEEK
        label.equals(SUB2_API, ignoreCase = true) -> SUB2_API
        label.equals(NEW_API, ignoreCase = true) -> NEW_API
        else -> NEW_API
    }

    /** 判断解析器是否使用 API Key 登录。 */
    fun usesApiKey(label: String?): Boolean = normalize(label) == DEEP_SEEK
}
