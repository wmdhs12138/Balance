package io.github.wmdhs12138.balance.core.balance

import java.util.Locale

/** 余额文本格式化工具。 */
object BalanceFormatters {
    /** 格式化美元余额。 */
    fun usd(value: Double): String = String.format(Locale.US, "$ %.2f", value)

    /** 格式化人民币余额。 */
    fun cny(value: Double): String = String.format(Locale.CHINA, "¥ %.2f", value)

    /** 格式化普通数值余额。 */
    fun number(value: Double): String = String.format(Locale.US, "%.2f", value)
}
