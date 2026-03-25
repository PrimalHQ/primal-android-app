/*
 * Persistence for TOFU-pinned ElectrumX certificates.
 *
 * Uses SharedPreferences (consistent with Primal's existing patterns)
 * to store PEM-encoded certificates that the user accepted via
 * the Test Connection flow.
 *
 * SPDX-License-Identifier: MIT
 */
package net.primal.android.namecoin

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Thread-safe store for user-pinned ElectrumX TLS certificates.
 */
@Singleton
class NamecoinCertStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "namecoin_certs",
        Context.MODE_PRIVATE,
    )

    companion object {
        private const val TAG = "NamecoinCertStore"
        private const val KEY_PINNED_CERTS = "pinned_certs"
    }

    /**
     * Store a PEM-encoded certificate that the user accepted via Test Connection.
     * The cert is appended to the existing list (deduped).
     */
    fun addPinnedCert(pem: String) {
        val existing = loadPinnedCerts()
        val updated = (existing + pem).distinct()
        savePinnedCerts(updated)
    }

    /** Load all user-pinned certs from disk. */
    fun loadPinnedCerts(): List<String> {
        return try {
            val certsJson = prefs.getString(KEY_PINNED_CERTS, null)
            if (certsJson != null) {
                json.decodeFromString<List<String>>(certsJson)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading pinned certs: ${e.message}")
            emptyList()
        }
    }

    /** Clear all pinned certs. */
    fun clearPinnedCerts() {
        savePinnedCerts(emptyList())
    }

    private fun savePinnedCerts(certs: List<String>) {
        try {
            prefs.edit()
                .putString(KEY_PINNED_CERTS, json.encodeToString(certs))
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error writing pinned certs: ${e.message}")
        }
    }
}
