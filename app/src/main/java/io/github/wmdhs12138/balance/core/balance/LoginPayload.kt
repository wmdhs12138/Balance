package io.github.wmdhs12138.balance.core.balance

import org.json.JSONObject

sealed interface LoginPayload {
    /** WebSession 数据结构。 */
    data class WebSession(
        val url: String,
        val cookies: String,
        val storage: String,
        val userAgent: String,
    ) : LoginPayload

    /** ApiKey 数据结构。 */
    data class ApiKey(val value: String) : LoginPayload
}

/** 处理parseLoginPayload 方法。 */
fun parseLoginPayload(raw: String?): LoginPayload? {
    if (raw.isNullOrBlank()) return null
    val json = JSONObject(raw)
    return when (json.optString("type")) {
        "api_key" -> LoginPayload.ApiKey(json.getString("apiKey"))
        else -> LoginPayload.WebSession(
            url = json.optString("url"),
            cookies = json.optString("cookies"),
            storage = json.optString("storage"),
            userAgent = json.optString("userAgent"),
        )
    }
}

/** 处理apiKeyPayload 方法。 */
fun apiKeyPayload(apiKey: String): String = JSONObject()
    .put("type", "api_key")
    .put("apiKey", apiKey)
    .put("capturedAtMillis", System.currentTimeMillis())
    .toString()
