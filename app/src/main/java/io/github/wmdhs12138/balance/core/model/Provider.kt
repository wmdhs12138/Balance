package io.github.wmdhs12138.balance.core.model

data class Provider(
    val id: Long,
    val name: String,
    val baseUrl: String,
    val loginUrl: String,
    val balanceEndpointHint: String?,
    val status: BalanceStatus,
    val balanceText: String?,
    val lastErrorText: String?,
    val lastSuccessAtMillis: Long?,
    val lastAttemptAtMillis: Long?,
)
