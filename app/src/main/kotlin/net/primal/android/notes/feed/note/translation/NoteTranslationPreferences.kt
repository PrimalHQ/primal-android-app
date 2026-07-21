package net.primal.android.notes.feed.note.translation

import android.content.Context
import java.util.Locale

/**
 * Local preferences for LibreTranslate fallback (endpoint, API key, target language).
 * Kept on-device via SharedPreferences so settings do not require account sync.
 */
object NoteTranslationPreferences {
    private const val PREFS = "primal_note_translation"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_BASE_URL = "base_url"
    private const val KEY_API_KEY = "api_key"
    private const val KEY_TARGET_LANG = "target_lang"

    fun isEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENABLED, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun baseUrl(context: Context): String =
        prefs(context).getString(KEY_BASE_URL, null)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: LibreTranslateClient.DEFAULT_BASE_URL

    fun setBaseUrl(context: Context, url: String) {
        val cleaned = url.trim().trimEnd('/')
        if (cleaned.isEmpty()) {
            prefs(context).edit().remove(KEY_BASE_URL).apply()
        } else {
            prefs(context).edit().putString(KEY_BASE_URL, cleaned).apply()
        }
    }

    fun apiKey(context: Context): String? =
        prefs(context).getString(KEY_API_KEY, null)?.takeIf { it.isNotBlank() }

    fun setApiKey(context: Context, key: String) {
        val cleaned = key.trim()
        if (cleaned.isEmpty()) {
            prefs(context).edit().remove(KEY_API_KEY).apply()
        } else {
            prefs(context).edit().putString(KEY_API_KEY, cleaned).apply()
        }
    }

    fun targetLanguage(context: Context): String {
        val stored = prefs(context).getString(KEY_TARGET_LANG, null)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        if (stored != null) {
            return normalizeLang(stored)
        }
        return LibreTranslateClient.deviceLanguageCode()
    }

    fun setTargetLanguage(context: Context, language: String) {
        val code = normalizeLang(language)
        if (code.isEmpty()) {
            prefs(context).edit().remove(KEY_TARGET_LANG).apply()
        } else {
            prefs(context).edit().putString(KEY_TARGET_LANG, code).apply()
        }
    }

    fun normalizeLang(raw: String): String {
        val first = raw.trim().lowercase(Locale.US).split('-', '_').firstOrNull().orEmpty()
        return first.filter { it.isLetter() }.take(8)
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
