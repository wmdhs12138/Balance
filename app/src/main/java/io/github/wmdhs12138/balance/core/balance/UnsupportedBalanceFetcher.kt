package io.github.wmdhs12138.balance.core.balance

import io.github.wmdhs12138.balance.core.database.ProviderEntity

/** UnsupportedBalanceFetcher 类。 */
class UnsupportedBalanceFetcher : BalanceFetcher {
    /** 获取余额数据 方法。 */
    override suspend fun fetch(provider: ProviderEntity, encryptedLoginPayload: String?): BalanceFetchResult {
        throw BalanceFetchException(
            message = "Balance parser is not configured",
            reason = BalanceFetchFailureReason.Unsupported,
        )
    }
}
