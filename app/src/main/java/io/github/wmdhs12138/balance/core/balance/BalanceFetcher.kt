package io.github.wmdhs12138.balance.core.balance

import io.github.wmdhs12138.balance.core.database.ProviderEntity

/** BalanceFetcher 接口。 */
interface BalanceFetcher {
    /** 获取余额数据 方法。 */
    suspend fun fetch(provider: ProviderEntity, encryptedLoginPayload: String?): BalanceFetchResult
}
