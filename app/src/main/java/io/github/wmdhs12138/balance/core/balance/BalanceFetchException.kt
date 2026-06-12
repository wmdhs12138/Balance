package io.github.wmdhs12138.balance.core.balance

class BalanceFetchException(
    message: String,
    val reason: BalanceFetchFailureReason = BalanceFetchFailureReason.Unknown,
    cause: Throwable? = null,
) : Exception(message, cause)

enum class BalanceFetchFailureReason {
    NeedsLogin,
    Forbidden,
    NotFound,
    Timeout,
    Network,
    ParserMismatch,
    InvalidResponse,
    Unsupported,
    Unknown,
}
