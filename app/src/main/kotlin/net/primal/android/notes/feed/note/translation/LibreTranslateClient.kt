package net.primal.android.notes.feed.note.translation

import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Minimal LibreTranslate client (self-hosted or public instance).
 * Default base URL can be overridden for privacy / reliability.
 */
class LibreTranslateClient(
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val apiKey: String? = null,
    private val client: OkHttpClient = defaultClient(),
) {
    fun translate(text: String, targetLang: String, sourceLang: String = "auto"): String {
        val root = JSONObject()
            .put("q", text)
            .put("source", sourceLang)
            .put("target", targetLang)
            .put("format", "text")
        if (!apiKey.isNullOrBlank()) {
            root.put("api_key", apiKey)
        }

        val url = baseUrl.trimEnd('/') + "/translate"
        val body = root.toString().toRequestBody(JSON_MEDIA)
        val request = Request.Builder().url(url).post(body).build()
        client.newCall(request).execute().use { response ->
            val payload = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException("LibreTranslate HTTP ${response.code}: $payload")
            }
            val json = JSONObject(payload)
            return json.optString("translatedText").ifBlank {
                throw IOException("LibreTranslate missing translatedText")
            }
        }
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://libretranslate.com"
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

        fun defaultClient(): OkHttpClient =
            OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(45, TimeUnit.SECONDS)
                .build()

        fun deviceLanguageCode(): String {
            val lang = Locale.getDefault().language
            return if (lang.isNullOrBlank()) "en" else lang
        }
    }
}
