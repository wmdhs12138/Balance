package io.github.wmdhs12138.balance.core.repository

import io.github.wmdhs12138.balance.core.balance.BalanceFetchException
import io.github.wmdhs12138.balance.core.balance.BalanceFetchFailureReason
import io.github.wmdhs12138.balance.core.balance.BalanceFetcherRegistry
import io.github.wmdhs12138.balance.core.balance.apiKeyPayload
import io.github.wmdhs12138.balance.core.crypto.LoginDataCipher
import io.github.wmdhs12138.balance.core.database.ProviderDao
import io.github.wmdhs12138.balance.core.database.ProviderEntity
import io.github.wmdhs12138.balance.core.model.BalanceStatus
import io.github.wmdhs12138.balance.core.model.Provider
import io.github.wmdhs12138.balance.core.net.UrlNormalizer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class ProviderRepository(
    private val providerDao: ProviderDao,
    private val loginDataCipher: LoginDataCipher,
    private val balanceFetcherRegistry: BalanceFetcherRegistry,
) {
    val providers: Flow<List<Provider>> = providerDao.observeProviders()
        .map { providers -> providers.map(ProviderEntity::toModel) }

    suspend fun normalizeLegacyData() {
        providerDao.normalizeNewApiParserLabel()
    }

    suspend fun addCustomProvider(name: String, baseUrl: String, parserLabel: String?): AddProviderResult {
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
                status = BalanceStatus.NotConnected,
            ),
        )
        return AddProviderResult.Added
    }

    suspend fun storeEncryptedLogin(provider: ProviderEntity, loginPayload: String) {
        providerDao.update(provider.copy(encryptedLoginPayload = loginDataCipher.encrypt(loginPayload)))
    }

    suspend fun storeEncryptedLogin(providerId: Long, loginPayload: String) {
        providerDao.updateEncryptedLogin(
            id = providerId,
            encryptedLoginPayload = loginDataCipher.encrypt(loginPayload),
            status = BalanceStatus.Ready,
            lastAttemptAtMillis = System.currentTimeMillis(),
        )
    }

    suspend fun storeApiKey(providerId: Long, apiKey: String) {
        providerDao.updateEncryptedLogin(
            id = providerId,
            encryptedLoginPayload = loginDataCipher.encrypt(apiKeyPayload(apiKey.trim())),
            status = BalanceStatus.Ready,
            lastAttemptAtMillis = System.currentTimeMillis(),
        )
    }

    suspend fun updateParserLabel(providerId: Long, parserLabel: String?) {
        providerDao.updateParserLabel(
            id = providerId,
            parserLabel = parserLabel?.takeIf { it.isNotBlank() },
            lastAttemptAtMillis = System.currentTimeMillis(),
        )
    }

    suspend fun updateProviderSettings(providerId: Long, name: String, parserLabel: String?) {
        providerDao.updateProviderSettings(
            id = providerId,
            name = name.trim(),
            parserLabel = parserLabel?.takeIf { it.isNotBlank() },
            lastAttemptAtMillis = System.currentTimeMillis(),
        )
    }

    suspend fun deleteProvider(providerId: Long) {
        providerDao.deleteProvider(providerId)
    }

    suspend fun deleteAllProviders() {
        providerDao.deleteAllProviders()
    }

    suspend fun refreshProvider(providerId: Long): Boolean {
        val provider = providerDao.getProvider(providerId) ?: return false
        return refreshProvider(provider).isSuccess
    }

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
        const val MAX_CONCURRENT_REFRESHES = 3
    }
}

enum class AddProviderResult {
    Added,
    AlreadyExists,
    InvalidUrl,
}

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
