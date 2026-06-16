package io.github.wmdhs12138.balance.core.web

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import io.github.wmdhs12138.balance.core.net.UrlNormalizer
import io.github.wmdhs12138.balance.core.web.WebViewUtils.cleanBookmarkTitle
import io.github.wmdhs12138.balance.core.web.WebViewUtils.safeDestroy

/** 使用轻量 WebView 解析服务商网页标题。 */
class WebTitleResolver(
    private val context: Context,
) {
    private var requestId = 0
    private var webView: WebView? = null

    /** 取消当前解析并释放 WebView。 */
    fun destroy() {
        requestId += 1
        webView?.safeDestroy()
        webView = null
    }

    /** 解析 URL 的网页标题，成功后回调清理后的标题。 */
    @SuppressLint("SetJavaScriptEnabled")
    fun resolve(rawUrl: String, onResolved: (String) -> Unit) {
        val targetUrl = UrlNormalizer.webUrl(rawUrl) ?: return
        webView?.safeDestroy()
        val currentRequestId = ++requestId
        var resolved = false
        val currentWebView = WebView(context).apply {
            settings.javaScriptEnabled = false
            settings.domStorageEnabled = false
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)
            webChromeClient = object : WebChromeClient() {
                /** 收到标题时尝试立即返回。 */
                override fun onReceivedTitle(view: WebView?, title: String?) {
                    if (currentRequestId != requestId) return
                    val cleanTitle = title?.cleanBookmarkTitle(view?.url) ?: return
                    resolved = true
                    onResolved(cleanTitle)
                }
            }
            webViewClient = object : WebViewClient() {
                /** 限制跳转在同源范围内，避免加载无关页面。 */
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val sameHost = UrlNormalizer.origin(request.url.toString()) == UrlNormalizer.origin(targetUrl)
                    return !sameHost
                }

                /** 页面完成后兜底读取标题并延迟销毁。 */
                override fun onPageFinished(view: WebView, url: String?) {
                    if (currentRequestId != requestId) return
                    if (!resolved) {
                        view.title?.cleanBookmarkTitle(view.url)?.let {
                            resolved = true
                            onResolved(it)
                        }
                    }
                    postDelayed({
                        if (currentRequestId == requestId) webView = null
                        safeDestroy()
                    }, CLEANUP_DELAY_MILLIS)
                }
            }
        }
        webView = currentWebView
        currentWebView.postDelayed({
            if (currentRequestId == requestId) {
                webView = null
                currentWebView.safeDestroy()
            }
        }, RESOLVE_TIMEOUT_MILLIS)
        currentWebView.loadUrl(targetUrl)
    }

    private companion object {
        /** 处理CLEANUP_DELAY_MILLIS 常量。 */
        const val CLEANUP_DELAY_MILLIS = 500L
        /** 处理RESOLVE_TIMEOUT_MILLIS 常量。 */
        const val RESOLVE_TIMEOUT_MILLIS = 10_000L
    }
}
