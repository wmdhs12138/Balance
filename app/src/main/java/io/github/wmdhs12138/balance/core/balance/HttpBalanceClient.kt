package io.github.wmdhs12138.balance.core.balance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/** BalanceHttpClient 接口。 */
interface BalanceHttpClient {
    /** 处理get 方法。 */
    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        connectTimeoutMillis: Int = 20_000,
        readTimeoutMillis: Int = 30_000,
        maxBodyBytes: Int = 256 * 1024,
    ): HttpResponse
}

/** HttpBalanceClient 类。 */
class HttpBalanceClient : BalanceHttpClient {
    /** 处理get 方法。 */
    override suspend fun get(
        url: String,
        headers: Map<String, String>,
        connectTimeoutMillis: Int,
        readTimeoutMillis: Int,
        maxBodyBytes: Int,
    ): HttpResponse = withContext(Dispatchers.IO) {
        runCatching {
            currentCoroutineContext().ensureActive()
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = connectTimeoutMillis
                readTimeout = readTimeoutMillis
                useCaches = false
                instanceFollowRedirects = true
                setRequestProperty("Connection", "close")
                headers.forEach { (key, value) -> setRequestProperty(key, value) }
            }

            try {
                val status = connection.responseCode
                val stream = if (status in 200..299) connection.inputStream else connection.errorStream
                HttpResponse(
                    statusCode = status,
                    contentType = connection.contentType.orEmpty(),
                    body = stream?.readLimitedText(maxBodyBytes).orEmpty(),
                    truncated = stream != null && connection.contentLengthLong > maxBodyBytes,
                )
            } finally {
                connection.disconnect()
            }
        }.getOrElse { throwable ->
            currentCoroutineContext().ensureActive()
            throw throwable.toBalanceFetchException(url)
        }
    }

    /** 处理readLimitedText 方法。 */
    private fun java.io.InputStream.readLimitedText(maxBytes: Int): String {
        val buffer = ByteArray(maxBytes + 1)
        var total = 0
        while (total < buffer.size) {
            val read = read(buffer, total, buffer.size - total)
            if (read == -1) break
            total += read
        }
        return buffer.decodeToString(endIndex = minOf(total, maxBytes))
    }

    /** 转换为BalanceFetchException结果 方法。 */
    private fun Throwable.toBalanceFetchException(url: String): BalanceFetchException {
        val reason = when (this) {
            is SocketTimeoutException -> BalanceFetchFailureReason.Timeout
            is UnknownHostException, is SSLException, is IOException -> BalanceFetchFailureReason.Network
            else -> BalanceFetchFailureReason.Unknown
        }
        return BalanceFetchException(
            message = "Network request failed for $url: ${message ?: javaClass.simpleName}",
            reason = reason,
            cause = this,
        )
    }

    private companion object {
        /** 处理MAX_BODY_BYTES 常量。 */
        const val MAX_BODY_BYTES = 256 * 1024
    }
}

/** HttpResponse 数据结构。 */
data class HttpResponse(
    val statusCode: Int,
    val contentType: String,
    val body: String,
    val truncated: Boolean = false,
) {
    val looksLikeHtml: Boolean
        get() = contentType.contains("text/html", ignoreCase = true) ||
            body.trimStart().startsWith("<!doctype html", ignoreCase = true) ||
            body.trimStart().startsWith("<html", ignoreCase = true)

    val failureReason: BalanceFetchFailureReason
        get() = when (statusCode) {
            401 -> BalanceFetchFailureReason.NeedsLogin
            403 -> BalanceFetchFailureReason.Forbidden
            404 -> BalanceFetchFailureReason.NotFound
            else -> BalanceFetchFailureReason.Unknown
        }
}
