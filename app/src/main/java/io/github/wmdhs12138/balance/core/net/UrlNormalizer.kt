package io.github.wmdhs12138.balance.core.net

import java.net.URI

data class NormalizedUrl(
    val displayUrl: String,
    val origin: String,
    val host: String,
)

object UrlNormalizer {
    fun normalize(rawUrl: String): NormalizedUrl? {
        val withScheme = rawUrl.trim().withDefaultScheme()
        if (withScheme.isBlank()) return null

        val uri = runCatching { URI(withScheme) }.getOrNull() ?: return null
        val scheme = uri.scheme?.lowercase()?.takeIf { it == "http" || it == "https" } ?: return null
        val host = uri.host?.lowercase()?.takeIf { it.isNotBlank() && it.contains('.') } ?: return null
        val port = uri.port.takeIf { it != -1 }
        val authority = if (port != null) "$host:$port" else host
        val rawPath = uri.rawPath.orEmpty().takeUnless { it == "/" }.orEmpty().trimEnd('/')
        val rawQuery = uri.rawQuery?.let { "?$it" }.orEmpty()
        val displayUrl = "$scheme://$authority$rawPath$rawQuery"

        return NormalizedUrl(
            displayUrl = displayUrl,
            origin = "$scheme://$authority",
            host = host,
        )
    }

    fun webUrl(rawUrl: String): String? = normalize(rawUrl)?.displayUrl

    fun origin(rawUrl: String): String? = normalize(rawUrl)?.origin

    fun endpoint(baseUrl: String, path: String): String {
        val base = origin(baseUrl) ?: baseUrl.trim().trimEnd('/')
        return base + "/" + path.trimStart('/')
    }

    private fun String.withDefaultScheme(): String {
        return if (startsWith("http://", ignoreCase = true) || startsWith("https://", ignoreCase = true)) {
            this
        } else {
            "https://$this"
        }
    }
}
