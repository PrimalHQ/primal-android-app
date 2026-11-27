package net.primal.android.notes.translation

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Local persistence for [TranslationSettings], following the pattern of
 * net.primal.android.core.logging.AppLogPreferences.
 *
 * These settings deliberately live only on the device (unlike the synced
 * app settings): the endpoint is the user's own server and the optional API
 * key must never be uploaded anywhere.
 */
@Singleton
class TranslationSettingsStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    // Non-secret settings in regular private preferences.
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    // The API key lives in EncryptedSharedPreferences (Jetpack Security)
    // only — never in plaintext. If the keystore is unavailable on a
    // device this is null and the key simply is not persisted (fail
    // closed): it then lasts for the app session via the settings flow,
    // and the user re-enters it after a restart.
    private val securePrefs: SharedPreferences? = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    } catch (e: Exception) {
        null
    }

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<TranslationSettings> = _settingsFlow.asStateFlow()

    val settings: TranslationSettings
        get() = _settingsFlow.value

    fun setEnabled(enabled: Boolean) = update { it.copy(enabled = enabled) }

    fun setEndpoint(endpoint: String) = update { it.copy(endpoint = endpoint) }

    fun setApiKey(apiKey: String) = update { it.copy(apiKey = apiKey) }

    fun setTargetLanguage(targetLanguage: String) = update { it.copy(targetLanguage = targetLanguage) }

    private fun update(reducer: (TranslationSettings) -> TranslationSettings) {
        val newSettings = reducer(_settingsFlow.value)
        prefs.edit(commit = true) {
            putBoolean(KEY_ENABLED, newSettings.enabled)
            putString(KEY_ENDPOINT, newSettings.endpoint)
            putString(KEY_TARGET_LANGUAGE, newSettings.targetLanguage)
        }
        securePrefs?.let { secure ->
            try {
                secure.edit(commit = true) {
                    putString(KEY_API_KEY, newSettings.apiKey)
                }
            } catch (e: Exception) {
                // Keystore became unavailable after init: the key simply is
                // not persisted (fail closed), the session value still
                // applies via the settings flow.
            }
        }
        _settingsFlow.value = newSettings
    }

    private fun readApiKey(): String {
        val secure = securePrefs ?: return ""
        return try {
            secure.getString(KEY_API_KEY, null) ?: ""
        } catch (e: Exception) {
            // Keystore became unavailable after init: behave as if no key
            // is stored rather than crashing settings load.
            ""
        }
    }

    private fun loadSettings(): TranslationSettings {
        return TranslationSettings(
            enabled = prefs.getBoolean(KEY_ENABLED, false),
            endpoint = prefs.getString(KEY_ENDPOINT, null) ?: "",
            apiKey = readApiKey(),
            targetLanguage = prefs.getString(KEY_TARGET_LANGUAGE, null) ?: "",
        )
    }

    companion object {
        private const val PREFS_NAME = "note_translation"
        private const val ENCRYPTED_PREFS_NAME = "note_translation_encrypted"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_ENDPOINT = "endpoint"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_TARGET_LANGUAGE = "target_language"
    }
}
