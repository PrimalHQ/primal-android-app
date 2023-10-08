package net.primal.android.settings.appearance

interface AppearanceSettingsContract {
    data class UiState(val selectedThemeName: String = "")

    sealed class UiEvent {
        data class SelectedThemeChanged(val themeName: String) : UiEvent()
    }
}