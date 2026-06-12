package io.github.wmdhs12138.balance.core.balance

import io.github.wmdhs12138.balance.core.database.ProviderEntity
import io.github.wmdhs12138.balance.core.net.UrlNormalizer
import org.json.JSONObject
import java.util.Locale

class AccountSummaryBalanceFetcher(
    private val httpClient: BalanceHttpClient,
) : BalanceFetcher {
    override suspend fun fetch(provider: ProviderEntity, encryptedLoginPayload: String?): BalanceFetchResult {
        val payload = parseLoginPayload(encryptedLoginPayload)
            ?: throw BalanceFetchException("API key or login required", BalanceFetchFailureReason.NeedsLogin)
        return when (payload) {
            is LoginPayload.ApiKey -> fetchAccountSummary(
                provider = provider,
                token = payload.value,
                userAgent = BalanceHttpDefaults.USER_AGENT,
                cookies = null,
            )
            is LoginPayload.WebSession -> fetchWebSessionBalance(provider, payload)
        }
    }

    private suspend fun fetchWebSessionBalance(
        provider: ProviderEntity,
        payload: LoginPayload.WebSession,
    ): BalanceFetchResult {
        val token = payload.findUserToken()
            ?: throw BalanceFetchException(
                message = "Account Summary login token not found: ${payload.diagnosticSummary()}",
                reason = BalanceFetchFailureReason.NeedsLogin,
            )

        val authResponse = httpClient.get(
            url = UrlNormalizer.endpoint(provider.baseUrl, "/auth/me"),
            headers = headers(
                provider = provider,
                token = token,
                userAgent = payload.userAgent.ifBlank { BalanceHttpDefaults.USER_AGENT },
                cookies = payload.cookies,
            ),
            connectTimeoutMillis = 20_000,
            readTimeoutMillis = 45_000,
        )
        if (authResponse.statusCode in 200..299) {
            if (!authResponse.looksLikeHtml) {
                val data = BalanceJsonExtractor.unwrapData(authResponse.body)
                data.findBalance()?.let { balance ->
                    return BalanceFetchResult(formatBalance(balance))
                }
            }
        } else if (authResponse.statusCode == 401 || authResponse.statusCode == 403) {
            throw BalanceFetchException(
                message = "Account Summary login expired or token missing. Open Login after sign-in, then tap Save.",
                reason = BalanceFetchFailureReason.NeedsLogin,
            )
        }

        return fetchAccountSummary(
            provider = provider,
            token = token,
            userAgent = payload.userAgent.ifBlank { BalanceHttpDefaults.USER_AGENT },
            cookies = payload.cookies,
        )
    }

    private suspend fun fetchAccountSummary(
        provider: ProviderEntity,
        token: String,
        userAgent: String,
        cookies: String?,
    ): BalanceFetchResult {
        var lastError: BalanceFetchException? = null
        ACCOUNT_SUMMARY_ENDPOINTS.forEach { path ->
            val response = httpClient.get(
                url = UrlNormalizer.endpoint(provider.baseUrl, path),
                headers = headers(provider, token, userAgent, cookies),
                connectTimeoutMillis = 20_000,
                readTimeoutMillis = 45_000,
            )
            if (response.statusCode == 401 || response.statusCode == 403) {
                throw BalanceFetchException(
                    message = "Account Summary auth failed. Save a valid API key or login token, then refresh.",
                    reason = BalanceFetchFailureReason.NeedsLogin,
                )
            }
            runCatching {
                response.ensureJsonSuccess("Account Summary", "request")
            }.onFailure { throwable ->
                lastError = throwable as? BalanceFetchException ?: BalanceFetchException(
                    message = throwable.message ?: "Account Summary request failed",
                    reason = BalanceFetchFailureReason.Unknown,
                    cause = throwable,
                )
                return@forEach
            }

            runCatching {
                val data = BalanceJsonExtractor.unwrapData(response.body)
                val balance = data.findBalance()
                    ?: throw BalanceFetchException(
                        message = "Account Summary balance field not found: fields=${BalanceJsonExtractor.fieldNames(data)}",
                        reason = BalanceFetchFailureReason.InvalidResponse,
                    )
                BalanceFetchResult(formatBalance(balance))
            }.onSuccess {
                return it
            }.onFailure { throwable ->
                lastError = BalanceFetchException(
                    message = throwable.message ?: "Account Summary balance field not found",
                    reason = BalanceFetchFailureReason.InvalidResponse,
                    cause = throwable,
                )
            }
        }
        throw lastError ?: BalanceFetchException("Account Summary balance field not found", BalanceFetchFailureReason.InvalidResponse)
    }

    private fun headers(provider: ProviderEntity, token: String, userAgent: String, cookies: String?): Map<String, String> {
        return buildMap {
            put("Accept", "application/json")
            put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
            put("Cache-Control", "no-store")
            put("Content-Type", "application/json")
            put("Authorization", bearerToken(token))
            put("Referer", provider.baseUrl)
            put("Origin", provider.origin())
            put("User-Agent", userAgent)
            if (!cookies.isNullOrBlank()) {
                put("Cookie", cookies)
            }
        }
    }

    private fun ProviderEntity.origin(): String = UrlNormalizer.origin(baseUrl) ?: baseUrl

    private fun LoginPayload.WebSession.findUserToken(): String? {
        return LoginDataInspector.findAuthToken(
            storage,
            listOf("userToken", "user_token", "token", "access_token", "accessToken"),
        )
    }

    private fun LoginPayload.WebSession.diagnosticSummary(): String {
        return "cookie=${cookies.isNotBlank()}, userToken=${findUserToken() != null}, storageKeys=${LoginDataInspector.storageKeySummary(storage)}"
    }

    private fun JSONObject.findBalance(): Double? {
        return BalanceJsonExtractor.findNumber(
            this,
            listOf(
                "balance",
                "wallet_balance",
                "remaining_balance",
                "available_balance",
                "credit",
                "credits",
                "amount",
                "money",
                "total_balance",
                "cash_balance",
            ),
        )
    }

    private fun formatBalance(balance: Double): String {
        return String.format(Locale.US, "$ %.2f", balance)
    }

    private companion object {
        val ACCOUNT_SUMMARY_ENDPOINTS = listOf(
            "/account/summary",
            "/api/account/summary",
            "/api/user/account/summary",
            "/api/v1/account/summary",
            "/api/wallet/summary",
            "/api/balance",
            "/api/user/balance",
        )
    }
}
