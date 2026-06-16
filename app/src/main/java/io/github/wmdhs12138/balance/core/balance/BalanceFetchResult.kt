package io.github.wmdhs12138.balance.core.balance

import io.github.wmdhs12138.balance.core.model.BalanceUnit

/** BalanceFetchResult 数据结构。 */
data class BalanceFetchResult(
    val balanceText: String,
    val balanceUnit: BalanceUnit = BalanceUnit.Unknown,
)
