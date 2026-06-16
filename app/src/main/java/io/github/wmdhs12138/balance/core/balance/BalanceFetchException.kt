package io.github.wmdhs12138.balance.core.balance

/** BalanceFetchException 类。 */
class BalanceFetchException(
    message: String,
    val reason: BalanceFetchFailureReason = BalanceFetchFailureReason.Unknown,
    cause: Throwable? = null,
) : Exception(message, cause)

/** BalanceFetchFailureReason 枚举。 */
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
