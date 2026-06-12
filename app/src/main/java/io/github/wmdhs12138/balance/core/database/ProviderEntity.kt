package io.github.wmdhs12138.balance.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.wmdhs12138.balance.core.model.BalanceStatus
import io.github.wmdhs12138.balance.core.model.Provider

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val baseUrl: String,
    val loginUrl: String = baseUrl,
    val balanceEndpointHint: String? = null,
    val status: BalanceStatus = BalanceStatus.NotConnected,
    val balanceText: String? = null,
    val lastErrorText: String? = null,
    val encryptedLoginPayload: String? = null,
    val lastSuccessAtMillis: Long? = null,
    val lastAttemptAtMillis: Long? = null,
) {
    fun toModel(): Provider = Provider(
        id = id,
        name = name,
        baseUrl = baseUrl,
        loginUrl = loginUrl,
        balanceEndpointHint = balanceEndpointHint,
        status = status,
        balanceText = balanceText,
        lastErrorText = lastErrorText,
        lastSuccessAtMillis = lastSuccessAtMillis,
        lastAttemptAtMillis = lastAttemptAtMillis,
    )
}
