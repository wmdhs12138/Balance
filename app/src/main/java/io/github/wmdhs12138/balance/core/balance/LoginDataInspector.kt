package io.github.wmdhs12138.balance.core.balance

import org.json.JSONArray
import org.json.JSONObject

object LoginDataInspector {
    private val tokenKeys = listOf("token", "access_token", "accessToken", "user_token", "auth_token", "jwt", "userToken", "user_token")
    private val userIdKeys = listOf("uid", "user_id", "userId", "id")

    fun hasLoginData(cookies: String, storage: String): Boolean {
        return cookies.isNotBlank() || findAuthToken(storage) != null
    }

    fun findAuthToken(storage: String, keys: List<String> = tokenKeys): String? {
        return findString(storage, keys, fuzzyName = "token")
    }

    fun findUserId(storage: String): String? {
        return findString(storage, userIdKeys, fuzzyName = null)
    }

    fun storageKeySummary(storage: String): String {
        val root = runCatching { JSONObject(storage) }.getOrNull() ?: return ""
        val localStorage = root.optJSONObject("localStorage")
        val sessionStorage = root.optJSONObject("sessionStorage")
        return listOfNotNull(
            localStorage?.keys()?.asSequence()?.map { "local.$it" }?.toList(),
            sessionStorage?.keys()?.asSequence()?.map { "session.$it" }?.toList(),
        ).flatten().joinToString(limit = 8)
    }

    private fun findString(storage: String, keys: List<String>, fuzzyName: String?): String? {
        val root = runCatching { JSONObject(storage) }.getOrNull() ?: return null
        val containers = listOfNotNull(
            root.optJSONObject("localStorage"),
            root.optJSONObject("sessionStorage"),
        )
        val normalizedKeys = keys.map { it.lowercase() }.toSet()
        containers.forEach { container ->
            findStringInValue(container, normalizedKeys, fuzzyName, depth = 0)?.let { return it }
        }
        return null
    }

    private fun findStringInValue(
        value: Any?,
        keys: Set<String>,
        fuzzyName: String?,
        depth: Int,
    ): String? {
        if (depth > MAX_DEPTH || value == null) return null
        return when (value) {
            is JSONObject -> findStringInObject(value, keys, fuzzyName, depth)
            is JSONArray -> findStringInArray(value, keys, fuzzyName, depth)
            is String -> value.asJsonValue()?.let { findStringInValue(it, keys, fuzzyName, depth + 1) }
            else -> null
        }
    }

    private fun findStringInObject(
        json: JSONObject,
        keys: Set<String>,
        fuzzyName: String?,
        depth: Int,
    ): String? {
        json.keys().forEach { key ->
            val keyName = key.lowercase()
            if (keyName in keys || fuzzyName?.let { keyName.contains(it) } == true) {
                json.optString(key).takeIf { it.isNotBlank() }?.let { return it }
            }
        }
        json.keys().forEach { key ->
            findStringInValue(json.opt(key), keys, fuzzyName, depth + 1)?.let { return it }
        }
        return null
    }

    private fun findStringInArray(
        array: JSONArray,
        keys: Set<String>,
        fuzzyName: String?,
        depth: Int,
    ): String? {
        for (index in 0 until array.length()) {
            findStringInValue(array.opt(index), keys, fuzzyName, depth + 1)?.let { return it }
        }
        return null
    }

    private fun String.asJsonValue(): Any? {
        val trimmed = trim()
        return when {
            trimmed.startsWith("{") -> runCatching { JSONObject(trimmed) }.getOrNull()
            trimmed.startsWith("[") -> runCatching { JSONArray(trimmed) }.getOrNull()
            else -> null
        }
    }

    private const val MAX_DEPTH = 6
}
