package io.github.wmdhs12138.balance.feature.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import io.github.wmdhs12138.balance.BalanceApp
import io.github.wmdhs12138.balance.R
import io.github.wmdhs12138.balance.core.balance.LoginDataInspector
import io.github.wmdhs12138.balance.ui.locale.ProvideLocalizedContext
import io.github.wmdhs12138.balance.ui.theme.BalanceTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URI

/** WebLoginActivity 类。 */
class WebLoginActivity : ComponentActivity() {
    private val providerId: Long by lazy { intent.getLongExtra(EXTRA_PROVIDER_ID, 0L) }
    private val providerName: String by lazy { intent.getStringExtra(EXTRA_PROVIDER_NAME).orEmpty() }
    private val loginUrl: String by lazy { intent.getStringExtra(EXTRA_LOGIN_URL).orEmpty() }

    /** 初始化界面与依赖 方法。 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as BalanceApp).container
        lifecycleScope.launch {
            val preferences = container.settingsRepository.preferences.first()
            setContent {
                BalanceTheme(
                    themeMode = preferences.themeMode,
                    seedColor = preferences.seedColor,
                    dynamicColor = preferences.dynamicColor,
                ) {
                    ProvideLocalizedContext(language = preferences.language) {
                        WebLoginScreen(
                            providerName = providerName,
                            loginUrl = loginUrl,
                            onClose = ::finish,
                            onLoginSnapshot = { snapshot ->
                                lifecycleScope.launch {
                                    container.providerRepository.storeEncryptedLogin(providerId, snapshot)
                                    setResult(
                                        Activity.RESULT_OK,
                                        Intent().putExtra(EXTRA_PROVIDER_ID, providerId),
                                    )
                                    finish()
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    companion object {
        /** 处理EXTRA_PROVIDER_ID 常量。 */
        private const val EXTRA_PROVIDER_ID = "provider_id"
        /** 处理EXTRA_PROVIDER_NAME 常量。 */
        private const val EXTRA_PROVIDER_NAME = "provider_name"
        /** 处理EXTRA_LOGIN_URL 常量。 */
        private const val EXTRA_LOGIN_URL = "login_url"

        /** 处理providerIdFromResult 方法。 */
        fun providerIdFromResult(data: Intent?): Long = data?.getLongExtra(EXTRA_PROVIDER_ID, 0L) ?: 0L

        /** 处理createIntent 方法。 */
        fun createIntent(
            context: Context,
            providerId: Long,
            providerName: String,
            loginUrl: String,
        ): Intent = Intent(context, WebLoginActivity::class.java)
            .putExtra(EXTRA_PROVIDER_ID, providerId)
            .putExtra(EXTRA_PROVIDER_NAME, providerName)
            .putExtra(EXTRA_LOGIN_URL, loginUrl)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
/** 处理WebLoginScreen 方法。 */
private fun WebLoginScreen(
    providerName: String,
    loginUrl: String,
    onClose: () -> Unit,
    onLoginSnapshot: (String) -> Unit,
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(providerName) }
    var currentUrl by remember { mutableStateOf(loginUrl) }
    var progress by remember { mutableIntStateOf(0) }
    var canGoBack by remember { mutableStateOf(false) }
    var webViewReady by remember { mutableStateOf(false) }
    var loginDataState by remember { mutableStateOf(LoginDataState.Unknown) }
    val loginStateLabel = loginDataState.label()

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            webView.destroy()
        }
    }

    BackHandler(enabled = canGoBack) {
        webView.goBack()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                        Text(
                            listOf(currentUrl.toHostLabel(), loginStateLabel)
                                .filter { it.isNotBlank() }
                                .joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (loginDataState == LoginDataState.Detected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close))
                    }
                },
                actions = {
                    Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.content_description_encrypted_login_data))
                    IconButton(
                        enabled = webViewReady && loginDataState == LoginDataState.Detected,
                        onClick = {
                            webView.captureLoginSnapshot(currentUrl) { snapshot ->
                                onLoginSnapshot(snapshot)
                            }
                        },
                    ) {
                        Icon(Icons.Default.Save, contentDescription = stringResource(R.string.action_save_login))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    webView.apply {
                        webChromeClient = object : WebChromeClient() {
                            /** 处理ProgressChanged事件 方法。 */
                            override fun onProgressChanged(view: WebView, newProgress: Int) {
                                progress = newProgress
                            }

                            /** 处理ReceivedTitle事件 方法。 */
                            override fun onReceivedTitle(view: WebView, pageTitle: String?) {
                                title = pageTitle?.takeIf { it.isNotBlank() } ?: providerName
                            }
                        }
                        webViewClient = object : WebViewClient() {
                            /** 处理PageStarted事件 方法。 */
                            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                                currentUrl = url.orEmpty()
                                canGoBack = view.canGoBack()
                                webViewReady = false
                                loginDataState = LoginDataState.Unknown
                            }

                            /** 处理shouldOverrideUrlLoading 方法。 */
                            override fun shouldOverrideUrlLoading(
                                view: WebView,
                                request: WebResourceRequest,
                            ): Boolean = false

                            /** 处理PageFinished事件 方法。 */
                            override fun onPageFinished(view: WebView, url: String?) {
                                currentUrl = url.orEmpty()
                                canGoBack = view.canGoBack()
                                CookieManager.getInstance().flush()
                                webViewReady = true
                                view.inspectLoginData(currentUrl) { detected ->
                                    loginDataState = if (detected) LoginDataState.Detected else LoginDataState.Missing
                                }
                            }
                        }
                        loadUrl(loginUrl)
                    }
                },
            )
            if (progress in 1..99) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                )
            }
        }
    }
}

