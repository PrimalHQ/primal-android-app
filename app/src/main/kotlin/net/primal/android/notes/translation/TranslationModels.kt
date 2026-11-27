package net.primal.android.notes.translation

/**
 * User-configurable note translation settings.
 *
 * Translation is opt-in and off by default: notes are public posts, but
 * sending their text to a third-party service is still a choice the user
 * should make explicitly, and the user picks (and can self-host) the
 * LibreTranslate-compatible endpoint. Settings are stored locally on the
 * device only — in particular the optional API key never leaves the device
 * except as a header/field on requests to the user's own endpoint.
 */
data class TranslationSettings(
    val enabled: Boolean = false,
    /** Base URL of a LibreTranslate-compatible instance, e.g. https://libretranslate.com */
    val endpoint: String = "",
    /** Optional API key for endpoints that require one. */
    val apiKey: String = "",
    /** ISO 639-1 code of the user's preferred language, e.g. "en". Empty = unset. */
    val targetLanguage: String = "",
) {
    val isConfigured: Boolean
        get() = endpoint.isNotBlank() && targetLanguage.isNotBlank()
}

/** Endpoint helpers shared by the service, the provider and Settings validation. */
object TranslationEndpoints {

    /**
     * Accept endpoints pasted with or without a trailing "/translate"
     * (or trailing slashes) and normalize them to the base URL. Any query
     * string or fragment is dropped first, so the provider path appended
     * later (/translate, /languages) can never be hijacked by them; a
     * subpath (e.g. a reverse-proxy prefix) is kept.
     */
    fun normalizeEndpoint(raw: String): String {
        var endpoint = raw.trim()
            .substringBefore('#')
            .substringBefore('?')
            .trim()
            .trimEnd('/')
        if (endpoint.endsWith("/translate")) {
            endpoint = endpoint.removeSuffix("/translate").trimEnd('/')
        }
        return endpoint
    }

    fun isValidEndpoint(raw: String): Boolean {
        val endpoint = normalizeEndpoint(raw)
        val uri = try {
            java.net.URI(endpoint)
        } catch (e: Exception) {
            return false
        }
        // A real parse, not just a scheme prefix: a host must be present,
        // the scheme must be HTTP(S), and userinfo is rejected outright —
        // the authority must be the host the user actually configured.
        return (uri.scheme == "http" || uri.scheme == "https") &&
            !uri.host.isNullOrBlank() &&
            uri.userInfo == null
    }

    /**
     * Whether an API key may be sent to this endpoint: only over HTTPS, or
     * to a loopback address (a self-hosted local instance). Sending a key
     * over plain HTTP to a remote host would expose it to network observers.
     * The host is taken from a real URI parse — never from string splitting —
     * so userinfo tricks such as `http://localhost:5000@attacker.example`
     * (whose real host is attacker.example) are rejected.
     */
    fun isSecureForApiKey(raw: String): Boolean {
        val endpoint = normalizeEndpoint(raw)
        val uri = try {
            java.net.URI(endpoint)
        } catch (e: Exception) {
            return false
        }
        // Reject userinfo on any scheme: the authority must be the real host.
        if (uri.userInfo != null) return false
        return when (uri.scheme) {
            "https" -> true
            "http" -> uri.host == "localhost" || uri.host == "127.0.0.1" ||
                uri.host == "::1" || uri.host == "[::1]"
            else -> false
        }
    }
}

enum class TranslationErrorCode {
    /** The feature toggle is off. */
    Disabled,

    /** No endpoint or no target language configured. */
    NotConfigured,

    /** The note has nothing translatable. */
    EmptyContent,

    /** The endpoint could not be reached (network error / timeout). */
    Unreachable,

    /** The endpoint answered with an error or an unusable payload. */
    ProviderError,
}

class TranslationException(
    val code: TranslationErrorCode,
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message ?: code.name, cause)

data class TranslationResult(
    val translatedText: String,
    /** Language reported by the provider, when it reports one. */
    val detectedLanguage: String?,
    /** Locally detected language of the prose, when confident. */
    val localDetectedLanguage: String?,
    val targetLanguage: String,
    val fromCache: Boolean,
)

data class ProviderTranslation(
    val translatedText: String,
    val detectedLanguage: String?,
)

data class SupportedLanguage(
    val code: String,
    val name: String,
)

/**
 * A translation backend. The app ships with a LibreTranslate-compatible
 * implementation; the interface keeps the rest of the feature testable and
 * leaves room for additional providers later.
 */
interface TranslationProvider {
    /**
     * Translate [text] into [targetLanguage]. [sourceLanguage] is an
     * ISO 639-1 code or "auto" to let the provider detect it.
     */
    suspend fun translate(text: String, sourceLanguage: String, targetLanguage: String): ProviderTranslation

    /** Languages the endpoint can translate to/from. */
    suspend fun listLanguages(): List<SupportedLanguage>
}
