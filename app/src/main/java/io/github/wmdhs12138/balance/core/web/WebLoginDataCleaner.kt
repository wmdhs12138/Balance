package io.github.wmdhs12138.balance.core.web

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import io.github.wmdhs12138.balance.core.model.Provider
import io.github.wmdhs12138.balance.core.net.UrlNormalizer
import io.github.wmdhs12138.balance.core.web.WebViewUtils.safeDestroy

/** 清理 Web 登录产生的 Cookie、缓存与本地存储。 */
class WebLoginDataCleaner(
    private val context: Context,
) {
    /** 清理指定服务商相关站点的 Web 登录数据。 */
    @SuppressLint("SetJavaScriptEnabled")
    fun clearProvider(provider: Provider): Boolean {
        val origins = provider.webOrigins()
        if (origins.isEmpty()) return false
        val cookieManager = CookieManager.getInstance()
        return runCatching {
            origins.forEach { origin ->
                cookieManager.clearCookiesForOrigin(origin)
                val webView = WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.cacheMode = WebSettings.LOAD_NO_CACHE
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                }
                val timeoutDestroy = Runnable { webView.safeDestroy() }
                webView.webViewClient = object : WebViewClient() {
                    /** 页面加载后清空 localStorage 和 sessionStorage。 */
                    override fun onPageFinished(view: WebView, url: String?) {
                        view.evaluateJavascript("localStorage.clear();sessionStorage.clear();") {
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

    /** 清理应用可访问的全部 Web 登录数据。 */
    fun clearAll(): Boolean {
        return runCatching {
            val cookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookies(null)
            cookieManager.removeSessionCookies(null)
            cookieManager.flush()
            WebView(context).apply {
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

    /** 收集服务商基础地址和登录地址对应的源。 */
    private fun Provider.webOrigins(): List<String> {
        return listOf(baseUrl, loginUrl)
            .mapNotNull(UrlNormalizer::origin)
            .distinct()
    }

    /** 让指定源的 Cookie 立即过期。 */
    private fun CookieManager.clearCookiesForOrigin(origin: String) {
        getCookie(origin)
            .orEmpty()
            .split(';')
            .mapNotNull { cookie -> cookie.substringBefore('=').trim().takeIf { it.isNotBlank() } }
            .forEach { name ->
                setCookie(origin, "$name=; Max-Age=0; Path=/")
                setCookie(origin, "$name=; Max-Age=0; Path=/; Secure")
                setCookie(origin, "$name=; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Path=/")
            }
    }

    private companion object {
        /** 处理WEB_LOGIN_DATA_CLEAR_TIMEOUT_MILLIS 常量。 */
        const val WEB_LOGIN_DATA_CLEAR_TIMEOUT_MILLIS = 3_000L
    }
}
