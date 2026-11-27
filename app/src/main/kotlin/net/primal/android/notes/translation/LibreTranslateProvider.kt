package net.primal.android.notes.translation

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * [TranslationProvider] for LibreTranslate-compatible endpoints
 * (POST /translate, GET /languages), e.g. a self-hosted LibreTranslate
 * instance or https://libretranslate.com.
 *
 * The HTTP client is supplied by the caller (one shared instance for the
 * whole feature) and is created without the app's default configuration on
 * purpose: the default config logs request bodies, and requests to the
 * translation endpoint can carry the user's API key.
 */
class LibreTranslateProvider(
    private val settings: TranslationSettings,
    private val client: HttpClient,
) : TranslationProvider {

    private val baseUrl: String = TranslationEndpoints.normalizeEndpoint(settings.endpoint)

    override suspend fun translate(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
    ): ProviderTranslation {
        val payload = buildJsonObject {
            put("q", text)
            put("source", sourceLanguage)
            put("target", targetLanguage)
            put("format", "text")
            if (settings.apiKey.isNotBlank()) {
                put("api_key", settings.apiKey)
            }
        }

        val response = client.post("$baseUrl/translate") {
            contentType(ContentType.Application.Json)
            setBody(payload.toString())
        }
        val bodyText = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw TranslationException(
                TranslationErrorCode.ProviderError,
                "Translation endpoint answered HTTP ${response.status.value}.",
            )
        }

        val root = parseJsonObject(bodyText)
            ?: throw TranslationException(TranslationErrorCode.ProviderError, "Translation endpoint returned invalid JSON.")

        val translatedText = root["translatedText"]?.jsonPrimitive?.contentOrNull
        if (translatedText.isNullOrEmpty()) {
            throw TranslationException(TranslationErrorCode.ProviderError, "Translation endpoint returned no translatedText.")
        }

        return ProviderTranslation(
            translatedText = translatedText,
            detectedLanguage = extractDetectedLanguage(root),
        )
    }

    override suspend fun listLanguages(): List<SupportedLanguage> {
        val response = client.get("$baseUrl/languages")
        val bodyText = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw TranslationException(
                TranslationErrorCode.ProviderError,
                "Translation endpoint answered HTTP ${response.status.value}.",
            )
        }
        val root = try {
            Json.parseToJsonElement(bodyText)
        } catch (e: Exception) {
            throw TranslationException(TranslationErrorCode.ProviderError, "Translation endpoint returned invalid JSON.", e)
        }
        val array = try {
            root.jsonArray
        } catch (e: Exception) {
            throw TranslationException(TranslationErrorCode.ProviderError, "Translation endpoint returned an unexpected /languages payload.", e)
        }
        return array.mapNotNull { element ->
            val obj = try {
                element.jsonObject
            } catch (e: Exception) {
                null
            } ?: return@mapNotNull null
            val code = obj["code"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: code
            SupportedLanguage(code = code, name = name)
        }
    }

    private fun parseJsonObject(text: String): JsonObject? {
        return try {
            Json.parseToJsonElement(text).jsonObject
        } catch (e: Exception) {
            null
        }
    }

    private fun extractDetectedLanguage(root: JsonObject): String? {
        val node = root["detectedLanguage"] ?: return null
        // LibreTranslate answers either "es" or {"language": "es", "confidence": 92}.
        return when (node) {
            is JsonPrimitive -> if (node.isString) node.contentOrNull else null
            is JsonObject -> (node["language"] as? JsonPrimitive)
                ?.takeIf { it.isString }
                ?.contentOrNull
            else -> null
        }
    }
}