private enum class LoginDataState {
    Unknown,
    Missing,
    Detected,
}

@Composable
/** 转换为显示标签 方法。 */
private fun LoginDataState.label(): String {
    return when (this) {
        LoginDataState.Unknown -> ""
        LoginDataState.Missing -> stringResource(R.string.login_data_missing)
        LoginDataState.Detected -> stringResource(R.string.login_data_detected)
    }
}

/** 处理captureLoginSnapshot 方法。 */
private fun WebView.captureLoginSnapshot(
    currentUrl: String,
    onSnapshot: (String) -> Unit,
) {
    evaluateJavascript(STORAGE_SNAPSHOT_SCRIPT) { storage ->
        val decodedStorage = runCatching { org.json.JSONArray("[${storage.orEmpty()}]").getString(0) }
            .getOrDefault("{}")
        onSnapshot(loginSnapshot(currentUrl, decodedStorage))
    }
}

/** 处理inspectLoginData 方法。 */
private fun WebView.inspectLoginData(
    currentUrl: String,
    onResult: (Boolean) -> Unit,
) {
    evaluateJavascript(STORAGE_SNAPSHOT_SCRIPT) { storage ->
        val decodedStorage = runCatching { org.json.JSONArray("[${storage.orEmpty()}]").getString(0) }
            .getOrDefault("{}")
        val cookieUrl = currentUrl.originUrl().ifBlank { currentUrl }
        val cookies = listOf(currentUrl, cookieUrl)
            .map { CookieManager.getInstance().getCookie(it).orEmpty() }
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString("; ")
        onResult(LoginDataInspector.hasLoginData(cookies, decodedStorage))
    }
}

/** 处理loginSnapshot 方法。 */
private fun WebView.loginSnapshot(
    currentUrl: String,
    storageSnapshot: String,
): String {
    val cookieUrl = currentUrl.originUrl().ifBlank { currentUrl }
    val cookies = listOf(currentUrl, cookieUrl)
        .map { CookieManager.getInstance().getCookie(it).orEmpty() }
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString("; ")
    return JSONObject()
        .put("type", "web_session")
        .put("url", currentUrl)
        .put("cookies", cookies)
        .put("storage", storageSnapshot)
        .put("userAgent", settings.userAgentString)
        .put(
            "diagnostics",
            JSONObject()
                .put("hasCookie", cookies.isNotBlank())
                .put("hasToken", LoginDataInspector.findAuthToken(storageSnapshot) != null)
                .put("storageKeys", LoginDataInspector.storageKeySummary(storageSnapshot)),
        )
        .put("capturedAtMillis", System.currentTimeMillis())
        .toString()
}

/** 处理originUrl 方法。 */
private fun String.originUrl(): String = runCatching {
    val uri = URI(this)
    "${uri.scheme}://${uri.host}"
}.getOrDefault("")

/** 转换为HostLabel结果 方法。 */
private fun String.toHostLabel(): String = runCatching {
    URI(this).host?.takeIf { it.isNotBlank() } ?: this
}.getOrDefault(this)

/** 处理STORAGE_SNAPSHOT_SCRIPT 常量。 */
private const val STORAGE_SNAPSHOT_SCRIPT = """
    JSON.stringify({
        localStorage: Object.keys(window.localStorage || {}).reduce(function(acc, key) {
            acc[key] = window.localStorage.getItem(key);
            return acc;
        }, {}),
        sessionStorage: Object.keys(window.sessionStorage || {}).reduce(function(acc, key) {
            acc[key] = window.sessionStorage.getItem(key);
            return acc;
        }, {})
    })
"""
