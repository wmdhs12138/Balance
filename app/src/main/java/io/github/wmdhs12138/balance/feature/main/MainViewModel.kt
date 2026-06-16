package io.github.wmdhs12138.balance.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.wmdhs12138.balance.R
import io.github.wmdhs12138.balance.core.model.AppLanguage
import io.github.wmdhs12138.balance.core.model.BalanceUnit
import io.github.wmdhs12138.balance.core.model.ThemeMode
import io.github.wmdhs12138.balance.core.net.UrlNormalizer
import io.github.wmdhs12138.balance.core.preferences.SettingsRepository
import io.github.wmdhs12138.balance.core.repository.AddProviderResult
import io.github.wmdhs12138.balance.core.repository.ProviderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** MainViewModel 类。 */
class MainViewModel(
    private val providerRepository: ProviderRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val transientState = MutableStateFlow(MainUiState())

    val uiState = combine(
        providerRepository.providers,
        settingsRepository.preferences,
        transientState,
    ) { providers, preferences, transient ->
        transient.copy(
            providers = providers,
            loading = false,
            themeMode = preferences.themeMode,
            dynamicColor = preferences.dynamicColor,
            seedColor = preferences.seedColor,
            language = preferences.language,
            pullRefreshHintShown = preferences.pullRefreshHintShown,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState(),
    )

    /** 刷新全部服务商余额 方法。 */
    fun refreshBalances() {
        viewModelScope.launch {
            settingsRepository.setPullRefreshHintShown(true)
            if (transientState.value.refreshingAll) return@launch
            transientState.update { it.copy(refreshingAll = true) }
            try {
                val summary = providerRepository.refreshBalances()
                transientState.update {
                    it.copy(message = MainMessages.refreshComplete(summary.ready, summary.needsLogin, summary.failed))
                }
            } finally {
                transientState.update { it.copy(refreshingAll = false) }
            }
        }
    }

    /** 刷新单个服务商余额 方法。 */
    fun refreshProvider(providerId: Long) {
        viewModelScope.launch {
            transientState.update {
                it.copy(refreshingProviderIds = it.refreshingProviderIds + providerId)
            }
            try {
                val success = providerRepository.refreshProvider(providerId)
                transientState.update {
                    it.copy(
                        message = if (success) {
                            MainMessages.ProviderRefreshed
                        } else {
                            MainMessages.ProviderRefreshFailed
                        },
                    )
                }
            } finally {
                transientState.update {
                    it.copy(refreshingProviderIds = it.refreshingProviderIds - providerId)
                }
            }
        }
    }

    /** 添加自定义服务商 方法。 */
    fun addCustomProvider(name: String, baseUrl: String, parserLabel: String?, balanceUnitOverride: BalanceUnit) {
        viewModelScope.launch {
            val normalizedUrl = UrlNormalizer.webUrl(baseUrl)
            if (normalizedUrl == null) {
                transientState.update { it.copy(message = MainMessages.ProviderUrlInvalid) }
                return@launch
            }
            val normalizedName = name.ifBlank {
                settingsRepository.localizedString(R.string.default_provider_name_custom)
            }
            val message = when (providerRepository.addCustomProvider(normalizedName, normalizedUrl, parserLabel, balanceUnitOverride)) {
                AddProviderResult.Added -> MainMessages.ProviderAdded
                AddProviderResult.AlreadyExists -> MainMessages.ProviderAlreadyExists
                AddProviderResult.InvalidUrl -> MainMessages.ProviderUrlInvalid
            }
            transientState.update { it.copy(message = message) }
        }
    }

    /** 处理saveApiKey 方法。 */
    fun saveApiKey(providerId: Long, apiKey: String) {
        viewModelScope.launch {
            providerRepository.storeApiKey(providerId, apiKey)
            transientState.update { it.copy(message = MainMessages.ApiKeySaved) }
        }
    }

    /** 更新解析器标签 方法。 */
    fun updateParserLabel(providerId: Long, parserLabel: String?) {
        viewModelScope.launch {
            providerRepository.updateParserLabel(providerId, parserLabel)
            transientState.update { it.copy(message = MainMessages.ParserUpdated) }
        }
    }

    /** 更新服务商设置 方法。 */
    fun updateProviderSettings(providerId: Long, name: String, parserLabel: String?, balanceUnitOverride: BalanceUnit) {
        viewModelScope.launch {
            val normalizedName = name.ifBlank {
                settingsRepository.localizedString(R.string.default_provider_name)
            }
            providerRepository.updateProviderSettings(providerId, normalizedName, parserLabel, balanceUnitOverride)
            transientState.update { it.copy(message = MainMessages.ProviderUpdated) }
        }
    }

    /** 删除服务商 方法。 */
    fun deleteProvider(providerId: Long, webDataCleared: Boolean = true) {
        viewModelScope.launch {
            providerRepository.deleteProvider(providerId)
            transientState.update {
                it.copy(
                    message = if (webDataCleared) {
                        MainMessages.ProviderDeleted
                    } else {
                        MainMessages.ProviderDeletedWebDataMayRemain
                    },
                )
            }
        }
    }

    /** 删除全部服务商 方法。 */
    fun deleteAllProviders(webDataCleared: Boolean = true) {
        viewModelScope.launch {
            providerRepository.deleteAllProviders()
            transientState.update {
                it.copy(
                    message = if (webDataCleared) {
                        MainMessages.LocalDataCleared
                    } else {
                        MainMessages.LocalDataClearedWebDataMayRemain
                    },
                )
            }
        }
    }

    /** 设置供应商排序模式。 */
    fun setSortingMode(enabled: Boolean) {
        transientState.update { it.copy(sortingMode = enabled) }
    }

    /** 标记下拉刷新引导已经展示。 */
    fun markPullRefreshHintShown() {
        viewModelScope.launch { settingsRepository.setPullRefreshHintShown(true) }
    }

    /** 保存供应商拖拽后的顺序。 */
    fun updateProviderOrder(providerIds: List<Long>) {
        viewModelScope.launch {
            providerRepository.updateProviderOrder(providerIds)
            transientState.update { it.copy(message = MainMessages.ProviderOrderUpdated) }
        }
    }

    /** 设置主题模式 方法。 */
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    /** 设置动态取色 方法。 */
    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDynamicColor(enabled) }
    }

    /** 设置主题种子色 方法。 */
    fun setSeedColor(color: Long) {
        viewModelScope.launch { settingsRepository.setSeedColor(color) }
    }

    /** 设置应用语言 方法。 */
    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { settingsRepository.setLanguage(language) }
    }

    /** 关闭提示消息 方法。 */
    fun dismissMessage() {
        transientState.update { it.copy(message = null) }
    }
}

/** MainViewModelFactory 类。 */
class MainViewModelFactory(
    private val providerRepository: ProviderRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(providerRepository, settingsRepository) as T
    }
}
