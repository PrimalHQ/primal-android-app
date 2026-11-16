package net.primal.android.notes.translation

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import net.primal.core.utils.serialization.CommonJson
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Note translation service.
 *
 * Translates note content into the user's language using the keyless
 * Google Translate endpoint (the same `gtx` client endpoint used by
 * Google Translate itself). No API key and no backend are required.
 * This is a Kotlin port of the web client's translation service
 * (primal-web-app, `src/lib/translate.ts`).
 *
 * Before anything is sent to the translation service, Nostr entities
 * (nostr: URIs, bech32 identifiers, URLs, hashtags, lightning invoices
 * and Cashu tokens) are swapped out for placeholders and restored
 * afterwards, so they are never mangled by the translation.
 *
 * Results are cached in-memory, keyed by a hash of the content and the
 * target language, so re-rendering a note never re-fetches its
 * translation.
 */
class NoteTranslationService(
    private val client: OkHttpClient = sharedClient,
) {

    /**
     * Translates [content] into [targetLanguage], or returns null when
     * the translation fails so callers can degrade gracefully.
     */
    suspend fun translate(content: String, targetLanguage: String): NoteTranslation? {
        val target = normalizeLanguageCode(targetLanguage)
        getCachedTranslation(content, target)?.let { return it }

        return try {
            val (protectedText, entities) = protectEntities(content)
            val chunks = chunkText(protectedText, MAX_CHUNK_LENGTH)

            var detectedLanguage = target
            val translatedText = StringBuilder()
            for (chunk in chunks) {
                val result = translateChunk(chunk, target)
                detectedLanguage = result.detectedLanguage
                translatedText.append(result.translated)
            }

            val translation = NoteTranslation(
                text = restoreEntities(translatedText.toString(), entities),
                detectedLanguage = detectedLanguage,
                targetLanguage = target,
            )
            cacheTranslation(content, translation)
            translation
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            null
        }
    }

    private suspend fun translateChunk(text: String, targetLanguage: String): ChunkTranslation {
        try {
            return translateChunkOnce(text, targetLanguage)
        } catch (error: TranslationException) {
            val status = error.statusCode
            // Retry once on transient rate limiting or server errors.
            if (status != HTTP_TOO_MANY_REQUESTS && (status == null || status < HTTP_SERVER_ERROR)) throw error
            delay(RETRY_DELAY_MILLIS)
            return translateChunkOnce(text, targetLanguage)
        }
    }

    private suspend fun translateChunkOnce(text: String, targetLanguage: String): ChunkTranslation {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(TRANSLATE_HOST)
            .addPathSegments(TRANSLATE_PATH)
            .addQueryParameter("client", "gtx")
            .addQueryParameter("sl", "auto")
            .addQueryParameter("tl", targetLanguage)
            .addQueryParameter("dt", "t")
            .addQueryParameter("q", text)
            .build()

        val request = Request.Builder().url(url).get().build()

        val body = withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw TranslationException(response.code)
                response.body?.string() ?: throw TranslationException(response.code)
            }
        }

        return parseResponse(body)
    }

    private fun parseResponse(body: String): ChunkTranslation {
        val root = CommonJson.parseToJsonElement(body) as? JsonArray
        val segments = root?.getOrNull(0) as? JsonArray
        val translated = segments?.joinToString(separator = "") { segment ->
            ((segment as? JsonArray)?.getOrNull(0) as? JsonPrimitive)?.contentOrNull ?: ""
        } ?: ""
        val detectedLanguage = normalizeLanguageCode(
            (root?.getOrNull(2) as? JsonPrimitive)?.contentOrNull,
        )
        return ChunkTranslation(translated = translated, detectedLanguage = detectedLanguage)
    }

    private fun protectEntities(content: String): ProtectedContent {
        val entities = mutableListOf<String>()
        val text = content.replace(entityRegex) { match ->
            entities.add(match.value)
            placeholderFor(entities.size - 1)
        }
        return ProtectedContent(text = text, entities = entities)
    }

    private fun restoreEntities(text: String, entities: List<String>): String {
        return text.replace(placeholderRegex) { match ->
            val index = match.groupValues.getOrNull(1)?.toIntOrNull()
            index?.let { entities.getOrNull(it) } ?: match.value
        }
    }

    // Split text into chunks of at most `maxLength` characters, preferring
    // paragraph, then sentence, then word boundaries.
    private fun chunkText(text: String, maxLength: Int): List<String> {
        if (text.length <= maxLength) return listOf(text)

        val chunks = mutableListOf<String>()
        var current = ""

        fun pushCurrent(keepBreak: Boolean = false) {
            if (current.isNotEmpty()) {
                // Translated chunks are rejoined without a separator, so keep
                // the paragraph break at the end of the chunk being pushed.
                chunks.add(if (keepBreak && !current.endsWith("\n")) "$current\n" else current)
                current = ""
            }
        }

        for (paragraph in text.split('\n')) {
            val candidate = if (current.isEmpty()) paragraph else "$current\n$paragraph"

            if (candidate.length <= maxLength) {
                current = candidate
                continue
            }

            pushCurrent(keepBreak = true)

            if (paragraph.length <= maxLength) {
                current = paragraph
                continue
            }

            // Paragraph alone is too long, split it into sentences.
            val sentences = sentenceRegex.findAll(paragraph)
                .map { it.value }
                .toList()
                .ifEmpty { listOf(paragraph) }

            for (sentence in sentences) {
                val next = current + sentence

                if (next.length <= maxLength) {
                    current = next
                    continue
                }

                pushCurrent()

                if (sentence.length <= maxLength) {
                    current = sentence
                    continue
                }

                // A single sentence is still too long, split it at word boundaries.
                var rest = sentence
                while (rest.length > maxLength) {
                    var cut = rest.lastIndexOf(' ', maxLength)
                    if (cut <= 0) cut = maxLength
                    chunks.add(rest.substring(0, cut))
                    rest = rest.substring(cut)
                }
                current = rest
            }
        }

        pushCurrent()

        return chunks.filter { it.isNotEmpty() }
    }

    private data class ProtectedContent(
        val text: String,
        val entities: List<String>,
    )

    private data class ChunkTranslation(
        val translated: String,
        val detectedLanguage: String,
    )

    private class TranslationException(val statusCode: Int?) :
        Exception("Translation request failed: $statusCode")

    companion object {
        private const val TRANSLATE_HOST = "translate.googleapis.com"
        private const val TRANSLATE_PATH = "translate_a/single"

        // The endpoint rejects overly long requests, so content is translated
        // in chunks no longer than this.
        private const val MAX_CHUNK_LENGTH = 1000
        private const val MAX_CACHE_ENTRIES = 200
        private const val REQUEST_TIMEOUT_SECONDS = 15L
        private const val RETRY_DELAY_MILLIS = 800L
        private const val HTTP_TOO_MANY_REQUESTS = 429
        private const val HTTP_SERVER_ERROR = 500
        private const val FNV_PRIME = 0x01000193
        private val FNV_OFFSET_BASIS: Int = 0x811c9dc5.toInt()

        private val sharedClient: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .callTimeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

        // Entities that must survive translation verbatim. Order matters:
        // longer, more specific patterns come first.
        private val entityRegex = Regex(
            pattern = listOf(
                """nostr:(?:npub|note|nevent|naddr|nprofile|nrelay)1[qpzry9x8gf2tvdw0s3jn54khce6mua7l]+""",
                """https?://[^\s]+""",
                """(?:npub|note|nevent|naddr|nprofile)1[qpzry9x8gf2tvdw0s3jn54khce6mua7l]+""",
                """ln(?:bc|tb|url)[0-9a-zA-Z]+""",
                """cashu[AB][0-9a-zA-Z+/=_\-]+""",
                """#[^\s#]+""",
            ).joinToString(separator = "|"),
            options = setOf(RegexOption.IGNORE_CASE),
        )

        // Tolerates whitespace the translation service may insert into a
        // placeholder while translating the surrounding text.
        private val placeholderRegex = Regex(
            pattern = """__\s*P\s*(\d+)\s*__""",
            options = setOf(RegexOption.IGNORE_CASE),
        )

        private val sentenceRegex = Regex(pattern = """[^.!?…]+[.!?…]*\s*""")

        // Language codes the translation endpoint expects, keyed by the
        // primary subtag used by the app's locale settings.
        private val languageAliases = mapOf(
            "zh-cn" to "zh-CN",
            "zh-tw" to "zh-TW",
            "zh-hk" to "zh-TW",
            "nb" to "no",
            "nn" to "no",
        )

        private val translationCache = LinkedHashMap<String, NoteTranslation>()

        fun normalizeLanguageCode(locale: String?): String {
            if (locale.isNullOrBlank()) return "en"

            val cleaned = locale.trim().lowercase().replace('_', '-')

            languageAliases[cleaned]?.let { return it }

            return cleaned.split('-').firstOrNull()?.takeIf { it.isNotEmpty() } ?: "en"
        }

        private fun placeholderFor(index: Int): String = "__P${index}__"

        // FNV-1a hash, used to key the translation cache by content.
        private fun hashContent(content: String): Int {
            var hash = FNV_OFFSET_BASIS
            for (char in content) {
                hash = hash xor char.code
                hash *= FNV_PRIME
            }
            return hash
        }

        private fun cacheKey(content: String, targetLanguage: String): String =
            "$targetLanguage:${content.length}:${hashContent(content).toUInt().toString(radix = 16)}"

        private fun getCachedTranslation(content: String, targetLanguage: String): NoteTranslation? =
            synchronized(translationCache) {
                translationCache[cacheKey(content, targetLanguage)]
            }

        private fun cacheTranslation(content: String, translation: NoteTranslation) {
            val key = cacheKey(content, translation.targetLanguage)
            synchronized(translationCache) {
                if (!translationCache.containsKey(key) && translationCache.size >= MAX_CACHE_ENTRIES) {
                    // Evict the oldest entry to keep the cache bounded.
                    translationCache.remove(translationCache.keys.first())
                }
                translationCache[key] = translation
            }
        }
    }
}
