package io.github.wmdhs12138.balance.core.balance

import io.github.wmdhs12138.balance.core.database.ProviderEntity
import io.github.wmdhs12138.balance.core.net.UrlNormalizer
import org.json.JSONObject
import java.util.Locale

class NewApiBalanceFetcher(
    private val httpClient: BalanceHttpClient,
) : BalanceFetcher {
    override suspend fun fetch(provider: ProviderEntity, encryptedLoginPayload: String?): BalanceFetchResult {
        val payload = parseLoginPayload(encryptedLoginPayload) as? LoginPayload.WebSession
            ?: throw BalanceFetchException("Login required", BalanceFetchFailureReason.NeedsLogin)

        var lastError: BalanceFetchException? = null
        SELF_ENDPOINTS.forEach { path ->
            val response = httpClient.get(
                url = UrlNormalizer.endpoint(provider.baseUrl, path),
                headers = payload.headers(provider),
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
                BalanceFetchResult(formatBalance(parseQuotaBalance(response.body)))
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

    private fun ProviderEntity.origin(): String = UrlNormalizer.origin(baseUrl) ?: baseUrl

    private fun LoginPayload.WebSession.headers(provider: ProviderEntity): Map<String, String> {
        val token = findAuthToken()
        val userId = findUserId()
        return buildMap {
            put("Accept", "application/json")
            put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
            put("Cache-Control", "no-store")
            put("Content-Type", "application/json")
            put("Cookie", cookies)
            put("User-Agent", userAgent.ifBlank { BalanceHttpDefaults.USER_AGENT })
            put("Referer", url.ifBlank { provider.baseUrl })
            put("Origin", provider.origin())
            if (!userId.isNullOrBlank()) {
                put("New-Api-User", userId)
            }
            if (!token.isNullOrBlank()) {
                put("Authorization", bearerToken(token))
            }
        }
    }

    private fun LoginPayload.WebSession.findAuthToken(): String? {
        return LoginDataInspector.findAuthToken(
            storage,
            listOf("token", "access_token", "accessToken", "user_token", "auth_token", "jwt"),
        )
    }

    private fun LoginPayload.WebSession.findUserId(): String? {
        return LoginDataInspector.findUserId(storage)
    }

    private fun LoginPayload.WebSession.diagnosticSummary(): String {
        return "cookie=${cookies.isNotBlank()}, uid=${findUserId() != null}, storageKeys=${LoginDataInspector.storageKeySummary(storage)}"
    }

    private fun formatBalance(value: Double): String {
        return String.format(Locale.US, "$ %.2f", value)
    }

    private companion object {
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
