package net.primal.android.core.translation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for translating text using LibreTranslate API.
 * Uses the free public instance at libretranslate.com
 */
@Singleton
class TranslationService @Inject constructor() {

    private val baseUrl = "https://libretranslate.com"
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Supported languages for translation
     */
    data class Language(
        val code: String,
        val name: String,
    )

    companion object {
        val supportedLanguages = listOf(
            Language("en", "English"),
            Language("es", "Spanish"),
            Language("fr", "French"),
            Language("de", "German"),
            Language("it", "Italian"),
            Language("pt", "Portuguese"),
            Language("zh", "Chinese"),
            Language("ja", "Japanese"),
            Language("ko", "Korean"),
            Language("ru", "Russian"),
            Language("ar", "Arabic"),
        )

        /**
         * Get default language code based on device locale
         */
        fun getDefaultLanguageCode(): String {
            val deviceLanguage = Locale.getDefault().language
            return if (supportedLanguages.any { it.code == deviceLanguage }) {
                deviceLanguage
            } else {
                "en"
            }
        }
    }

    @Serializable
    private data class TranslateRequest(
        val q: String,
        val source: String,
        val target: String,
        val format: String = "text",
    )

    @Serializable
    private data class TranslateResponse(
        @SerialName("translatedText")
        val translatedText: String,
    )

    @Serializable
    private data class ErrorResponse(
        val error: String,
    )

    @Serializable
    private data class DetectResponse(
        val confidence: Double,
        val language: String,
    )

    /**
     * Translates text to the target language.
     *
     * @param text The text to translate
     * @param targetLanguage Target language code (e.g., "en", "es", "fr")
     * @param sourceLanguage Source language code, or "auto" for auto-detection
     * @return Result containing translated text or error
     */
    suspend fun translate(
        text: String,
        targetLanguage: String,
        sourceLanguage: String = "auto",
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/translate")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val requestBody = json.encodeToString(
                TranslateRequest.serializer(),
                TranslateRequest(
                    q = text,
                    source = sourceLanguage,
                    target = targetLanguage,
                ),
            )

            connection.outputStream.use { os ->
                os.write(requestBody.toByteArray())
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().readText()
                val response = json.decodeFromString(TranslateResponse.serializer(), responseText)
                Result.success(response.translatedText)
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                try {
                    val errorResponse = json.decodeFromString(ErrorResponse.serializer(), errorText)
                    Result.failure(TranslationException(errorResponse.error))
                } catch (e: Exception) {
                    Result.failure(TranslationException("Translation failed: HTTP $responseCode"))
                }
            }
        } catch (e: Exception) {
            Result.failure(TranslationException(e.message ?: "Translation failed"))
        }
    }

    /**
     * Detects the language of the given text.
     *
     * @param text The text to analyze
     * @return Result containing detected language code or error
     */
    suspend fun detectLanguage(text: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/detect")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val requestBody = """{"q": "${text.replace("\"", "\\\"")}"}"""
            connection.outputStream.use { os ->
                os.write(requestBody.toByteArray())
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().readText()
                // Response is an array, get first element
                val responses = json.decodeFromString<List<DetectResponse>>(responseText)
                if (responses.isNotEmpty()) {
                    Result.success(responses.first().language)
                } else {
                    Result.failure(TranslationException("Could not detect language"))
                }
            } else {
                Result.failure(TranslationException("Language detection failed: HTTP $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(TranslationException(e.message ?: "Language detection failed"))
        }
    }
}

class TranslationException(message: String) : Exception(message)
