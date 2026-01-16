package net.primal.android.core.logging

interface AppLogController {
    val loggingEnabled: Boolean
    fun setLoggingEnabled(enabled: Boolean)
}
