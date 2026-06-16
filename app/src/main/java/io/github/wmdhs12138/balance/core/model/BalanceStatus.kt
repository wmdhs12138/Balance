package io.github.wmdhs12138.balance.core.model

/** BalanceStatus 枚举。 */
enum class BalanceStatus {
    NotConnected,
    NeedsLogin,
    Ready,
    Forbidden,
    NotFound,
    Timeout,
    NetworkError,
    ParserMismatch,
    Failed,
}
