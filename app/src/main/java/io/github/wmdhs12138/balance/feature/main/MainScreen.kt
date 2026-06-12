package io.github.wmdhs12138.balance.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.wmdhs12138.balance.R
import io.github.wmdhs12138.balance.core.model.AppLanguage
import io.github.wmdhs12138.balance.core.model.BalanceStatus
import io.github.wmdhs12138.balance.core.model.Provider
import io.github.wmdhs12138.balance.core.model.ThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: MainUiState,
    onRefresh: () -> Unit,
    onRefreshProvider: (Long) -> Unit,
    onAddProvider: (String, String, String?) -> Unit,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onSeedColorChanged: (Long) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    onLoginProvider: (Provider) -> Unit,
    onSaveApiKey: (Long, String) -> Unit,
    onUpdateProviderSettings: (Long, String, String?) -> Unit,
    onDeleteProvider: (Provider) -> Unit,
    onClearLocalData: () -> Unit,
    onResolveProviderTitle: (String, (String) -> Unit) -> Unit,
    onDismissMessage: () -> Unit,
) {
    val strings = rememberLocalizedStrings(uiState.language)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()
    var showAddSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var apiKeyProvider by remember { mutableStateOf<Provider?>(null) }
    var parserProvider by remember { mutableStateOf<Provider?>(null) }
    val snackbarMessage = uiState.message?.localizedText(strings)

    LaunchedEffect(snackbarMessage) {
        val message = snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onDismissMessage()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(strings.get(R.string.app_name), fontWeight = FontWeight.SemiBold)
                },
                actions = {
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = strings.get(R.string.settings_title),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = strings.get(R.string.action_add_provider),
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.refreshingAll,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            state = pullToRefreshState,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    SummaryHeader(uiState, strings)
                }
                items(uiState.providers, key = { it.id }) { provider ->
                    ProviderCard(
                        provider = provider,
                        strings = strings,
                        refreshing = uiState.refreshingAll || provider.id in uiState.refreshingProviderIds,
                        onRefresh = { onRefreshProvider(provider.id) },
                        onLogin = { onLoginProvider(provider) },
                        onApiKey = { apiKeyProvider = provider },
                        onLongPress = { parserProvider = provider },
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }

    if (showAddSheet) {
        AddProviderSheet(
            strings = strings,
            onDismiss = { showAddSheet = false },
            onResolveTitle = onResolveProviderTitle,
            onSubmit = { name, url, parserLabel ->
                scope.launch {
                    onAddProvider(name, url, parserLabel)
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
            onSubmit = { name, parserLabel ->
                onUpdateProviderSettings(provider.id, name, parserLabel)
                parserProvider = null
            },
            onDelete = {
                onDeleteProvider(provider)
                parserProvider = null
            },
        )
    }
}

@Composable
private fun SummaryHeader(
    uiState: MainUiState,
    strings: LocalizedStrings,
) {
    val connected = uiState.providers.count { it.status == BalanceStatus.Ready }
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "$connected / ${uiState.providers.size}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = strings.get(R.string.summary_ready_message),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
