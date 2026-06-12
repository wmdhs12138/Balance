package io.github.wmdhs12138.balance

import android.app.Application
import io.github.wmdhs12138.balance.core.repository.AppContainer

class BalanceApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
