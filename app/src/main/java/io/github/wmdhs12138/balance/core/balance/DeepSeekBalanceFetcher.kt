package io.github.wmdhs12138.balance.core.balance

import io.github.wmdhs12138.balance.core.database.ProviderEntity
import org.json.JSONObject
import java.util.Locale

class DeepSeekBalanceFetcher(
    private val httpClient: BalanceHttpClient,
) : BalanceFetcher {
    override suspend fun fetch(provider: ProviderEntity, encryptedLoginPayload: String?): BalanceFetchResult {
        val payload = parseLoginPayload(encryptedLoginPayload) as? LoginPayload.ApiKey
            ?: throw BalanceFetchException("API key required", BalanceFetchFailureReason.NeedsLogin)

        val response = httpClient.get(
            url = "https://api.deepseek.com/user/balance",
            headers = mapOf(
                "Authorization" to "Bearer ${payload.value}",
                "Accept" to "application/json",
            ),
        )
        response.ensureJsonSuccess("DeepSeek", "balance request")

        val body = JSONObject(response.body)
        val balances = body.optJSONArray("balance_infos")
        if (balances == null || balances.length() == 0) {
            throw BalanceFetchException("DeepSeek balance response is empty", BalanceFetchFailureReason.InvalidResponse)
        }

        val first = balances.getJSONObject(0)
        val total = BalanceJsonExtractor.findNumber(
            first,
            listOf("total_balance", "granted_balance", "topped_up_balance", "balance", "amount"),
        )
            ?: throw BalanceFetchException("DeepSeek balance value is invalid", BalanceFetchFailureReason.InvalidResponse)
        return BalanceFetchResult(String.format(Locale.CHINA, "¥ %.2f", total))
    }
}
