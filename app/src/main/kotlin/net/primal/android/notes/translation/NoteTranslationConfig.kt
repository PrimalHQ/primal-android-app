package net.primal.android.notes.translation

import java.net.URI
import net.primal.android.user.domain.NoteTranslationProvider
import net.primal.android.user.domain.NoteTranslationSettings
import net.primal.core.utils.runCatching

sealed class TranslationPlan {
    data class OnDeviceThenNetwork(val network: NetworkTranslationRoute?) : TranslationPlan()
    data class NetworkOnly(val network: NetworkTranslationRoute) : TranslationPlan()
    data object Unavailable : TranslationPlan()
}

sealed class NetworkTranslationRoute {
    data class LibreTranslate(val endpoint: String, val apiKey: String?) : NetworkTranslationRoute()
    data class DeepL(val apiKey: String, val endpoint: String) : NetworkTranslationRoute()
    data class Google(val apiKey: String) : NetworkTranslationRoute()

    fun cacheId(): String =
        when (this) {
            is LibreTranslate -> "lt:$endpoint"
            is DeepL -> "deepl:$endpoint"
            is Google -> "google"
        }
}

object NoteTranslationConfig {

    const val DEEPL_FREE_ENDPOINT = "https://api-free.deepl.com/v2/translate"
    const val DEEPL_PRO_ENDPOINT = "https://api.deepl.com/v2/translate"
    const val GOOGLE_TRANSLATE_ENDPOINT = "https://translation.googleapis.com/language/translate/v2"

    fun resolve(settings: NoteTranslationSettings): TranslationPlan {
        val network = resolveNetworkRoute(settings)
        return when (settings.provider) {
            NoteTranslationProvider.PreferOnDevice -> TranslationPlan.OnDeviceThenNetwork(network = network)
            NoteTranslationProvider.LibreTranslate,
            NoteTranslationProvider.DeepL,
            NoteTranslationProvider.Google,
            -> {
                if (network == null) TranslationPlan.Unavailable else TranslationPlan.NetworkOnly(network)
            }
        }
    }

    @Suppress("ReturnCount")
    fun resolveNetworkRoute(settings: NoteTranslationSettings): NetworkTranslationRoute? {
        val apiKey = settings.apiKey.trim().ifBlank { null }
        return when (settings.provider) {
            NoteTranslationProvider.PreferOnDevice,
            NoteTranslationProvider.LibreTranslate,
            -> parseLibreTranslateRoute(settings.libreTranslateUrl, apiKey)

            NoteTranslationProvider.DeepL -> {
                val key = apiKey ?: return null
                NetworkTranslationRoute.DeepL(apiKey = key, endpoint = deeplEndpoint(key))
            }

            NoteTranslationProvider.Google -> {
                val key = apiKey ?: return null
                NetworkTranslationRoute.Google(apiKey = key)
            }
        }
    }

    fun parseLibreTranslateRoute(rawUrl: String, apiKey: String?): NetworkTranslationRoute.LibreTranslate? {
        val endpoint = normalizeLibreTranslateBaseUrl(rawUrl) ?: return null
        return NetworkTranslationRoute.LibreTranslate(endpoint = endpoint, apiKey = apiKey)
    }

    /**
     * Fail-closed: empty or invalid input never falls back to a public default host.
     * Accepts a base URL or a full `/translate` path.
     */
    @Suppress("ReturnCount")
    fun normalizeLibreTranslateBaseUrl(raw: String): String? {
        val cleaned = raw.trim().trimEnd('/')
        if (cleaned.isEmpty()) return null

        val withoutTranslate = if (cleaned.endsWith("/translate", ignoreCase = true)) {
            cleaned.dropLast("/translate".length).trimEnd('/')
        } else {
            cleaned
        }
        if (withoutTranslate.isEmpty()) return null

        val uri = runCatching { URI(withoutTranslate) }.getOrNull() ?: return null
        val scheme = uri.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") return null
        if (uri.host.isNullOrBlank()) return null
        return withoutTranslate
    }

    fun deeplEndpoint(apiKey: String): String {
        return if (apiKey.trim().endsWith(":fx", ignoreCase = true)) {
            DEEPL_FREE_ENDPOINT
        } else {
            DEEPL_PRO_ENDPOINT
        }
    }

    fun isReady(settings: NoteTranslationSettings): Boolean {
        if (!settings.enabled) return false
        return resolve(settings) !is TranslationPlan.Unavailable
    }
}
