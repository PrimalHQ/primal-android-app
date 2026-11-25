package net.primal.android.settings.translation

import net.primal.android.core.translation.TranslationService

interface TranslationSettingsContract {

    data class UiState(
        val selectedLanguageCode: String = "en",
        val languages: List<TranslationService.Language> = TranslationService.supportedLanguages,
    )

    sealed class UiEvent {
        data class SelectLanguage(val languageCode: String) : UiEvent()
    }
}
