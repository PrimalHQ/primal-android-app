package net.primal.android.notes.translation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import net.primal.core.networking.factory.HttpClientFactory

/**
 * App-wide entry point to the note translation feature: owns the settings
 * store, the shared result cache and the [TranslationService]. Used by the
 * note translate bar and by the translation settings screen.
 */
@Singleton
class NoteTranslationManager @Inject constructor(
    val settingsStore: TranslationSettingsStore,
) {
    val cache = TranslationCache()

    // One HTTP client for the whole feature, created without the app's
    // default HTTP configuration on purpose: the default config logs request
    // bodies, and translation requests can carry the user's API key. The
    // client is never closed — it lives as long as the app process, like the
    // app's other HTTP clients — so settings changes only rebuild the cheap
    // provider wrapper around it.
    private val httpClient by lazy {
        HttpClientFactory.createHttpClient {
            // Never follow redirects for translation requests: a redirect
            // could carry the request — including the API key in its body —
            // to a host the user never configured. A 3xx surfaces as an
            // error instead; configure the final endpoint URL.
            followRedirects = false
            install(io.ktor.client.plugins.HttpTimeout) {
                requestTimeoutMillis = REQUEST_TIMEOUT_MS
                connectTimeoutMillis = CONNECT_TIMEOUT_MS
            }
        }
    }

    // Providers are lightweight wrappers over the shared client; reuse one
    // per settings snapshot instead of rebuilding it for every translation.
    private var cachedProvider: LibreTranslateProvider? = null
    private var cachedProviderSettings: TranslationSettings? = null

    private fun providerFor(settings: TranslationSettings): LibreTranslateProvider {
        val existing = cachedProvider
        if (existing != null && cachedProviderSettings == settings) return existing
        val created = LibreTranslateProvider(settings, httpClient)
        cachedProvider = created
        cachedProviderSettings = settings
        return created
    }

    private val service = TranslationService(
        settingsProvider = { settingsStore.settings },
        providerFactory = { settings -> providerFor(settings) },
        cache = cache,
    )

    suspend fun translateNote(content: String): TranslationResult = service.translateNote(content)

    suspend fun listLanguages(): List<SupportedLanguage> = service.listLanguages()

    fun clearCache() = cache.clear()

    companion object {
        private const val REQUEST_TIMEOUT_MS = 60_000L
        private const val CONNECT_TIMEOUT_MS = 15_000L
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NoteTranslationEntryPoint {
    fun noteTranslationManager(): NoteTranslationManager
}

@Composable
fun rememberNoteTranslationManager(): NoteTranslationManager {
    val context = LocalContext.current
    return remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            NoteTranslationEntryPoint::class.java,
        ).noteTranslationManager()
    }
}
