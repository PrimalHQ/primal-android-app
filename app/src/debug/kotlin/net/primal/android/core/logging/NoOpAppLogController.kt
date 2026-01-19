package net.primal.android.core.logging

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpAppLogController @Inject constructor() : AppLogController {

    override val loggingEnabled: Boolean = true

    override fun setLoggingEnabled(enabled: Boolean) {
        // No-op in debug builds - logging is only available in release builds
    }
}
