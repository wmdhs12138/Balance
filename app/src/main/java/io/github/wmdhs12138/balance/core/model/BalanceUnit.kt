package io.github.wmdhs12138.balance.core.model

/** BalanceUnit 枚举。 */
enum class BalanceUnit(val storageValue: String) {
    Auto("auto"),
    Cny("cny"),
    Usd("usd"),
    Points("points"),
    Credits("credits"),
    Quota("quota"),
    Tokens("tokens"),
    Unknown("unknown"),
    ;

    companion object {
        /** 从Storage创建结果 方法。 */
        fun fromStorage(value: String?): BalanceUnit {
            return entries.firstOrNull { it.storageValue.equals(value, ignoreCase = true) } ?: Auto
        }

        /** 处理inferredFromKey 方法。 */
        fun inferredFromKey(key: String?): BalanceUnit {
            val normalized = key.orEmpty().lowercase()
            return when {
                normalized.contains("point") || normalized.contains("score") || normalized.contains("积分") -> Points
                normalized.contains("credit") -> Credits
                normalized.contains("token") -> Tokens
                normalized.contains("quota") -> Quota
                normalized.contains("money") || normalized.contains("amount") || normalized.contains("balance") -> Usd
                else -> Unknown
            }
        }
    }
}
