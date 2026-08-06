package net.primal.android.settings.appearance

import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.user.domain.ContentAppearance

interface AppearanceSettingsContract {
    data class UiState(
        val selectedThemeName: String? = null,
        val themes: List<PrimalTheme> = emptyList(),
        val translationLanguage: String? = null,
    )

    sealed class UiEvent {
        data class ChangeTheme(val themeName: String) : UiEvent()
        data class ToggleAutoAdjustDarkTheme(val enabled: Boolean, val isSystemInDarkTheme: Boolean) : UiEvent()
        data class ChangeContentAppearance(val contentAppearance: ContentAppearance) : UiEvent()
        data class ChangeTranslationLanguage(val language: String) : UiEvent()
    }
}
