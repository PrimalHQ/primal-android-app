package net.primal.android.notes.translation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

data class NoteTranslateResult(
    val translatedText: String,
    val detectedSourceLanguage: String? = null,
    val provider: String,
)

/**
 * Translate plain note text into the user's preferred language.
 *
 * Primary: unofficial Google Chrome translate endpoint (no API key).
 * Fallback: MyMemory free public API (rate-limited).
 *
 * Unofficial Google endpoints can break without notice; a future official
 * backend-integrated provider would be more durable.
 */
object NoteTranslate {

    private const val GOOGLE_ENDPOINT = "https://clients5.google.com/translate_a/t"
    private const val MYMEMORY_ENDPOINT = "https://api.mymemory.translated.net/get"
    private const val CHUNK_SIZE = 4500

    private val json = Json { ignoreUnknownKeys = true }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().followRedirects(true).build()
    }

    fun normalizeLangCode(locale: String): String {
        val trimmed = locale.trim().lowercase(Locale.ROOT).replace('_', '-')
        if (trimmed.startsWith("zh-tw") || trimmed.startsWith("zh-hk")) return "zh-TW"
        val primary = trimmed.split('-', limit = 2).firstOrNull().orEmpty()
        if (primary == "zh") return "zh-CN"
        return primary.ifBlank { "en" }
    }

    fun resolveTargetLanguage(settingsLocale: String? = null, deviceLocale: Locale = Locale.getDefault()): String {
        val raw = settingsLocale?.trim()?.takeIf { it.isNotEmpty() }
            ?: sequenceOf(deviceLocale.toLanguageTag(), deviceLocale.language)
                .firstOrNull { it.isNotBlank() }
            ?: "en"
        return normalizeLangCode(raw)
    }

    fun languageDisplayName(code: String, displayLocale: Locale = Locale.getDefault()): String {
        val normalized = normalizeLangCode(code)
        val locale = Locale.forLanguageTag(normalized)
        val name = locale.getDisplayLanguage(displayLocale)
        return name.ifBlank { normalized }
    }

    fun buildGoogleTranslateWebUrl(text: String, targetLang: String): String {
        val tl = normalizeLangCode(targetLang)
        return "https://translate.google.com/?sl=auto&tl=$tl&text=${urlEncode(text)}&op=translate"
    }

    suspend fun translateText(
        text: String,
        targetLang: String,
        client: OkHttpClient = httpClient,
    ): NoteTranslateResult = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        require(trimmed.isNotEmpty()) { "Nothing to translate" }
        val target = normalizeLangCode(targetLang)

        try {
            translateWithGoogle(trimmed, target, client)
        } catch (primary: Exception) {
            try {
                translateWithMyMemory(trimmed, target, client)
            } catch (fallback: Exception) {
                throw IllegalStateException(
                    "Translation failed (${primary.message}; fallback: ${fallback.message})",
                    fallback,
                )
            }
        }
    }

    internal fun parseGoogleResponse(body: String): Pair<String, String?> {
        val root = json.parseToJsonElement(body)
        val data = root as? JsonArray ?: error("Unexpected translation response")
        if (data.isEmpty()) error("Unexpected translation response")

        val first = data.first()
        if (first is JsonPrimitive && first.isString) {
            val text = data.joinToString("") { (it as? JsonPrimitive)?.contentOrNull.orEmpty() }
            return text to null
        }

        if (first is JsonArray) {
            val text = buildString {
                data.forEach { element ->
                    val arr = element as? JsonArray ?: return@forEach
                    append((arr.getOrNull(0) as? JsonPrimitive)?.contentOrNull.orEmpty())
                }
            }
            val source = (first.getOrNull(1) as? JsonPrimitive)?.contentOrNull?.ifBlank { null }
            return text to source
        }

        error("Unexpected translation response shape")
    }

    internal fun chunkText(text: String, size: Int = CHUNK_SIZE): List<String> {
        if (text.length <= size) return listOf(text)
        val chunks = mutableListOf<String>()
        var remaining = text
        while (remaining.isNotEmpty()) {
            if (remaining.length <= size) {
                chunks += remaining
                break
            }
            var cut = remaining.lastIndexOf(' ', size)
            if (cut < size / 2) cut = size
            chunks += remaining.substring(0, cut)
            remaining = remaining.substring(cut).trimStart()
        }
        return chunks
    }

    private fun translateWithGoogle(
        text: String,
        targetLang: String,
        client: OkHttpClient,
    ): NoteTranslateResult {
        val parts = mutableListOf<String>()
        var source: String? = null
        for (chunk in chunkText(text)) {
            val url = GOOGLE_ENDPOINT.toHttpUrl().newBuilder()
                .addQueryParameter("client", "dict-chrome-ex")
                .addQueryParameter("sl", "auto")
                .addQueryParameter("tl", targetLang)
                .addQueryParameter("q", chunk)
                .build()
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    error("Google translate HTTP ${response.code}")
                }
                val body = response.body?.string().orEmpty()
                val (translated, detected) = parseGoogleResponse(body)
                parts += translated
                if (source == null && detected != null) source = detected
            }
        }
        return NoteTranslateResult(
            translatedText = parts.joinToString(" "),
            detectedSourceLanguage = source,
            provider = "google-gtx",
        )
    }

    private fun translateWithMyMemory(
        text: String,
        targetLang: String,
        client: OkHttpClient,
    ): NoteTranslateResult {
        val piece = text.take(500)
        val url = MYMEMORY_ENDPOINT.toHttpUrl().newBuilder()
            .addQueryParameter("q", piece)
            .addQueryParameter("langpair", "autodetect|$targetLang")
            .build()
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                error("MyMemory HTTP ${response.code}")
            }
            val body = response.body?.string().orEmpty()
            val root = json.parseToJsonElement(body).jsonObject
            val responseData = root["responseData"] as? JsonObject
            val translated = responseData?.get("translatedText")?.jsonPrimitive?.contentOrNull.orEmpty()
            val status = root["responseStatus"]?.jsonPrimitive?.intOrNull ?: 0
            if (translated.isBlank() || status == 429 || translated.contains("MYMEMORY WARNING", ignoreCase = true)) {
                error(translated.ifBlank { "MyMemory quota exceeded" })
            }
            val detected = responseData?.get("detectedLanguage")?.jsonPrimitive?.contentOrNull?.ifBlank { null }
            return NoteTranslateResult(
                translatedText = translated,
                detectedSourceLanguage = detected,
                provider = "mymemory",
            )
        }
    }

    private fun urlEncode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name())
}
