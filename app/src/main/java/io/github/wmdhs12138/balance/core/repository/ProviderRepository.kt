package io.github.wmdhs12138.balance.core.repository

import io.github.wmdhs12138.balance.core.balance.BalanceFetchException
import io.github.wmdhs12138.balance.core.balance.BalanceFetchFailureReason
import io.github.wmdhs12138.balance.core.balance.BalanceFetcherRegistry
import io.github.wmdhs12138.balance.core.balance.apiKeyPayload
import io.github.wmdhs12138.balance.core.crypto.LoginDataCipher
import io.github.wmdhs12138.balance.core.database.ProviderDao
import io.github.wmdhs12138.balance.core.database.ProviderEntity
import io.github.wmdhs12138.balance.core.model.BalanceStatus
import io.github.wmdhs12138.balance.core.model.BalanceUnit
import io.github.wmdhs12138.balance.core.model.Provider
import io.github.wmdhs12138.balance.core.net.UrlNormalizer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/** ProviderRepository 类。 */
class ProviderRepository(
    private val providerDao: ProviderDao,
    private val loginDataCipher: LoginDataCipher,
    private val balanceFetcherRegistry: BalanceFetcherRegistry,
) {
    val providers: Flow<List<Provider>> = providerDao.observeProviders()
        .map { providers -> providers.map(ProviderEntity::toModel) }

    /** 添加自定义服务商 方法。 */
    suspend fun addCustomProvider(name: String, baseUrl: String, parserLabel: String?, balanceUnitOverride: BalanceUnit): AddProviderResult {
        val normalizedUrl = UrlNormalizer.normalize(baseUrl)
            ?: return AddProviderResult.InvalidUrl
        if (providerDao.getProviderByBaseUrl(normalizedUrl.origin) != null) {
            return AddProviderResult.AlreadyExists
        }
        providerDao.insert(
            ProviderEntity(
                name = name.trim(),
                baseUrl = normalizedUrl.origin,
                loginUrl = normalizedUrl.displayUrl,
                balanceEndpointHint = parserLabel?.takeIf { it.isNotBlank() },
                sortOrder = providerDao.getMaxSortOrder() + 1,
                balanceUnitOverride = balanceUnitOverride,
                status = BalanceStatus.NotConnected,
            ),
        )
        return AddProviderResult.Added
    }

    /** 保存加密登录数据 方法。 */
    suspend fun storeEncryptedLogin(provider: ProviderEntity, loginPayload: String) {
        providerDao.update(provider.copy(encryptedLoginPayload = loginDataCipher.encrypt(loginPayload)))
    }

    /** 保存加密登录数据 方法。 */
    suspend fun storeEncryptedLogin(providerId: Long, loginPayload: String) {
        providerDao.updateEncryptedLogin(
            id = providerId,
            encryptedLoginPayload = loginDataCipher.encrypt(loginPayload),
            status = BalanceStatus.Ready,
            lastAttemptAtMillis = System.currentTimeMillis(),
        )
    }

    /** 保存 API Key 方法。 */
    suspend fun storeApiKey(providerId: Long, apiKey: String) {
        providerDao.updateEncryptedLogin(
            id = providerId,
            encryptedLoginPayload = loginDataCipher.encrypt(apiKeyPayload(apiKey.trim())),
            status = BalanceStatus.Ready,
            lastAttemptAtMillis = System.currentTimeMillis(),
        )
    }

    /** 更新解析器标签 方法。 */
    suspend fun updateParserLabel(providerId: Long, parserLabel: String?) {
        providerDao.updateParserLabel(
            id = providerId,
            parserLabel = parserLabel?.takeIf { it.isNotBlank() },
            lastAttemptAtMillis = System.currentTimeMillis(),
        )
    }

    /** 更新服务商设置 方法。 */
    suspend fun updateProviderSettings(providerId: Long, name: String, parserLabel: String?, balanceUnitOverride: BalanceUnit) {
        providerDao.updateProviderSettings(
            id = providerId,
            name = name.trim(),
            parserLabel = parserLabel?.takeIf { it.isNotBlank() },
            balanceUnitOverride = balanceUnitOverride,
            lastAttemptAtMillis = System.currentTimeMillis(),
        )
    }

    /** 按给定 ID 顺序保存供应商排序。 */
    suspend fun updateProviderOrder(providerIds: List<Long>) {
        providerIds.forEachIndexed { index, id ->
            providerDao.updateSortOrder(id, index)
        }
    }

    /** 删除服务商 方法。 */
    suspend fun deleteProvider(providerId: Long) {
        providerDao.deleteProvider(providerId)
    }

    /** 删除全部服务商 方法。 */
    suspend fun deleteAllProviders() {
        providerDao.deleteAllProviders()
    }

    /** 刷新单个服务商余额 方法。 */
    suspend fun refreshProvider(providerId: Long): Boolean {
        val provider = providerDao.getProvider(providerId) ?: return false
        return refreshProvider(provider).isSuccess
    }

    /** 刷新全部服务商余额 方法。 */
    suspend fun refreshBalances(): RefreshSummary {
        val providers = providerDao.getProviders()
        val refreshResults = coroutineScope {
            val semaphore = Semaphore(MAX_CONCURRENT_REFRESHES)
            providers.map { provider ->
                async {
                    semaphore.withPermit {
                        refreshProvider(provider)
                    }
                }
            }.awaitAll()
        }
        return RefreshSummary(
            ready = refreshResults.count { it == RefreshResult.Ready },
            needsLogin = refreshResults.count { it == RefreshResult.NeedsLogin },
            failed = refreshResults.count { it == RefreshResult.Failed },
        )
    }

    /** 处理decryptLoginPayload 方法。 */
    private suspend fun decryptLoginPayload(provider: ProviderEntity): String? {
        val encrypted = provider.encryptedLoginPayload ?: return null
        return runCatching { loginDataCipher.decrypt(encrypted) }
            .getOrElse {
                throw BalanceFetchException(
                    message = "Saved login data can no longer be decrypted. Please login again.",
                    reason = BalanceFetchFailureReason.NeedsLogin,
                    cause = it,
                )
            }
    }

    /** 刷新单个服务商余额 方法。 */
    private suspend fun refreshProvider(provider: ProviderEntity): RefreshResult {
        val timestamp = System.currentTimeMillis()
        return runCatching {
            val encryptedLoginPayload = decryptLoginPayload(provider)
            balanceFetcherRegistry.forProvider(provider).fetch(provider, encryptedLoginPayload)
        }.onSuccess { result ->
            providerDao.updateBalanceSuccess(
                id = provider.id,
                status = BalanceStatus.Ready,
                balanceText = result.balanceText,
                balanceUnit = result.balanceUnit,
                timestamp = timestamp,
            )
        }.onFailure { throwable ->
            val status = throwable.balanceStatus()
            providerDao.updateBalanceFailure(
                id = provider.id,
                status = status,
                lastErrorText = throwable.message ?: "Refresh failed",
                lastAttemptAtMillis = timestamp,
            )
        }.fold(
            onSuccess = { RefreshResult.Ready },
            onFailure = { throwable ->
                if (throwable.balanceStatus() == BalanceStatus.NeedsLogin) RefreshResult.NeedsLogin else RefreshResult.Failed
            },
        )
    }

    /** 处理balanceStatus 方法。 */
    private fun Throwable.balanceStatus(): BalanceStatus {
        return when (if (this is BalanceFetchException) reason else BalanceFetchFailureReason.Unknown) {
            BalanceFetchFailureReason.NeedsLogin -> BalanceStatus.NeedsLogin
            BalanceFetchFailureReason.Forbidden -> BalanceStatus.Forbidden
            BalanceFetchFailureReason.NotFound -> BalanceStatus.NotFound
            BalanceFetchFailureReason.Timeout -> BalanceStatus.Timeout
            BalanceFetchFailureReason.Network -> BalanceStatus.NetworkError
            BalanceFetchFailureReason.ParserMismatch -> BalanceStatus.ParserMismatch
            BalanceFetchFailureReason.InvalidResponse,
            BalanceFetchFailureReason.Unsupported,
            BalanceFetchFailureReason.Unknown,
            -> BalanceStatus.Failed
        }
    }

    private companion object {
        /** 处理MAX_CONCURRENT_REFRESHES 常量。 */
        const val MAX_CONCURRENT_REFRESHES = 3
    }
}

/** AddProviderResult 枚举。 */
enum class AddProviderResult {
    Added,
    AlreadyExists,
    InvalidUrl,
}

/** RefreshSummary 数据结构。 */
data class RefreshSummary(
    val ready: Int,
    val needsLogin: Int,
    val failed: Int,
)

private enum class RefreshResult {
    Ready,
    NeedsLogin,
    Failed,
}

private val RefreshResult.isSuccess: Boolean
    get() = this == RefreshResult.Ready
