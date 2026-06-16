package io.github.wmdhs12138.balance.core.web

import android.webkit.WebView
import android.webkit.WebViewClient

/** WebView 通用安全工具。 */
object WebViewUtils {
    /** 安全停止并销毁 WebView，忽略销毁阶段异常。 */
    fun WebView.safeDestroy() {
        runCatching {
            stopLoading()
            webChromeClient = null
            webViewClient = WebViewClient()
            destroy()
        }
    }

    /** 清理网页标题中的 HTML 实体和无效内容。 */
    fun String.cleanBookmarkTitle(currentUrl: String?): String? {
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
}
