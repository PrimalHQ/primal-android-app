package net.primal.android.core.logging

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingAntilog @Inject constructor(
    private val preferences: AppLogPreferences,
    private val recorder: AppLogRecorder,
) : Antilog(), AppLogController {

    private val _loggingEnabled = AtomicBoolean(preferences.isLoggingEnabled)

    override val loggingEnabled: Boolean
        get() = _loggingEnabled.get()

    override fun setLoggingEnabled(enabled: Boolean) {
        _loggingEnabled.set(enabled)
        preferences.setLoggingEnabled(enabled)
    }

    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
    ) {
        if (!_loggingEnabled.get()) return
        recorder.writeLog(priority, tag, throwable, message)
    }
}
