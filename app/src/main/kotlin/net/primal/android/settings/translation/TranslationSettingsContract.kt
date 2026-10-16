package net.primal.android.settings.translation

import net.primal.android.user.domain.NoteTranslationProvider
import net.primal.android.user.domain.NoteTranslationSettings

interface TranslationSettingsContract {

    data class UiState(
        val enabled: Boolean = true,
        val targetLanguageCode: String = NoteTranslationSettings.DEVICE_LANGUAGE_CODE,
        val provider: NoteTranslationProvider = NoteTranslationProvider.PreferOnDevice,
        val libreTranslateUrl: String = "",
        val apiKey: String = "",
        val ready: Boolean = true,
    )

    sealed class UiEvent {
        data class UpdateEnabled(val enabled: Boolean) : UiEvent()
        data class UpdateTargetLanguage(val languageCode: String) : UiEvent()
        data class UpdateProvider(val provider: NoteTranslationProvider) : UiEvent()
        data class UpdateLibreTranslateUrl(val url: String) : UiEvent()
        data class UpdateApiKey(val apiKey: String) : UiEvent()
    }
}
