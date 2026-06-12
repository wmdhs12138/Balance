package io.github.wmdhs12138.balance.core.balance

import io.github.wmdhs12138.balance.core.database.ProviderEntity

class UnsupportedBalanceFetcher : BalanceFetcher {
    override suspend fun fetch(provider: ProviderEntity, encryptedLoginPayload: String?): BalanceFetchResult {
        throw BalanceFetchException(
            message = "Balance parser is not configured",
            reason = BalanceFetchFailureReason.Unsupported,
        )
    }
}
