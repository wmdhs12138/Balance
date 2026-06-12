package io.github.wmdhs12138.balance.core.balance

import io.github.wmdhs12138.balance.core.database.ProviderEntity

class BalanceFetcherRegistry(
    httpClient: BalanceHttpClient = HttpBalanceClient(),
) {
    private val newApiFetcher = NewApiBalanceFetcher(httpClient)
    private val deepSeekFetcher = DeepSeekBalanceFetcher(httpClient)
    private val sub2ApiFetcher = Sub2ApiBalanceFetcher(httpClient)
    private val accountSummaryFetcher = AccountSummaryBalanceFetcher(httpClient)
    private val unsupportedFetcher = UnsupportedBalanceFetcher()

    fun forProvider(provider: ProviderEntity): BalanceFetcher = when {
        provider.balanceEndpointHint.equals("DeepSeek", ignoreCase = true) -> deepSeekFetcher
        provider.balanceEndpointHint.equals("Sub2API", ignoreCase = true) -> sub2ApiFetcher
        provider.balanceEndpointHint.equals("Account Summary", ignoreCase = true) -> accountSummaryFetcher
        provider.balanceEndpointHint.equals("NewAPI", ignoreCase = true) -> newApiFetcher
        else -> unsupportedFetcher
    }
}
