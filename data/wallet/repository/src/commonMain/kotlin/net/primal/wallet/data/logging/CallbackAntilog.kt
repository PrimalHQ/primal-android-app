package net.primal.wallet.data.logging

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel

internal class CallbackAntilog(
    private val writer: (LogEntry) -> Unit,
) : Antilog() {

    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
    ) {
        val fullMessage = buildString {
            if (message != null) append(message)
            if (throwable != null) {
                if (isNotEmpty()) append("\n")
                append(throwable.stackTraceToString())
            }
        }
        if (fullMessage.isNotEmpty()) {
            writer(LogEntry(level = priority.name, tag = tag, message = fullMessage))
        }
    }
}
