package net.primal.android.notes.feed.note.translation

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.security.MessageDigest
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class NoteTranslationSettings(
    val enabled: Boolean = true,
    val endpoint: String = NoteTranslationRepository.DEFAULT_ENDPOINT,
    val apiKey: String = "",
    val targetLanguage: String = NoteTranslationRepository.defaultTargetLanguage(),
)

data class NoteTranslation(
    val translatedText: String,
    val detectedLanguage: String?,
    val fromCache: Boolean,
)

@Singleton
class NoteTranslationRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSettings(): NoteTranslationSettings {
        return NoteTranslationSettings(
            enabled = prefs.getBoolean(KEY_ENABLED, true),
            endpoint = prefs.getString(KEY_ENDPOINT, DEFAULT_ENDPOINT)?.ifBlank { DEFAULT_ENDPOINT } ?: DEFAULT_ENDPOINT,
            apiKey = prefs.getString(KEY_API_KEY, "").orEmpty(),
            targetLanguage = prefs.getString(KEY_TARGET_LANGUAGE, defaultTargetLanguage())
                ?.ifBlank { defaultTargetLanguage() }
                ?: defaultTargetLanguage(),
        )
    }

    fun saveSettings(settings: NoteTranslationSettings) {
        prefs.edit(commit = true) {
            putBoolean(KEY_ENABLED, settings.enabled)
            putString(KEY_ENDPOINT, settings.endpoint.trim().ifBlank { DEFAULT_ENDPOINT })
            putString(KEY_API_KEY, settings.apiKey.trim())
            putString(KEY_TARGET_LANGUAGE, settings.targetLanguage.trim().ifBlank { defaultTargetLanguage() })
        }
    }

    fun restoreDefaults(): NoteTranslationSettings {
        val defaults = NoteTranslationSettings()
        saveSettings(defaults)
        return defaults
    }

    fun translate(sourceText: String): Result<NoteTranslation> = runCatching {
        val settings = getSettings()
        require(settings.enabled) { "Translation is disabled." }

        val targetLanguage = settings.targetLanguage.trim().lowercase(Locale.US)
        require(targetLanguage.isNotBlank()) { "Target language is not configured." }

        val protectedText = protectEntities(sourceText)
        val cacheKey = buildCacheKey(
            endpoint = settings.endpoint,
            targetLanguage = targetLanguage,
            sourceText = protectedText.text,
        )

        prefs.getString(cacheKey, null)?.let { cached ->
            val json = JSONObject(cached)
            return@runCatching NoteTranslation(
                translatedText = json.getString(JSON_TRANSLATED_TEXT),
                detectedLanguage = json.optString(JSON_DETECTED_LANGUAGE).takeIf { it.isNotBlank() },
                fromCache = true,
            )
        }

        val responseJson = requestTranslation(
            settings = settings,
            sourceText = protectedText.text,
            targetLanguage = targetLanguage,
        )
        val translatedText = protectedText.restore(responseJson.getString("translatedText"))
        val detectedLanguage = responseJson.detectedLanguage()

        prefs.edit(commit = true) {
            putString(
                cacheKey,
                JSONObject()
                    .put(JSON_TRANSLATED_TEXT, translatedText)
                    .put(JSON_DETECTED_LANGUAGE, detectedLanguage ?: "")
                    .toString(),
            )
        }

        NoteTranslation(
            translatedText = translatedText,
            detectedLanguage = detectedLanguage,
            fromCache = false,
        )
    }

    private fun requestTranslation(
        settings: NoteTranslationSettings,
        sourceText: String,
        targetLanguage: String,
    ): JSONObject {
        val body = FormBody.Builder()
            .add("q", sourceText)
            .add("source", "auto")
            .add("target", targetLanguage)
            .apply {
                settings.apiKey.trim().takeIf { it.isNotBlank() }?.let { apiKey ->
                    add("api_key", apiKey)
                }
            }
            .build()

        val request = Request.Builder()
            .url(settings.endpoint.trim())
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException("Translation service returned HTTP ${response.code}.")
            }
            return JSONObject(responseBody)
        }
    }

    private fun buildCacheKey(endpoint: String, targetLanguage: String, sourceText: String): String {
        return "$CACHE_PREFIX${sha256("$endpoint\n$targetLanguage\n$sourceText")}"
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString(separator = "") { "%02x".format(it) }
    }

    private fun JSONObject.detectedLanguage(): String? {
        val detected = opt("detectedLanguage") ?: return null
        return when (detected) {
            is JSONObject -> detected.optString("language").takeIf { it.isNotBlank() }
            is String -> detected.takeIf { it.isNotBlank() }
            else -> null
        }
    }

    private fun protectEntities(text: String): ProtectedText {
        val replacements = linkedMapOf<String, String>()
        var protected = text

        PROTECTED_ENTITY_REGEX.findAll(text)
            .map { it.value.trimEnd('.', ',', ';', ':', ')', ']') }
            .distinct()
            .forEachIndexed { index, entity ->
                val placeholder = "__PRIMAL_TRANSLATION_ENTITY_${index}__"
                replacements[placeholder] = entity
                protected = protected.replace(entity, placeholder)
            }

        return ProtectedText(text = protected, replacements = replacements)
    }

    private data class ProtectedText(
        val text: String,
        val replacements: Map<String, String>,
    ) {
        fun restore(value: String): String {
            var restored = value
            replacements.forEach { (placeholder, entity) ->
                restored = restored.replace(placeholder, entity)
            }
            return restored
        }
    }

    companion object {
        const val DEFAULT_ENDPOINT = "https://libretranslate.com/translate"

        private const val PREFS_NAME = "note_translation"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_ENDPOINT = "endpoint"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_TARGET_LANGUAGE = "target_language"
        private const val CACHE_PREFIX = "cache_"
        private const val JSON_TRANSLATED_TEXT = "translatedText"
        private const val JSON_DETECTED_LANGUAGE = "detectedLanguage"

        private val client = OkHttpClient.Builder().build()

        private val PROTECTED_ENTITY_REGEX = Regex(
            pattern = """https?://\S+|nostr:\S+|\b(?:lnbc|bc1|npub1|note1|nevent1|nprofile1|naddr1)[a-zA-Z0-9]+\b|[#@][\p{L}\p{N}_.-]+""",
        )

        fun defaultTargetLanguage(): String {
            return Locale.getDefault().language
                .takeIf { it.isNotBlank() }
                ?.lowercase(Locale.US)
                ?: "en"
        }
    }
}
