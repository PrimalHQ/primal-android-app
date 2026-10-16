package net.primal.android.user.domain

import kotlinx.serialization.Serializable

@Serializable
data class NoteTranslationSettings(
    val enabled: Boolean = true,
    val targetLanguageCode: String = DEVICE_LANGUAGE_CODE,
    val provider: NoteTranslationProvider = NoteTranslationProvider.PreferOnDevice,
    val libreTranslateUrl: String = "",
    val apiKey: String = "",
) {
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
