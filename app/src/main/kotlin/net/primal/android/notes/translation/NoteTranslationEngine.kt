package net.primal.android.notes.translation

import kotlinx.coroutines.withContext
import net.primal.android.notes.translation.device.OnDeviceNoteTranslator
import net.primal.android.notes.translation.remote.RemoteNoteTranslator
import net.primal.android.user.domain.NoteTranslationSettings
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.fold
import net.primal.core.utils.runCatching

class NoteTranslationEngine(
    private val remoteClient: RemoteNoteTranslator,
    private val onDeviceTranslator: OnDeviceNoteTranslator,
    private val dispatcherProvider: DispatcherProvider,
    private val cache: NoteTranslationCache = NoteTranslationCache(),
) {
    @Suppress("ReturnCount")
    suspend fun translate(
        text: String,
        settings: NoteTranslationSettings,
    ): NoteTranslationOutcome {
        if (!settings.enabled || text.isBlank()) {
            return NoteTranslationOutcome.Failed()
        }

        val targetLanguage = NoteTranslationLanguages.resolveTargetLanguageCode(settings.targetLanguageCode)
        val plan = NoteTranslationConfig.resolve(settings)
        val cacheKey = cacheKey(text = text, targetLanguage = targetLanguage, plan = plan)
        cache.get(cacheKey)?.let { return it }

        val protected = NoteTokenProtector.protect(text)
        val outcome = when (plan) {
            TranslationPlan.Unavailable -> NoteTranslationOutcome.NotConfigured
            is TranslationPlan.OnDeviceThenNetwork -> translatePreferOnDevice(
                original = text,
                protected = protected,
                targetLanguage = targetLanguage,
                network = plan.network,
            )

            is TranslationPlan.NetworkOnly -> translateNetwork(
                original = text,
                protected = protected,
                targetLanguage = targetLanguage,
                network = plan.network,
            )
        }
        cache.put(cacheKey, outcome)
        return outcome
    }

    @Suppress("ReturnCount")
    private suspend fun translatePreferOnDevice(
        original: String,
        protected: ProtectedNoteText,
        targetLanguage: String,
        network: NetworkTranslationRoute?,
    ): NoteTranslationOutcome {
        val onDevice = runCatching {
            onDeviceTranslator.translate(text = protected.masked, targetLanguage = targetLanguage)
        }.getOrNull()
        if (!onDevice.isNullOrBlank()) {
            return finish(
                original = original,
                protected = protected,
                translated = onDevice,
                source = NoteTranslationSource.OnDevice,
            )
        }
        if (network == null) {
            return NoteTranslationOutcome.NotConfigured
        }
        return translateNetwork(
            original = original,
            protected = protected,
            targetLanguage = targetLanguage,
            network = network,
        )
    }

    private suspend fun translateNetwork(
        original: String,
        protected: ProtectedNoteText,
        targetLanguage: String,
        network: NetworkTranslationRoute,
    ): NoteTranslationOutcome {
        val result: Result<String> = withContext(dispatcherProvider.io()) {
            runCatching {
                remoteClient.translate(
                    route = network,
                    text = protected.masked,
                    targetLanguage = targetLanguage,
                )
            }
        }
        return result.fold(
            onSuccess = { translated ->
                finish(
                    original = original,
                    protected = protected,
                    translated = translated,
                    source = network.toSource(),
                )
            },
            onFailure = { error -> NoteTranslationOutcome.Failed(cause = error) },
        )
    }

    private fun finish(
        original: String,
        protected: ProtectedNoteText,
        translated: String,
        source: NoteTranslationSource,
    ): NoteTranslationOutcome {
        val restored = protected.restore(translated).ifBlank { translated }
        return if (isAlreadyInTarget(original = original, translated = restored)) {
            NoteTranslationOutcome.AlreadyInTarget(text = restored)
        } else {
            NoteTranslationOutcome.Translated(text = restored, source = source)
        }
    }

    private fun cacheKey(
        text: String,
        targetLanguage: String,
        plan: TranslationPlan,
    ): NoteTranslationCache.CacheKey {
        val routeId = when (plan) {
            TranslationPlan.Unavailable -> "unavailable"
            is TranslationPlan.OnDeviceThenNetwork -> "on-device+${plan.network?.cacheId().orEmpty()}"
            is TranslationPlan.NetworkOnly -> plan.network.cacheId()
        }
        return NoteTranslationCache.CacheKey(
            textHash = text.hashCode(),
            targetLanguage = targetLanguage,
            routeId = routeId,
        )
    }

    companion object {
        fun isAlreadyInTarget(original: String, translated: String): Boolean =
            original.trim().equals(translated.trim(), ignoreCase = true)
    }
}
