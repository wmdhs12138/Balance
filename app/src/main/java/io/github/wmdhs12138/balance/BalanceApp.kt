package io.github.wmdhs12138.balance

import android.app.Application
import io.github.wmdhs12138.balance.core.repository.AppContainer

/** BalanceApp 类。 */
class BalanceApp : Application() {
    lateinit var container: AppContainer
        private set

    /** 初始化界面与依赖 方法。 */
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
