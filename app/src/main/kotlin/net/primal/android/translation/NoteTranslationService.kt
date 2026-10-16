package net.primal.android.translation

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class NoteTranslationEntry(
    val translated: String,
    val detected: String? = null,
    val showingOriginal: Boolean = false,
)

object NoteTranslationService {
    private val cache = linkedMapOf<String, NoteTranslationEntry>()
    private val mutex = Mutex()
    private val listeners = mutableSetOf<(String) -> Unit>()

    fun subscribe(listener: (String) -> Unit): () -> Unit {
        listeners.add(listener)
        return { listeners.remove(listener) }
    }

    private fun notify(noteId: String) {
        listeners.toList().forEach { it(noteId) }
    }

    fun getCached(noteId: String): NoteTranslationEntry? = cache[noteId]

    suspend fun toggleShowOriginal(noteId: String) {
        mutex.withLock {
            val entry = cache[noteId] ?: return
            cache[noteId] = entry.copy(showingOriginal = !entry.showingOriginal)
        }
        notify(noteId)
    }

    suspend fun translate(noteId: String, content: String, prefs: NoteTranslationPrefs): NoteTranslationEntry {
        cache[noteId]?.let { existing ->
            if (!existing.showingOriginal) return existing
            mutex.withLock {
                cache[noteId] = existing.copy(showingOriginal = false)
            }
            notify(noteId)
            return cache[noteId]!!
        }

        when (prefs.provider) {
            TranslationProvider.Google, TranslationProvider.DeepL -> {
                if (prefs.apiKey.isBlank()) {
                    error("${prefs.provider.name} requires a user-owned API key in Settings → Translation")
                }
            }
            TranslationProvider.LibreTranslate -> Unit
        }

        val sanitized = TranslationSanitizer.sanitizeForTranslation(content)
        val result = withContext(Dispatchers.IO) {
            when (prefs.provider) {
                TranslationProvider.LibreTranslate -> callLibre(sanitized.content, prefs)
                TranslationProvider.Google -> callGoogle(sanitized.content, prefs)
                TranslationProvider.DeepL -> callDeepL(sanitized.content, prefs)
            }
        }
        val restored = TranslationSanitizer.restoreTranslationContent(result.text, sanitized.placeholders)
        val entry = NoteTranslationEntry(
            translated = restored,
            detected = result.detected,
            showingOriginal = false,
        )
        mutex.withLock { cache[noteId] = entry }
        notify(noteId)
        return entry
    }

    private data class ProviderResult(val text: String, val detected: String? = null)

    private fun callLibre(text: String, prefs: NoteTranslationPrefs): ProviderResult {
        val base = NoteTranslationPrefsStore.normalizeLibreBaseUrl(prefs.libreBaseUrl)
        val body = JSONObject()
            .put("q", text)
            .put("source", "auto")
            .put("target", prefs.targetLang.ifBlank { "en" })
            .put("format", "text")
        if (prefs.apiKey.isNotBlank()) body.put("api_key", prefs.apiKey)
        val json = postJson("$base/translate", body.toString())
        val detected = json.optJSONObject("detectedLanguage")?.optString("language")
            ?.takeIf { it.isNotBlank() }
            ?: json.optString("detectedSourceLanguage").takeIf { it.isNotBlank() }
        return ProviderResult(
            text = json.optString("translatedText"),
            detected = detected,
        )
    }

    private fun callGoogle(text: String, prefs: NoteTranslationPrefs): ProviderResult {
        val url =
            "https://translation.googleapis.com/language/translate/v2?key=${URLEncoder.encode(prefs.apiKey, "UTF-8")}"
        val body = JSONObject()
            .put("q", text)
            .put("target", prefs.targetLang.ifBlank { "en" })
            .put("format", "text")
        val json = postJson(url, body.toString())
        val translation = json.getJSONObject("data")
            .getJSONArray("translations")
            .getJSONObject(0)
        return ProviderResult(
            text = translation.getString("translatedText"),
            detected = translation.optString("detectedSourceLanguage").takeIf { it.isNotBlank() },
        )
    }

    private fun callDeepL(text: String, prefs: NoteTranslationPrefs): ProviderResult {
        val isFree = prefs.apiKey.endsWith(":fx")
        val host = if (isFree) "https://api-free.deepl.com" else "https://api.deepl.com"
        val form = buildString {
            append("text=").append(URLEncoder.encode(text, "UTF-8"))
            append("&target_lang=").append(
                URLEncoder.encode(prefs.targetLang.uppercase(Locale.ROOT), "UTF-8"),
            )
        }
        val conn = (URL("$host/v2/translate").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Authorization", "DeepL-Auth-Key ${prefs.apiKey}")
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connectTimeout = 20_000
            readTimeout = 30_000
        }
        conn.outputStream.use { it.write(form.toByteArray(StandardCharsets.UTF_8)) }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val response = stream.bufferedReader().use { it.readText() }
        if (code !in 200..299) error("DeepL HTTP $code: ${response.take(180)}")
        val json = JSONObject(response)
        val translation = json.getJSONArray("translations").getJSONObject(0)
        return ProviderResult(
            text = translation.getString("text"),
            detected = translation.optString("detected_source_language").takeIf { it.isNotBlank() },
        )
    }

    private fun postJson(url: String, body: String): JSONObject {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connectTimeout = 20_000
            readTimeout = 30_000
        }
        conn.outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val response = stream.bufferedReader().use { it.readText() }
        if (code !in 200..299) error("Translation HTTP $code: ${response.take(180)}")
        return JSONObject(response)
    }
}
