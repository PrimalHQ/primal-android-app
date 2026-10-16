package net.primal.android.notes.translation.remote

import java.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.primal.android.notes.translation.NetworkTranslationRoute
import net.primal.android.notes.translation.NoteTranslationConfig
import net.primal.core.utils.getOrElse
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.CommonJsonImplicitNulls
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

fun interface RemoteNoteTranslator {
    suspend fun translate(
        route: NetworkTranslationRoute,
        text: String,
        targetLanguage: String,
    ): String
}

class UserOwnedTranslationClient(
    private val httpClient: OkHttpClient,
) : RemoteNoteTranslator {
    override suspend fun translate(
        route: NetworkTranslationRoute,
        text: String,
        targetLanguage: String,
    ): String {
        val translated = when (route) {
            is NetworkTranslationRoute.LibreTranslate -> translateLibre(
                route = route,
                text = text,
                targetLanguage = targetLanguage,
            )

            is NetworkTranslationRoute.DeepL -> translateDeepL(
                route = route,
                text = text,
                targetLanguage = targetLanguage,
            )

            is NetworkTranslationRoute.Google -> translateGoogle(
                route = route,
                text = text,
                targetLanguage = targetLanguage,
            )
        }
        if (translated.isBlank()) {
            throw IOException("Translation provider returned empty text.")
        }
        return translated
    }

    private fun translateLibre(
        route: NetworkTranslationRoute.LibreTranslate,
        text: String,
        targetLanguage: String,
    ): String {
        val body = buildJsonObject {
            put("q", text)
            put("source", "auto")
            put("target", targetLanguage)
            put("format", "text")
            val apiKey = route.apiKey
            if (!apiKey.isNullOrBlank()) {
                put("api_key", apiKey)
            }
        }
        val payload = postJson(url = "${route.endpoint}/translate", json = body.toString())
        val parsed = decode<LibreTranslateResponse>(payload)
        return parsed.translatedText?.ifBlank { null }
            ?: parsed.translation?.ifBlank { null }
            ?: parsed.text.orEmpty()
    }

    private fun translateDeepL(
        route: NetworkTranslationRoute.DeepL,
        text: String,
        targetLanguage: String,
    ): String {
        val body = buildJsonObject {
            putJsonArray("text") { add(kotlinx.serialization.json.JsonPrimitive(text)) }
            put("target_lang", toDeepLLanguage(targetLanguage))
        }
        val payload = postJson(
            url = route.endpoint,
            json = body.toString(),
            headers = mapOf("Authorization" to "DeepL-Auth-Key ${route.apiKey}"),
        )
        return decode<DeepLResponse>(payload).translations?.firstOrNull()?.text.orEmpty()
    }

    private fun translateGoogle(
        route: NetworkTranslationRoute.Google,
        text: String,
        targetLanguage: String,
    ): String {
        val body = CommonJsonImplicitNulls.encodeToString(
            GoogleTranslateRequest(
                q = listOf(text),
                target = targetLanguage,
                format = "text",
            ),
        )
        val payload = postJson(
            url = NoteTranslationConfig.GOOGLE_TRANSLATE_ENDPOINT,
            json = body,
            query = mapOf("key" to route.apiKey),
        )
        return decode<GoogleTranslateResponse>(payload).data?.translations?.firstOrNull()?.translatedText.orEmpty()
    }

    private fun postJson(
        url: String,
        json: String,
        headers: Map<String, String> = emptyMap(),
        query: Map<String, String> = emptyMap(),
    ): String {
        val httpUrl = url.toHttpUrl().newBuilder().apply {
            query.forEach { (name, value) -> addQueryParameter(name, value) }
        }.build()
        val request = Request.Builder()
            .url(httpUrl)
            .apply { headers.forEach { (name, value) -> header(name, value) } }
            .post(json.toRequestBody(JSON_MEDIA))
            .build()
        httpClient.newCall(request).execute().use { response ->
            val payload = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException("Translation HTTP ${response.code}")
            }
            return payload
        }
    }

    private inline fun <reified T> decode(payload: String): T {
        return runCatching { CommonJson.decodeFromString<T>(payload) }.getOrElse {
            throw IOException("Unable to parse translation response.")
        }
    }

    companion object {
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }
}

internal fun toDeepLLanguage(code: String): String {
    val normalized = code.trim()
    return when {
        normalized.equals("zh-TW", ignoreCase = true) ||
            normalized.equals("zh-Hant", ignoreCase = true) -> "ZH-HANT"
        normalized.equals("zh", ignoreCase = true) ||
            normalized.equals("zh-CN", ignoreCase = true) ||
            normalized.equals("zh-Hans", ignoreCase = true) -> "ZH-HANS"
        normalized.equals("pt-PT", ignoreCase = true) -> "PT-PT"
        normalized.equals("pt", ignoreCase = true) ||
            normalized.equals("pt-BR", ignoreCase = true) -> "PT-BR"
        normalized.equals("en-GB", ignoreCase = true) -> "EN-GB"
        normalized.equals("en", ignoreCase = true) ||
            normalized.equals("en-US", ignoreCase = true) -> "EN"
        else -> normalized.uppercase()
    }
}

@Serializable
private data class LibreTranslateResponse(
    val translatedText: String? = null,
    val translation: String? = null,
    val text: String? = null,
)

@Serializable
private data class DeepLResponse(
    val translations: List<DeepLTranslation>? = null,
)

@Serializable
private data class DeepLTranslation(
    val text: String? = null,
)

@Serializable
private data class GoogleTranslateRequest(
    val q: List<String>,
    val target: String,
    val format: String,
)

@Serializable
private data class GoogleTranslateResponse(
    val data: GoogleTranslateData? = null,
)

@Serializable
private data class GoogleTranslateData(
    val translations: List<GoogleTranslation>? = null,
)

@Serializable
private data class GoogleTranslation(
    val translatedText: String? = null,
)
