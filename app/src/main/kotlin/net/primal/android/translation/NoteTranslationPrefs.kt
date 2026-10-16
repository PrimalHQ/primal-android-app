package net.primal.android.translation

import android.content.Context
import java.util.Locale

enum class TranslationProvider {
    LibreTranslate,
    Google,
    DeepL,
}

data class NoteTranslationPrefs(
    val enabled: Boolean = true,
    val provider: TranslationProvider = TranslationProvider.LibreTranslate,
    val targetLang: String = Locale.getDefault().language.ifBlank { "en" },
    val libreBaseUrl: String = "https://libretranslate.com",
    val apiKey: String = "",
)

object NoteTranslationPrefsStore {
    private const val PREFS_NAME = "primal.noteTranslation.v1"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_PROVIDER = "provider"
    private const val KEY_TARGET = "targetLang"
    private const val KEY_LIBRE = "libreBaseUrl"
    private const val KEY_API = "apiKey"

    fun read(context: Context): NoteTranslationPrefs {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val provider = when (prefs.getString(KEY_PROVIDER, TranslationProvider.LibreTranslate.name)) {
            TranslationProvider.Google.name -> TranslationProvider.Google
            TranslationProvider.DeepL.name -> TranslationProvider.DeepL
            else -> TranslationProvider.LibreTranslate
        }
        return NoteTranslationPrefs(
            enabled = prefs.getBoolean(KEY_ENABLED, true),
            provider = provider,
            targetLang = prefs.getString(KEY_TARGET, null)
                ?: Locale.getDefault().language.ifBlank { "en" },
            libreBaseUrl = prefs.getString(KEY_LIBRE, null) ?: "https://libretranslate.com",
            apiKey = prefs.getString(KEY_API, "") ?: "",
        )
    }

    fun write(context: Context, update: NoteTranslationPrefs) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_ENABLED, update.enabled)
            .putString(KEY_PROVIDER, update.provider.name)
            .putString(KEY_TARGET, update.targetLang)
            .putString(KEY_LIBRE, update.libreBaseUrl)
            .putString(KEY_API, update.apiKey)
            .apply()
    }

    fun normalizeLibreBaseUrl(url: String): String {
        val trimmed = url.trim().trimEnd('/')
        return trimmed.ifBlank { "https://libretranslate.com" }
    }
}
