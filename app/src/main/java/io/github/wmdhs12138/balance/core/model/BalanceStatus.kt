package io.github.wmdhs12138.balance.core.model

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
