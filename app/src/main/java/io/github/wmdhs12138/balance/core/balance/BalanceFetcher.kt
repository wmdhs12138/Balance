package io.github.wmdhs12138.balance.core.balance

import io.github.wmdhs12138.balance.core.database.ProviderEntity

interface BalanceFetcher {
    suspend fun fetch(provider: ProviderEntity, encryptedLoginPayload: String?): BalanceFetchResult
}
