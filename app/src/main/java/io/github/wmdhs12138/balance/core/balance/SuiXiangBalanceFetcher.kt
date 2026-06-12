package io.github.wmdhs12138.balance.core.balance

import io.github.wmdhs12138.balance.core.database.ProviderEntity
import io.github.wmdhs12138.balance.core.net.UrlNormalizer
import java.util.Locale

class Sub2ApiBalanceFetcher(
    private val httpClient: BalanceHttpClient,
) : BalanceFetcher {
    override suspend fun fetch(provider: ProviderEntity, encryptedLoginPayload: String?): BalanceFetchResult {
        val payload = parseLoginPayload(encryptedLoginPayload) as? LoginPayload.WebSession
            ?: throw BalanceFetchException("Login required", BalanceFetchFailureReason.NeedsLogin)
        val token = payload.findAuthToken()
            ?: throw BalanceFetchException(
                message = "Sub2API auth missing: ${payload.diagnosticSummary()}. Open Login after sign-in, wait for dashboard, then tap Save.",
                reason = BalanceFetchFailureReason.NeedsLogin,
            )

        var lastError: BalanceFetchException? = null
        AUTH_ENDPOINTS.forEach { path ->
            val response = httpClient.get(
                url = UrlNormalizer.endpoint(provider.baseUrl, path),
                headers = payload.headers(provider, token),
                connectTimeoutMillis = 20_000,
                readTimeoutMillis = 45_000,
            )
            if (response.statusCode == 401 || response.statusCode == 403) {
                throw BalanceFetchException(
                    message = "Sub2API login expired or token missing. Open Login after sign-in, wait for dashboard, then tap Save.",
                    reason = BalanceFetchFailureReason.NeedsLogin,
                )
            }
            runCatching {
                response.ensureJsonSuccess("Sub2API", "balance request")
            }.onFailure { throwable ->
                lastError = throwable as? BalanceFetchException ?: BalanceFetchException(
                    message = throwable.message ?: "Sub2API balance request failed",
                    reason = BalanceFetchFailureReason.Unknown,
                    cause = throwable,
                )
                return@forEach
            }
            runCatching {
                val data = BalanceJsonExtractor.unwrapData(response.body)
                val balance = BalanceJsonExtractor.findNumber(
                    data,
                    listOf(
                        "balance",
                        "wallet_balance",
                        "remaining_balance",
                        "available_balance",
                        "credit",
                        "credits",
                        "amount",
                        "money",
                        "quota",
                    ),
                ) ?: throw BalanceFetchException(
                    message = "Sub2API balance field not found: fields=${BalanceJsonExtractor.fieldNames(data)}",
                    reason = BalanceFetchFailureReason.InvalidResponse,
                )
                BalanceFetchResult(String.format(Locale.US, "$ %.2f", balance))
            }.onSuccess {
                return it
            }.onFailure { throwable ->
                lastError = BalanceFetchException(
                    message = throwable.message ?: "Sub2API balance field not found",
                    reason = BalanceFetchFailureReason.InvalidResponse,
                    cause = throwable,
                )
            }
        }
        throw lastError ?: BalanceFetchException("Sub2API balance field not found", BalanceFetchFailureReason.InvalidResponse)
    }

    private fun ProviderEntity.origin(): String = UrlNormalizer.origin(baseUrl) ?: baseUrl

    private fun LoginPayload.WebSession.headers(provider: ProviderEntity, token: String): Map<String, String> {
        return buildMap {
            put("Accept", "application/json")
            put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
            put("Cache-Control", "no-store")
            put("Content-Type", "application/json")
            if (cookies.isNotBlank()) {
                put("Cookie", cookies)
            }
            put("User-Agent", userAgent.ifBlank { BalanceHttpDefaults.USER_AGENT })
            put("Referer", url.ifBlank { provider.baseUrl })
            put("Origin", provider.origin())
            put("Authorization", bearerToken(token))
        }
    }

    private fun LoginPayload.WebSession.findAuthToken(): String? {
        return LoginDataInspector.findAuthToken(
            storage,
            listOf("auth_token", "access_token", "accessToken", "token", "jwt"),
        )
    }

    private fun LoginPayload.WebSession.diagnosticSummary(): String {
        return "cookie=${cookies.isNotBlank()}, authToken=${findAuthToken() != null}, storageKeys=${LoginDataInspector.storageKeySummary(storage)}"
    }

    private companion object {
        val AUTH_ENDPOINTS = listOf(
            "/api/v1/auth/me",
            "/api/auth/me",
            "/auth/me",
            "/api/user/me",
            "/api/user/profile",
        )
    }
}
