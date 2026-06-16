package io.github.wmdhs12138.balance.feature.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.wmdhs12138.balance.R
import io.github.wmdhs12138.balance.core.model.AppLanguage
import io.github.wmdhs12138.balance.core.model.BalanceUnit
import io.github.wmdhs12138.balance.core.model.Provider
import io.github.wmdhs12138.balance.core.model.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/** 渲染主界面。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: MainUiState,
    onRefresh: () -> Unit,
    onRefreshProvider: (Long) -> Unit,
    onSortingModeChanged: (Boolean) -> Unit,
    onUpdateProviderOrder: (List<Long>) -> Unit,
    onPullRefreshHintShown: () -> Unit,
    onAddProvider: (String, String, String?, BalanceUnit) -> Unit,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onSeedColorChanged: (Long) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    onLoginProvider: (Provider) -> Unit,
    onSaveApiKey: (Long, String) -> Unit,
    onUpdateProviderSettings: (Long, String, String?, BalanceUnit) -> Unit,
    onDeleteProvider: (Provider) -> Unit,
    onClearLocalData: () -> Unit,
    onResolveProviderTitle: (String, (String) -> Unit) -> Unit,
    onDismissMessage: () -> Unit,
) {
    val strings = rememberLocalizedStrings(uiState.language)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()
    val lazyListState = rememberLazyListState()
    var showAddSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var apiKeyProvider by remember { mutableStateOf<Provider?>(null) }
    var parserProvider by remember { mutableStateOf<Provider?>(null) }
    var sortableProviders by remember { mutableStateOf(uiState.providers) }
    val snackbarMessage = uiState.message?.localizedText(strings)
    val showPullRefreshHint = !uiState.pullRefreshHintShown &&
        uiState.providers.isNotEmpty() &&
        !uiState.refreshingAll &&
        !uiState.sortingMode

    BackHandler(enabled = uiState.sortingMode) {
        onSortingModeChanged(false)
    }

    LaunchedEffect(showPullRefreshHint) {
        if (showPullRefreshHint) {
            delay(3_500)
            onPullRefreshHintShown()
        }
    }

    LaunchedEffect(uiState.providers, uiState.sortingMode) {
        if (!uiState.sortingMode) sortableProviders = uiState.providers
    }

    LaunchedEffect(snackbarMessage) {
        val message = snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onDismissMessage()
    }

    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        sortableProviders = sortableProviders.toMutableList().apply {
            add(to.index - 1, removeAt(from.index - 1))
        }
        onUpdateProviderOrder(sortableProviders.map { it.id })
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MainTopBar(
                title = strings.get(if (uiState.sortingMode) R.string.sort_providers_title else R.string.app_name),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val providerListContent: @Composable () -> Unit = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(key = "action-bar") {
                    MainActionBar(
                        strings = strings,
                        sortingMode = uiState.sortingMode,
                        onAdd = { showAddSheet = true },
                        onSort = {
                            sortableProviders = uiState.providers
                            onSortingModeChanged(true)
                        },
                        onSettings = { showSettingsSheet = true },
                        onDoneSorting = { onSortingModeChanged(false) },
                    )
                }
                items(
                    items = if (uiState.sortingMode) sortableProviders else uiState.providers,
                    key = { it.id },
                ) { provider ->
                    if (uiState.sortingMode) {
                        ReorderableItem(reorderableState, key = provider.id) { isDragging ->
                            ProviderCard(
                                provider = provider,
                                strings = strings,
                                refreshing = false,
                                sortingMode = true,
                                isDragging = isDragging,
                                dragHandleModifier = Modifier.longPressDraggableHandle(),
                                onRefresh = {},
                                onLogin = {},
                                onApiKey = {},
                                onLongPress = {},
                            )
                        }
                    } else {
                        ProviderCard(
                            provider = provider,
                            strings = strings,
                            refreshing = uiState.refreshingAll || provider.id in uiState.refreshingProviderIds,
                            sortingMode = false,
                            onRefresh = { onRefreshProvider(provider.id) },
                            onLogin = { onLoginProvider(provider) },
                            onApiKey = { apiKeyProvider = provider },
                            onLongPress = { parserProvider = provider },
                        )
                    }
                }
                item(key = "bottom-spacer") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (uiState.sortingMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                providerListContent()
            }
        } else {
            PullToRefreshBox(
                isRefreshing = uiState.refreshingAll,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                state = pullToRefreshState,
                indicator = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        if (showPullRefreshHint && pullToRefreshState.distanceFraction <= 0.01f) {
                            PullRefreshHint(
                                visible = true,
                                strings = strings,
                            )
                        } else {
                            BalancePullRefreshIndicator(
                                isRefreshing = uiState.refreshingAll,
                                state = pullToRefreshState,
                                strings = strings,
                            )
                        }
                    }
                },
            ) {
                providerListContent()
            }
        }
    }

    if (showAddSheet) {
        AddProviderSheet(
            strings = strings,
            onDismiss = { showAddSheet = false },
            onResolveTitle = onResolveProviderTitle,
            onSubmit = { name, url, parserLabel, balanceUnitOverride ->
                scope.launch {
                    onAddProvider(name, url, parserLabel, balanceUnitOverride)
                    showAddSheet = false
                }
            },
        )
    }

    if (showSettingsSheet) {
        SettingsSheet(
            uiState = uiState,
            strings = strings,
            onDismiss = { showSettingsSheet = false },
            onThemeModeChanged = onThemeModeChanged,
            onDynamicColorChanged = onDynamicColorChanged,
            onSeedColorChanged = onSeedColorChanged,
            onLanguageChanged = onLanguageChanged,
            onClearLocalData = onClearLocalData,
        )
    }

    apiKeyProvider?.let { provider ->
        ApiKeySheet(
            provider = provider,
            strings = strings,
            onDismiss = { apiKeyProvider = null },
            onSubmit = { apiKey ->
                onSaveApiKey(provider.id, apiKey)
                apiKeyProvider = null
            },
        )
    }

    parserProvider?.let { provider ->
        ProviderSettingsSheet(
            provider = provider,
            strings = strings,
            onDismiss = { parserProvider = null },
            onLogin = {
                parserProvider = null
                onLoginProvider(provider)
            },
            onApiKey = {
                parserProvider = null
                apiKeyProvider = provider
            },
            onSubmit = { name, parserLabel, balanceUnitOverride ->
                onUpdateProviderSettings(provider.id, name, parserLabel, balanceUnitOverride)
                parserProvider = null
            },
            onDelete = {
                onDeleteProvider(provider)
                parserProvider = null
            },
        )
    }
}
