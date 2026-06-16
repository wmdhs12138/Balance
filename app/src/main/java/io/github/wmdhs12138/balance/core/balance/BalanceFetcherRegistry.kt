package io.github.wmdhs12138.balance.core.balance

import io.github.wmdhs12138.balance.core.database.ProviderEntity

/** BalanceFetcherRegistry 类。 */
class BalanceFetcherRegistry(
    httpClient: BalanceHttpClient = HttpBalanceClient(),
) {
    private val newApiFetcher = NewApiBalanceFetcher(httpClient)
    private val deepSeekFetcher = DeepSeekBalanceFetcher(httpClient)
    private val sub2ApiFetcher = Sub2ApiBalanceFetcher(httpClient)
    private val unsupportedFetcher = UnsupportedBalanceFetcher()

    /** 根据服务商选择余额抓取器 方法。 */
    fun forProvider(provider: ProviderEntity): BalanceFetcher = when {
        BalanceParserLabels.normalize(provider.balanceEndpointHint) == BalanceParserLabels.DEEP_SEEK -> deepSeekFetcher
        BalanceParserLabels.normalize(provider.balanceEndpointHint) == BalanceParserLabels.SUB2_API -> sub2ApiFetcher
        BalanceParserLabels.normalize(provider.balanceEndpointHint) == BalanceParserLabels.NEW_API -> newApiFetcher
        else -> unsupportedFetcher
    }
}
