package io.github.wmdhs12138.balance.core.balance

import io.github.wmdhs12138.balance.core.database.ProviderEntity
import io.github.wmdhs12138.balance.core.model.BalanceUnit
import io.github.wmdhs12138.balance.core.net.UrlNormalizer

/** NewApiBalanceFetcher 类。 */
class NewApiBalanceFetcher(
    private val httpClient: BalanceHttpClient,
) : BalanceFetcher {
    /** 获取余额数据 方法。 */
    override suspend fun fetch(provider: ProviderEntity, encryptedLoginPayload: String?): BalanceFetchResult {
        val payload = parseLoginPayload(encryptedLoginPayload) as? LoginPayload.WebSession
            ?: throw BalanceFetchException("Login required", BalanceFetchFailureReason.NeedsLogin)

        var lastError: BalanceFetchException? = null
        SELF_ENDPOINTS.forEach { path ->
            val response = httpClient.get(
                url = UrlNormalizer.endpoint(provider.baseUrl, path),
                headers = BalanceHeaders.webSession(provider, payload, token = payload.findAuthToken(), userId = payload.findUserId()),
                connectTimeoutMillis = 20_000,
                readTimeoutMillis = 45_000,
            )
            if (response.statusCode == 401 || response.statusCode == 403) {
                throw BalanceFetchException(
                    message = "NewAPI auth missing: ${payload.diagnosticSummary()}. Open Login after sign-in, wait for dashboard, then tap Save.",
                    reason = BalanceFetchFailureReason.NeedsLogin,
                )
            }
            runCatching {
                response.ensureJsonSuccess("NewAPI", "balance request")
            }.onFailure { throwable ->
                lastError = throwable as? BalanceFetchException ?: BalanceFetchException(
                    message = throwable.message ?: "NewAPI balance request failed",
                    reason = BalanceFetchFailureReason.Unknown,
                    cause = throwable,
                )
                return@forEach
            }
            runCatching {
                BalanceFetchResult(formatBalance(parseQuotaBalance(response.body)), BalanceUnit.Usd)
            }.onSuccess {
                return it
            }.onFailure { throwable ->
                lastError = BalanceFetchException(
                    message = throwable.message ?: "NewAPI quota fields not found",
                    reason = BalanceFetchFailureReason.InvalidResponse,
                    cause = throwable,
                )
            }
        }

        throw lastError ?: BalanceFetchException("NewAPI quota fields not found", BalanceFetchFailureReason.InvalidResponse)
    }

    /** 处理parseQuotaBalance 方法。 */
    private fun parseQuotaBalance(body: String): Double {
        val data = BalanceJsonExtractor.unwrapData(body)
        val remainingQuota = BalanceJsonExtractor.findNumber(
            data,
            listOf("quota", "remain_quota", "remaining_quota", "left_quota"),
        )
        val balance = BalanceJsonExtractor.findNumber(
            data,
            listOf("balance", "money", "credit", "amount", "remaining_balance", "available_balance"),
        )
        val usedQuota = BalanceJsonExtractor.findNumber(data, listOf("used_quota", "usedQuota"))
        balance?.let { return it }
        val quota = remainingQuota ?: usedQuota?.unaryMinus()
            ?: throw BalanceFetchException(
                message = "NewAPI quota fields not found: fields=${BalanceJsonExtractor.fieldNames(data)}",
                reason = BalanceFetchFailureReason.InvalidResponse,
            )
        return quota / ONE_API_QUOTA_PER_USD
    }

    /** 处理findAuthToken 方法。 */
    private fun LoginPayload.WebSession.findAuthToken(): String? {
        return LoginDataInspector.findAuthToken(
            storage,
            listOf("token", "access_token", "accessToken", "user_token", "auth_token", "jwt"),
        )
    }

    /** 处理findUserId 方法。 */
    private fun LoginPayload.WebSession.findUserId(): String? {
        return LoginDataInspector.findUserId(storage)
    }

    /** 处理diagnosticSummary 方法。 */
    private fun LoginPayload.WebSession.diagnosticSummary(): String {
        return "cookie=${cookies.isNotBlank()}, uid=${findUserId() != null}, storageKeys=${LoginDataInspector.storageKeySummary(storage)}"
    }

    /** 处理formatBalance 方法。 */
    private fun formatBalance(value: Double): String {
        return BalanceFormatters.usd(value)
    }

    private companion object {
        /** 处理ONE_API_QUOTA_PER_USD 常量。 */
        const val ONE_API_QUOTA_PER_USD = 500_000.0
        val SELF_ENDPOINTS = listOf(
            "/api/user/self",
            "/api/user/profile",
            "/api/user/info",
            "/api/user/status",
            "/api/user",
            "/api/v1/user/self",
            "/api/v1/user/profile",
        )
    }
}
