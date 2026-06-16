package io.github.wmdhs12138.balance.core.balance

import io.github.wmdhs12138.balance.core.database.ProviderEntity
import io.github.wmdhs12138.balance.core.model.BalanceUnit
import io.github.wmdhs12138.balance.core.net.UrlNormalizer

/** Sub2ApiBalanceFetcher 类。 */
class Sub2ApiBalanceFetcher(
    private val httpClient: BalanceHttpClient,
) : BalanceFetcher {
    /** 获取余额数据 方法。 */
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
                headers = BalanceHeaders.webSession(provider, payload, token = token),
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
                val balanceKeys = listOf(
                    "points",
                    "point",
                    "score",
                    "credits",
                    "credit",
                    "tokens",
                    "token",
                    "quota",
                    "balance",
                    "wallet_balance",
                    "remaining_balance",
                    "available_balance",
                    "amount",
                    "money",
                )
                val balance = BalanceJsonExtractor.findNumber(data, balanceKeys)
                    ?: throw BalanceFetchException(
                        message = "Sub2API balance field not found: fields=${BalanceJsonExtractor.fieldNames(data)}",
                        reason = BalanceFetchFailureReason.InvalidResponse,
                    )
                val unit = BalanceJsonExtractor.findMatchedNumberKey(data, balanceKeys)
                    ?.let(BalanceUnit::inferredFromKey)
                    ?.takeUnless { it == BalanceUnit.Unknown }
                    ?: BalanceUnit.Usd
                BalanceFetchResult(BalanceFormatters.number(balance), unit)
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

    /** 处理findAuthToken 方法。 */
    private fun LoginPayload.WebSession.findAuthToken(): String? {
        return LoginDataInspector.findAuthToken(
            storage,
            listOf("auth_token", "access_token", "accessToken", "token", "jwt"),
        )
    }

    /** 处理diagnosticSummary 方法。 */
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
