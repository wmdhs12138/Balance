package io.github.wmdhs12138.balance.core.repository

import android.content.Context
import io.github.wmdhs12138.balance.core.balance.BalanceFetcherRegistry
import io.github.wmdhs12138.balance.core.crypto.LoginDataCipher
import io.github.wmdhs12138.balance.core.database.AppDatabase
import io.github.wmdhs12138.balance.core.preferences.SettingsRepository

class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)

    val providerRepository = ProviderRepository(
        providerDao = database.providerDao(),
        loginDataCipher = LoginDataCipher(),
        balanceFetcherRegistry = BalanceFetcherRegistry(),
    )
    val settingsRepository = SettingsRepository(context.applicationContext)
}
