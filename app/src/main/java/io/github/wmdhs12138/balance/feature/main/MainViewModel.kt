package io.github.wmdhs12138.balance.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.wmdhs12138.balance.R
import io.github.wmdhs12138.balance.core.model.AppLanguage
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
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState(),
    )

    init {
        viewModelScope.launch {
            providerRepository.normalizeLegacyData()
        }
    }

    fun refreshBalances() {
        viewModelScope.launch {
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

    fun addCustomProvider(name: String, baseUrl: String, parserLabel: String?) {
        viewModelScope.launch {
            val normalizedUrl = UrlNormalizer.webUrl(baseUrl)
            if (normalizedUrl == null) {
                transientState.update { it.copy(message = MainMessages.ProviderUrlInvalid) }
                return@launch
            }
            val normalizedName = name.ifBlank {
                settingsRepository.localizedString(R.string.default_provider_name_custom)
            }
            val message = when (providerRepository.addCustomProvider(normalizedName, normalizedUrl, parserLabel)) {
                AddProviderResult.Added -> MainMessages.ProviderAdded
                AddProviderResult.AlreadyExists -> MainMessages.ProviderAlreadyExists
                AddProviderResult.InvalidUrl -> MainMessages.ProviderUrlInvalid
            }
            transientState.update { it.copy(message = message) }
        }
    }

    fun saveApiKey(providerId: Long, apiKey: String) {
        viewModelScope.launch {
            providerRepository.storeApiKey(providerId, apiKey)
            transientState.update { it.copy(message = MainMessages.ApiKeySaved) }
        }
    }

    fun updateParserLabel(providerId: Long, parserLabel: String?) {
        viewModelScope.launch {
            providerRepository.updateParserLabel(providerId, parserLabel)
            transientState.update { it.copy(message = MainMessages.ParserUpdated) }
        }
    }

    fun updateProviderSettings(providerId: Long, name: String, parserLabel: String?) {
        viewModelScope.launch {
            val normalizedName = name.ifBlank {
                settingsRepository.localizedString(R.string.default_provider_name)
            }
            providerRepository.updateProviderSettings(providerId, normalizedName, parserLabel)
            transientState.update { it.copy(message = MainMessages.ProviderUpdated) }
        }
    }

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

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDynamicColor(enabled) }
    }

    fun setSeedColor(color: Long) {
        viewModelScope.launch { settingsRepository.setSeedColor(color) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { settingsRepository.setLanguage(language) }
    }

    fun dismissMessage() {
        transientState.update { it.copy(message = null) }
    }
}

class MainViewModelFactory(
    private val providerRepository: ProviderRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(providerRepository, settingsRepository) as T
    }
}
