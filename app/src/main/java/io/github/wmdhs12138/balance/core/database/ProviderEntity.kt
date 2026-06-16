package io.github.wmdhs12138.balance.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.wmdhs12138.balance.core.model.BalanceStatus
import io.github.wmdhs12138.balance.core.model.BalanceUnit
import io.github.wmdhs12138.balance.core.model.Provider

@Entity(tableName = "providers")
/** ProviderEntity 数据结构。 */
data class ProviderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val baseUrl: String,
    val loginUrl: String = baseUrl,
    val balanceEndpointHint: String? = null,
    val status: BalanceStatus = BalanceStatus.NotConnected,
    val balanceText: String? = null,
    val balanceUnit: BalanceUnit = BalanceUnit.Unknown,
    val balanceUnitOverride: BalanceUnit = BalanceUnit.Auto,
    val lastErrorText: String? = null,
    val encryptedLoginPayload: String? = null,
    val lastSuccessAtMillis: Long? = null,
    val lastAttemptAtMillis: Long? = null,
) {
    /** 转换为Model结果 方法。 */
    fun toModel(): Provider = Provider(
        id = id,
        name = name,
        baseUrl = baseUrl,
        loginUrl = loginUrl,
        balanceEndpointHint = balanceEndpointHint,
        status = status,
        balanceText = balanceText,
        balanceUnit = balanceUnit,
        balanceUnitOverride = balanceUnitOverride,
        lastErrorText = lastErrorText,
        lastSuccessAtMillis = lastSuccessAtMillis,
        lastAttemptAtMillis = lastAttemptAtMillis,
    )
}
