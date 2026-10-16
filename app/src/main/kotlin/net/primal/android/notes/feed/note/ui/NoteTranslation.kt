package net.primal.android.notes.feed.note.ui

import android.content.Context
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

// Translation of note content into the user's preferred language.
//
// Uses free, keyless public translation endpoints, so no API key is required:
// the public LibreTranslate instances are tried first, and, if none of them is
// reachable, we fall back to the free Google Translate endpoint
// (translate.googleapis.com), which requires no key either.

enum class TranslationEngine { LIBRETRANSLATE, GOOGLE }

data class TranslationResult(
    val text: String,
    val engine: TranslationEngine,
    val target: String,
)

data class TranslationLanguage(
    val code: String,
    val name: String,
)

// A curated list of the most common target languages.
// Codes are ISO 639-1 (LibreTranslate style); Google-specific variants
// (zh-Hans, nb) are mapped to their Google equivalents when used.
val TRANSLATION_LANGUAGES = listOf(
    TranslationLanguage("en", "English"),
    TranslationLanguage("es", "Spanish"),
    TranslationLanguage("fr", "French"),
    TranslationLanguage("de", "German"),
    TranslationLanguage("it", "Italian"),
    TranslationLanguage("pt", "Portuguese"),
    TranslationLanguage("nl", "Dutch"),
    TranslationLanguage("ru", "Russian"),
    TranslationLanguage("uk", "Ukrainian"),
    TranslationLanguage("pl", "Polish"),
    TranslationLanguage("cs", "Czech"),
    TranslationLanguage("sk", "Slovak"),
    TranslationLanguage("sv", "Swedish"),
    TranslationLanguage("nb", "Norwegian"),
    TranslationLanguage("da", "Danish"),
    TranslationLanguage("fi", "Finnish"),
    TranslationLanguage("el", "Greek"),
    TranslationLanguage("tr", "Turkish"),
    TranslationLanguage("ar", "Arabic"),
    TranslationLanguage("he", "Hebrew"),
    TranslationLanguage("hi", "Hindi"),
    TranslationLanguage("bn", "Bengali"),
    TranslationLanguage("th", "Thai"),
    TranslationLanguage("vi", "Vietnamese"),
    TranslationLanguage("id", "Indonesian"),
    TranslationLanguage("ja", "Japanese"),
    TranslationLanguage("ko", "Korean"),
    TranslationLanguage("zh-Hans", "Chinese (Simplified)"),
)

private const val PREFS_NAME = "translation"
private const val KEY_TRANSLATE_LANG = "translate_lang"

private val LIBRETRANSLATE_INSTANCES = listOf(
    "https://libretranslate.com/translate",
    "https://translate.terraprint.co/translate",
    "https://libretranslate.pussthecat.org/translate",
    "https://lt.vern.cc/translate",
)

private const val GOOGLE_TRANSLATE_URL = "https://translate.googleapis.com/translate_a/single"

// In-memory cache of translated notes: noteId -> last translation result
// (kept together with the target language it was translated into).
private val translationCache = mutableMapOf<String, TranslationResult>()

private fun stripRegion(lang: String) = lang.substringBefore('-').lowercase(Locale.ROOT)

fun defaultTranslateLanguage(): String {
    val deviceLang = stripRegion(Locale.getDefault().language)
    val normalized = when (deviceLang) {
        "zh" -> "zh-Hans"
        "no" -> "nb"
        else -> deviceLang
    }
    return if (TRANSLATION_LANGUAGES.any { it.code == normalized }) normalized else "en"
}

object TranslationPreferences {

    fun getTranslateLanguage(context: Context): String {
        val stored = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TRANSLATE_LANG, null)
        return if (stored != null && TRANSLATION_LANGUAGES.any { it.code == stored }) {
            stored
        } else {
            defaultTranslateLanguage()
        }
    }

    fun setTranslateLanguage(context: Context, language: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TRANSLATE_LANG, language)
            .apply()
    }
}

private fun toGoogleTarget(target: String): String = when (target) {
    "zh-Hans" -> "zh-CN"
    "nb" -> "no"
    else -> target
}

object NoteTranslator {

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    fun getCachedTranslation(noteId: String, target: String): TranslationResult? =
        translationCache[noteId]?.takeIf { it.target == target }

    fun cacheTranslation(noteId: String, result: TranslationResult) {
        translationCache[noteId] = result
    }

    suspend fun translateNoteContent(text: String, target: String): TranslationResult? =
        withContext(Dispatchers.IO) {
            translateViaLibreTranslate(text, target) ?: translateViaGoogle(text, target)
        }

    private fun translateViaLibreTranslate(q: String, target: String): TranslationResult? {
        val body = JSONObject()
            .put("q", q)
            .put("source", "auto")
            .put("target", target)
            .put("format", "text")
            .toString()
            .toRequestBody("application/json".toMediaType())

        for (url in LIBRETRANSLATE_INSTANCES) {
            try {
                val request = Request.Builder().url(url).post(body).build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@use
                    }
                    val json = JSONObject(response.body?.string().orEmpty())
                    val translatedText = json.optString("translatedText")
                    if (translatedText.isNotBlank()) {
                        return TranslationResult(
                            text = translatedText,
                            engine = TranslationEngine.LIBRETRANSLATE,
                            target = target,
                        )
                    }
                }
            } catch (_: Exception) {
                // Instance is unreachable; try the next one.
            }
        }
        return null
    }

    private fun translateViaGoogle(q: String, target: String): TranslationResult? {
        val url = "$GOOGLE_TRANSLATE_URL?client=gtx&sl=auto&tl=${toGoogleTarget(target)}&dt=t&q=${
            URLEncoder.encode(q, "UTF-8")
        }"
        return try {
            val request = Request.Builder().url(url).build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return null
                }
                val json = JSONArray(response.body?.string().orEmpty())
                val translatedText = json.optJSONArray(0)?.optJSONArray(0)?.optString(0).orEmpty()
                if (translatedText.isNotBlank()) {
                    TranslationResult(
                        text = translatedText,
                        engine = TranslationEngine.GOOGLE,
                        target = target,
                    )
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}
