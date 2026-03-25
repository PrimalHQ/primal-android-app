/*
 * Persistent storage for Namecoin settings via SharedPreferences.
 *
 * Follows the same pattern as Primal's AppLogPreferences — simple
 * SharedPreferences with synchronous reads and a StateFlow for
 * reactive observation.
 *
 * SPDX-License-Identifier: MIT
 */
package net.primal.android.namecoin

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.primal.android.namecoin.electrumx.ElectrumxServer

/**
 * Persistent storage for [NamecoinSettings].
 *
 * Uses SharedPreferences (like [net.primal.android.core.logging.AppLogPreferences])
 * so Namecoin resolution settings are global — not per-account.
 *
 * The current settings are available synchronously via [current] and
 * reactively via [settings] StateFlow.
 */
@Singleton
class NamecoinPreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    private val _settings = MutableStateFlow(loadFromDisk())
    val settings: StateFlow<NamecoinSettings> = _settings

    /** Synchronous snapshot — safe to call from `serverListProvider` lambdas. */
    val current: NamecoinSettings get() = _settings.value

    /**
     * Parsed [ElectrumxServer] list from current custom settings, or `null`
     * if the user hasn't configured any (meaning "use defaults").
     */
    val customServersOrNull: List<ElectrumxServer>?
        get() = current.toElectrumxServers()

    // ── Mutators ───────────────────────────────────────────────────────

    fun setEnabled(enabled: Boolean) {
        val updated = current.copy(enabled = enabled)
        persist(updated)
    }

    fun addServer(server: String) {
        if (server.isBlank() || server in current.customServers) return
        val updated = current.copy(customServers = current.customServers + server)
        persist(updated)
    }

    fun removeServer(server: String) {
        val updated = current.copy(customServers = current.customServers - server)
        persist(updated)
    }

    fun reset() {
        persist(NamecoinSettings.DEFAULT)
    }

    // ── Internal ───────────────────────────────────────────────────────

    private fun persist(settings: NamecoinSettings) {
        _settings.value = settings
        prefs.edit(commit = true) {
            putBoolean(KEY_ENABLED, settings.enabled)
            putString(
                KEY_CUSTOM_SERVERS,
                json.encodeToString(settings.customServers.filter { it.isNotBlank() }),
            )
        }
    }

    private fun loadFromDisk(): NamecoinSettings {
        val enabled = prefs.getBoolean(KEY_ENABLED, true)
        val serversJson = prefs.getString(KEY_CUSTOM_SERVERS, null)
        val servers = if (serversJson != null) {
            try {
                json.decodeFromString<List<String>>(serversJson)
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        return NamecoinSettings(enabled = enabled, customServers = servers)
    }

    companion object {
        private const val PREFS_NAME = "namecoin_settings"
        private const val KEY_ENABLED = "namecoin.enabled"
        private const val KEY_CUSTOM_SERVERS = "namecoin.customServers"
    }
}
