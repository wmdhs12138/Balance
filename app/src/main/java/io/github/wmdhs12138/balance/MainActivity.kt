package io.github.wmdhs12138.balance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.wmdhs12138.balance.core.web.WebLoginDataCleaner
import io.github.wmdhs12138.balance.core.web.WebTitleResolver
import io.github.wmdhs12138.balance.feature.login.WebLoginActivity
import io.github.wmdhs12138.balance.feature.main.MainScreen
import io.github.wmdhs12138.balance.feature.main.MainViewModel
import io.github.wmdhs12138.balance.feature.main.MainViewModelFactory
import io.github.wmdhs12138.balance.ui.locale.ProvideLocalizedContext
import io.github.wmdhs12138.balance.ui.theme.BalanceTheme

/** 应用主入口 Activity，负责装配主题、状态和主界面。 */
class MainActivity : ComponentActivity() {
    private val titleResolver by lazy { WebTitleResolver(this) }
    private val webLoginDataCleaner by lazy { WebLoginDataCleaner(this) }

    /** 释放标题解析 WebView。 */
    override fun onDestroy() {
        titleResolver.destroy()
        super.onDestroy()
    }

    /** 初始化 Compose 主界面和各类用户操作回调。 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as BalanceApp).container
        setContent {
            val viewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(
                    providerRepository = container.providerRepository,
                    settingsRepository = container.settingsRepository,
                ),
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val loginLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult(),
            ) { result ->
                val providerId = WebLoginActivity.providerIdFromResult(result.data)
                if (providerId > 0L) viewModel.refreshProvider(providerId)
            }

            BalanceTheme(
                themeMode = uiState.themeMode,
                seedColor = uiState.seedColor,
                dynamicColor = uiState.dynamicColor,
            ) {
                ProvideLocalizedContext(language = uiState.language) {
                    MainScreen(
                        uiState = uiState,
                        onRefresh = viewModel::refreshBalances,
                        onRefreshProvider = viewModel::refreshProvider,
                        onAddProvider = viewModel::addCustomProvider,
                        onThemeModeChanged = viewModel::setThemeMode,
                        onDynamicColorChanged = viewModel::setDynamicColor,
                        onSeedColorChanged = viewModel::setSeedColor,
                        onLanguageChanged = viewModel::setLanguage,
                        onLoginProvider = { provider ->
                            loginLauncher.launch(
                                WebLoginActivity.createIntent(
                                    context = this,
                                    providerId = provider.id,
                                    providerName = provider.name,
                                    loginUrl = provider.loginUrl,
                                ),
                            )
                        },
                        onSaveApiKey = viewModel::saveApiKey,
                        onUpdateProviderSettings = viewModel::updateProviderSettings,
                        onDeleteProvider = { provider ->
                            val webDataCleared = webLoginDataCleaner.clearProvider(provider)
                            viewModel.deleteProvider(provider.id, webDataCleared)
                        },
                        onClearLocalData = {
                            val webDataCleared = webLoginDataCleaner.clearAll()
                            viewModel.deleteAllProviders(webDataCleared)
                        },
                        onResolveProviderTitle = titleResolver::resolve,
                        onDismissMessage = viewModel::dismissMessage,
                    )
                }
            }
        }
    }
}
