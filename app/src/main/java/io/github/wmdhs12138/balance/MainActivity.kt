package io.github.wmdhs12138.balance

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.wmdhs12138.balance.feature.login.WebLoginActivity
import io.github.wmdhs12138.balance.feature.main.MainScreen
import io.github.wmdhs12138.balance.feature.main.MainViewModel
import io.github.wmdhs12138.balance.feature.main.MainViewModelFactory
import io.github.wmdhs12138.balance.core.model.Provider
import io.github.wmdhs12138.balance.core.net.UrlNormalizer
import io.github.wmdhs12138.balance.ui.locale.ProvideLocalizedContext
import io.github.wmdhs12138.balance.ui.theme.BalanceTheme

class MainActivity : ComponentActivity() {
    private var titleResolveRequestId = 0
    private var titleResolveWebView: WebView? = null

    override fun onDestroy() {
        titleResolveRequestId += 1
        titleResolveWebView?.safeDestroy()
        titleResolveWebView = null
        super.onDestroy()
    }

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
                if (providerId > 0L) {
                    viewModel.refreshProvider(providerId)
                }
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
                            val webDataCleared = clearWebLoginData(provider)
                            viewModel.deleteProvider(provider.id, webDataCleared)
                        },
                        onClearLocalData = {
                            val webDataCleared = clearAllWebLoginData()
                            viewModel.deleteAllProviders(webDataCleared)
                        },
                        onResolveProviderTitle = ::resolveProviderTitle,
                        onDismissMessage = viewModel::dismissMessage,
                    )
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun resolveProviderTitle(rawUrl: String, onResolved: (String) -> Unit) {
        val targetUrl = UrlNormalizer.webUrl(rawUrl) ?: return
        titleResolveWebView?.safeDestroy()
        val requestId = ++titleResolveRequestId
        var resolved = false
        val webView = WebView(this).apply {
            settings.javaScriptEnabled = false
            settings.domStorageEnabled = false
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)
            webChromeClient = object : WebChromeClient() {
                override fun onReceivedTitle(view: WebView?, title: String?) {
                    if (requestId != titleResolveRequestId) return
                    val cleanTitle = title?.cleanBookmarkTitle(view?.url) ?: return
                    resolved = true
                    onResolved(cleanTitle)
                }
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val sameHost = UrlNormalizer.origin(request.url.toString()) == UrlNormalizer.origin(targetUrl)
                    return !sameHost
                }

                override fun onPageFinished(view: WebView, url: String?) {
                    if (requestId != titleResolveRequestId) return
                    if (!resolved) {
                        view.title?.cleanBookmarkTitle(view.url)?.let {
                            resolved = true
                            onResolved(it)
                        }
                    }
                    postDelayed({
                        if (requestId == titleResolveRequestId) {
                            titleResolveWebView = null
                        }
                        safeDestroy()
                    }, 500)
                }
            }
        }
        titleResolveWebView = webView
        webView.postDelayed({
            if (requestId == titleResolveRequestId) {
                titleResolveWebView = null
                webView.safeDestroy()
            }
        }, 10_000)
        webView.loadUrl(targetUrl)
    }

    private fun clearWebLoginData(provider: Provider): Boolean {
        val origins = provider.webOrigins()
        if (origins.isEmpty()) return false
        val cookieManager = CookieManager.getInstance()
        return runCatching {
            origins.forEach { origin ->
                cookieManager.getCookie(origin)
                    .orEmpty()
                    .split(';')
                    .mapNotNull { cookie -> cookie.substringBefore('=').trim().takeIf { it.isNotBlank() } }
                    .forEach { name ->
                        cookieManager.setCookie(origin, "$name=; Max-Age=0; Path=/")
                        cookieManager.setCookie(origin, "$name=; Max-Age=0; Path=/; Secure")
                        cookieManager.setCookie(origin, "$name=; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Path=/")
                    }
                val webView = WebView(this).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.cacheMode = WebSettings.LOAD_NO_CACHE
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                }
                val timeoutDestroy = Runnable { webView.safeDestroy() }
                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        view.evaluateJavascript(
                            "localStorage.clear();sessionStorage.clear();",
                        ) {
                            view.removeCallbacks(timeoutDestroy)
                            view.clearCache(true)
                            view.safeDestroy()
                        }
                    }
                }
                webView.postDelayed(timeoutDestroy, WEB_LOGIN_DATA_CLEAR_TIMEOUT_MILLIS)
                webView.loadUrl(origin)
            }
            cookieManager.flush()
        }.isSuccess
    }

    private fun clearAllWebLoginData(): Boolean {
        return runCatching {
            val cookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookies(null)
            cookieManager.removeSessionCookies(null)
            cookieManager.flush()
            WebView(this).apply {
                clearCache(true)
                clearHistory()
                clearFormData()
                settings.javaScriptEnabled = false
                settings.domStorageEnabled = false
                loadUrl("about:blank")
                safeDestroy()
            }
        }.isSuccess
    }

    private fun Provider.webOrigins(): List<String> {
        return listOf(baseUrl, loginUrl)
            .mapNotNull(UrlNormalizer::origin)
            .distinct()
    }

    private fun String.cleanBookmarkTitle(currentUrl: String?): String? {
        val normalizedTitle = replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        val normalizedUrl = currentUrl.orEmpty().trim()
        return normalizedTitle
            .takeIf { it.isNotBlank() }
            ?.takeUnless { it.equals("about:blank", ignoreCase = true) }
            ?.takeUnless { normalizedUrl.isNotBlank() && it.equals(normalizedUrl, ignoreCase = true) }
    }

    private fun WebView.safeDestroy() {
        runCatching {
            stopLoading()
            webChromeClient = null
            webViewClient = WebViewClient()
            destroy()
        }
    }

    private companion object {
        const val WEB_LOGIN_DATA_CLEAR_TIMEOUT_MILLIS = 3_000L
    }
}
