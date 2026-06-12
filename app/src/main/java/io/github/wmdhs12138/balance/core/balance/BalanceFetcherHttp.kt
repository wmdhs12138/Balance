package io.github.wmdhs12138.balance.core.balance

internal fun bearerToken(token: String): String {
    return if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
}

internal fun HttpResponse.ensureJsonSuccess(parserName: String, action: String) {
    if (statusCode !in 200..299) {
        throw BalanceFetchException(
            message = failureMessage("$parserName $action failed"),
            reason = failureReason,
        )
    }
    if (looksLikeHtml) {
        throw BalanceFetchException(
            message = "$parserName endpoint returned HTML. Check login status or parser label.",
            reason = BalanceFetchFailureReason.ParserMismatch,
        )
    }
}

internal fun HttpResponse.failureMessage(prefix: String): String {
    val serverMessage = BalanceJsonExtractor.errorMessage(body)
    return if (serverMessage.isNullOrBlank()) {
        "$prefix: HTTP $statusCode"
    } else {
        "$prefix: HTTP $statusCode, $serverMessage"
    }
}
