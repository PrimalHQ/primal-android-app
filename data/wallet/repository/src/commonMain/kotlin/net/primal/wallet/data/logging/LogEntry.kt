package net.primal.wallet.data.logging

data class LogEntry(
    val level: String,
    val tag: String?,
    val message: String,
)
