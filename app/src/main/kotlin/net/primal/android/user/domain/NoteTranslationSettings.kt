package net.primal.android.user.domain

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class NoteTranslationSettings(
    val enabled: Boolean = true,
    val targetLanguageCode: String = DEVICE_LANGUAGE_CODE,
    val provider: NoteTranslationProvider = NoteTranslationProvider.PreferOnDevice,
    val libreTranslateUrl: String = "",
    val apiKey: String = "",
    @EncodeDefault val apiKeyProvider: NoteTranslationProvider = provider,
    @EncodeDefault val apiKeyLibreTranslateUrl: String = libreTranslateUrl,
) {
    fun apiKeyForProvider(): String? {
        val usesLibreTranslate = provider == NoteTranslationProvider.LibreTranslate ||
            provider == NoteTranslationProvider.PreferOnDevice
        val sameProvider = apiKeyProvider == provider
        val sameEndpoint = !usesLibreTranslate || apiKeyLibreTranslateUrl == libreTranslateUrl
        return apiKey.takeIf { sameProvider && sameEndpoint }?.trim()?.ifBlank { null }
    }

    companion object {
        const val DEVICE_LANGUAGE_CODE = ""
    }
}

@Serializable
enum class NoteTranslationProvider {
    PreferOnDevice,
    LibreTranslate,
    DeepL,
    Google,
}
