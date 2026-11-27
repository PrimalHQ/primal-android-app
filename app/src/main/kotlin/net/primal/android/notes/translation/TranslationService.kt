package net.primal.android.notes.translation

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Orchestrates one note translation:
 *
 *   raw note content
 *     -> mask protected tokens (TranslationSanitizer)
 *     -> cache lookup (TranslationCache)
 *     -> provider.translate(source = "auto") on the masked text only
 *     -> restore protected tokens into the result
 *     -> cache store
 *
 * Translation only ever happens when this is called, which is only from an
 * explicit user tap on the Translate action — notes are never translated
 * automatically.
 *
 * Ported from the web implementation in PrimalHQ/primal-web-app
 * (src/lib/translation.ts, PR #240).
 */
class TranslationService(
    private val settingsProvider: () -> TranslationSettings,
    private val providerFactory: (TranslationSettings) -> TranslationProvider,
    private val cache: TranslationCache = TranslationCache(),
) {

    suspend fun translateNote(content: String): TranslationResult {
        val settings = settingsProvider()
        if (!settings.enabled) {
            throw TranslationException(TranslationErrorCode.Disabled, "Note translation is disabled.")
        }
        if (!settings.isConfigured || !TranslationEndpoints.isValidEndpoint(settings.endpoint)) {
            throw TranslationException(
                TranslationErrorCode.NotConfigured,
                "Configure a translation endpoint and a target language in Settings first.",
            )
        }
        if (settings.apiKey.isNotBlank() && !TranslationEndpoints.isSecureForApiKey(settings.endpoint)) {
            throw TranslationException(
                TranslationErrorCode.NotConfigured,
                "An API key requires an HTTPS endpoint (or a localhost one).",
            )
        }
        val trimmed = content.trim()
        if (trimmed.isEmpty() || !TranslationSanitizer.shouldOfferTranslation(trimmed)) {
            throw TranslationException(TranslationErrorCode.EmptyContent, "This note has nothing to translate.")
        }

        // The target language is sent to the provider verbatim (e.g.
        // "zh-Hans", "pt-BR"), never reduced to its primary subtag — some
        // endpoints only offer region/script variants. Reduction to the
        // primary code happens only where codes are compared (detection).
        val target = settings.targetLanguage.trim()
        if (!TARGET_LANGUAGE_RE.matches(target)) {
            throw TranslationException(TranslationErrorCode.NotConfigured, "Invalid target language.")
        }

        val masked = TranslationSanitizer.maskProtectedTokens(trimmed)
        val prose = TranslationSanitizer.extractTranslatableProse(trimmed)
        val localDetected = LanguageDetector.detectLanguage(prose)

        val cached = cache.get(content = trimmed, endpoint = settings.endpoint, targetLanguage = target, apiKey = settings.apiKey)
        if (cached != null) {
            return TranslationResult(
                translatedText = cached,
                detectedLanguage = null,
                localDetectedLanguage = localDetected,
                targetLanguage = target,
                fromCache = true,
            )
        }

        val provider = providerFactory(settings)
        val providerResult = try {
            provider.translate(text = masked.maskedText, sourceLanguage = "auto", targetLanguage = target)
        } catch (e: CancellationException) {
            throw e
        } catch (e: TranslationException) {
            throw e
        } catch (e: Exception) {
            throw TranslationException(TranslationErrorCode.Unreachable, "Translation endpoint is unreachable.", e)
        }

        // If the caller cancelled while the provider was finishing anyway,
        // do not restore or cache the result.
        currentCoroutineContext().ensureActive()

        val translatedRaw = providerResult.translatedText.trim()
        if (translatedRaw.isEmpty()) {
            throw TranslationException(TranslationErrorCode.ProviderError, "The translation endpoint returned an empty result.")
        }

        val restored = TranslationSanitizer.restoreProtectedTokens(translatedRaw, masked.tokens, masked.placeholderPrefix)
        cache.put(content = trimmed, endpoint = settings.endpoint, targetLanguage = target, translatedText = restored, apiKey = settings.apiKey)

        return TranslationResult(
            translatedText = restored,
            detectedLanguage = LanguageDetector.primaryLanguageCode(providerResult.detectedLanguage),
            localDetectedLanguage = localDetected,
            targetLanguage = target,
            fromCache = false,
        )
    }

    companion object {
        /** BCP-47-ish language tag: "en", "id", "pt-BR", "zh-Hans", ... */
        private val TARGET_LANGUAGE_RE = Regex("""^[A-Za-z]{2,3}(-[A-Za-z0-9]{2,8})*$""")
    }

    /** Languages offered by the currently configured endpoint (Settings "test connection"). */
    suspend fun listLanguages(): List<SupportedLanguage> {
        val settings = settingsProvider()
        if (!TranslationEndpoints.isValidEndpoint(settings.endpoint)) {
            throw TranslationException(TranslationErrorCode.NotConfigured, "Configure a translation endpoint first.")
        }
        return try {
            providerFactory(settings).listLanguages()
        } catch (e: CancellationException) {
            throw e
        } catch (e: TranslationException) {
            throw e
        } catch (e: Exception) {
            throw TranslationException(TranslationErrorCode.Unreachable, "Translation endpoint is unreachable.", e)
        }
    }
}
