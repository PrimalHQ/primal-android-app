package net.primal.android.core.logging

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLogPreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    val isLoggingEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGING_ENABLED, false)

    val developerToolsEnabled: Boolean
        get() = prefs.getBoolean(KEY_DEVELOPER_TOOLS_ENABLED, false)

    fun setLoggingEnabled(enabled: Boolean) {
        prefs.edit(commit = true) {
            putBoolean(KEY_IS_LOGGING_ENABLED, enabled)
        }
    }

    fun setDeveloperToolsEnabled(enabled: Boolean) {
        prefs.edit(commit = true) {
            putBoolean(KEY_DEVELOPER_TOOLS_ENABLED, enabled)
        }
    }

    companion object {
        private const val PREFS_NAME = "dev_tools"
        private const val KEY_IS_LOGGING_ENABLED = "is_logging_enabled"
        private const val KEY_DEVELOPER_TOOLS_ENABLED = "developer_tools_enabled"
    }
}
