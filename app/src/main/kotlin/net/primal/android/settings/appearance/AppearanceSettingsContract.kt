package net.primal.android.settings.appearance

import net.primal.android.theme.domain.PrimalTheme

interface AppearanceSettingsContract {
    data class UiState(
        val selectedThemeName: String? = null,
        val themes: List<PrimalTheme> = emptyList(),
    )

    sealed class UiEvent {
        data class SelectedThemeChanged(val themeName: String) : UiEvent()
        data class ToggleAutoAdjustDarkTheme(val enabled: Boolean, val isSystemInDarkTheme: Boolean) : UiEvent()
    }
}
