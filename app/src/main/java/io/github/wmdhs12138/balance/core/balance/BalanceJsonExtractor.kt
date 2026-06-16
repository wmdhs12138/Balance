package io.github.wmdhs12138.balance.core.balance

import org.json.JSONArray
import org.json.JSONObject

/** BalanceJsonExtractor 单例对象。 */
object BalanceJsonExtractor {
    /** 展开接口 data 包装 方法。 */
    fun unwrapData(body: String): JSONObject {
        val root = JSONObject(body)
        return root.optJSONObject("data") ?: root
    }

    /** 提取错误信息 方法。 */
    fun errorMessage(body: String): String? {
        val root = runCatching { JSONObject(body) }.getOrNull() ?: return null
        return findString(
            root,
            listOf("message", "msg", "error", "error_message", "errorMessage", "detail", "reason"),
        )
    }

    /** 查找数值字段 方法。 */
    fun findNumber(json: JSONObject, keys: Collection<String>): Double? {
        return findNumber(json, keys.map { it.lowercase() }.toSet(), 0)
    }

    /** 查找文本字段 方法。 */
    fun findString(json: JSONObject, keys: Collection<String>): String? {
        return findString(json, keys.map { it.lowercase() }.toSet(), 0)
    }

    /** 处理findMatchedNumberKey 方法。 */
    fun findMatchedNumberKey(json: JSONObject, keys: Collection<String>): String? {
        return findMatchedNumberKey(json, keys.map { it.lowercase() }.toSet(), 0)
    }

    /** 列出响应字段名 方法。 */
    fun fieldNames(json: JSONObject): String {
        return json.keys().asSequence().joinToString(limit = 16)
    }

    /** 查找数值字段 方法。 */
    private fun findNumber(value: Any?, keys: Set<String>, depth: Int): Double? {
        if (depth > MAX_DEPTH || value == null) return null
        return when (value) {
            is JSONObject -> findNumberInObject(value, keys, depth)
            is JSONArray -> findNumberInArray(value, keys, depth)
            else -> null
        }
    }

    /** 处理findNumberInObject 方法。 */
    private fun findNumberInObject(json: JSONObject, keys: Set<String>, depth: Int): Double? {
        json.keys().forEach { key ->
            if (key.lowercase() in keys) {
                json.opt(key).toBalanceNumber()?.let { return it }
            }
        }
        json.keys().forEach { key ->
            findNumber(json.opt(key), keys, depth + 1)?.let { return it }
        }
        return null
    }

    /** 处理findNumberInArray 方法。 */
    private fun findNumberInArray(array: JSONArray, keys: Set<String>, depth: Int): Double? {
        for (index in 0 until array.length()) {
            findNumber(array.opt(index), keys, depth + 1)?.let { return it }
        }
        return null
    }

    /** 处理findMatchedNumberKey 方法。 */
    private fun findMatchedNumberKey(value: Any?, keys: Set<String>, depth: Int): String? {
        if (depth > MAX_DEPTH || value == null) return null
        return when (value) {
            is JSONObject -> {
                value.keys().forEach { key ->
                    if (key.lowercase() in keys && value.opt(key).toBalanceNumber() != null) {
                        return key
                    }
                }
                value.keys().forEach { key ->
                    findMatchedNumberKey(value.opt(key), keys, depth + 1)?.let { return it }
                }
                null
            }
            is JSONArray -> {
                for (index in 0 until value.length()) {
                    findMatchedNumberKey(value.opt(index), keys, depth + 1)?.let { return it }
                }
                null
            }
            else -> null
        }
    }

    /** 查找文本字段 方法。 */
    private fun findString(value: Any?, keys: Set<String>, depth: Int): String? {
        if (depth > MAX_DEPTH || value == null) return null
        return when (value) {
            is JSONObject -> findStringInObject(value, keys, depth)
            is JSONArray -> findStringInArray(value, keys, depth)
            else -> null
        }
    }

    /** 处理findStringInObject 方法。 */
    private fun findStringInObject(json: JSONObject, keys: Set<String>, depth: Int): String? {
        json.keys().forEach { key ->
            if (key.lowercase() in keys) {
                val value = json.opt(key)
                if (value is String && value.isNotBlank()) {
                    return value
                }
            }
        }
        json.keys().forEach { key ->
            findString(json.opt(key), keys, depth + 1)?.let { return it }
        }
        return null
    }

    /** 处理findStringInArray 方法。 */
    private fun findStringInArray(array: JSONArray, keys: Set<String>, depth: Int): String? {
        for (index in 0 until array.length()) {
            findString(array.opt(index), keys, depth + 1)?.let { return it }
        }
        return null
    }

    private fun Any?.toBalanceNumber(): Double? {
        return when (this) {
            is Number -> toDouble()
            is String -> normalizeNumberString().toDoubleOrNull()
            else -> null
        }
    }

    /** 处理normalizeNumberString 方法。 */
    private fun String.normalizeNumberString(): String {
        val cleaned = trim()
            .replace(",", "")
            .replace("¥", "")
            .replace("￥", "")
            .replace("$", "")
            .trim()
        return NUMBER_PATTERN.find(cleaned)?.value ?: cleaned
    }

    /** 处理MAX_DEPTH 常量。 */
    private const val MAX_DEPTH = 5
    private val NUMBER_PATTERN = Regex("[-+]?\\d+(?:\\.\\d+)?")
}
