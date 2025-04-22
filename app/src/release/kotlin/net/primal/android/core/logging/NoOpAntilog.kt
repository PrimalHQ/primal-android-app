package net.primal.android.core.logging

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel

class NoOpAntilog : Antilog() {
    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
    ) = Unit
}
