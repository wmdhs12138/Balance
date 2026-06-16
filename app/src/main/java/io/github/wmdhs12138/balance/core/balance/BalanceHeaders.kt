package io.github.wmdhs12138.balance.core.balance

import io.github.wmdhs12138.balance.core.database.ProviderEntity
import io.github.wmdhs12138.balance.core.net.UrlNormalizer

/** 余额接口请求头构建工具。 */
object BalanceHeaders {
    /** 创建通用 JSON 请求头。 */
    fun json(userAgent: String = BalanceHttpDefaults.USER_AGENT): MutableMap<String, String> = mutableMapOf(
        "Accept" to "application/json",
        "Accept-Language" to "zh-CN,zh;q=0.9,en;q=0.8",
        "Cache-Control" to "no-store",
        "Content-Type" to "application/json",
        "User-Agent" to userAgent,
    )

    /** 创建 Web 登录态请求头。 */
    fun webSession(
        provider: ProviderEntity,
        payload: LoginPayload.WebSession,
        token: String? = null,
        userId: String? = null,
    ): Map<String, String> = json(payload.userAgent.ifBlank { BalanceHttpDefaults.USER_AGENT }).apply {
        if (payload.cookies.isNotBlank()) put("Cookie", payload.cookies)
        put("Referer", payload.url.ifBlank { provider.baseUrl })
        put("Origin", UrlNormalizer.origin(provider.baseUrl) ?: provider.baseUrl)
        if (!token.isNullOrBlank()) put("Authorization", bearerToken(token))
        if (!userId.isNullOrBlank()) put("New-Api-User", userId)
    }

    /** 创建 Bearer Token 请求头。 */
    fun bearer(token: String): Map<String, String> = mapOf(
        "Authorization" to bearerToken(token),
        "Accept" to "application/json",
    )
}
