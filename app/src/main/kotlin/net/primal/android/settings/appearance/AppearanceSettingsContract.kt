package net.primal.android.settings.appearance

interface AppearanceSettingsContract {
    data class UiState(
        val selectedThemeName: String = "",
        val error: AppearanceError? = null
    ) {

        sealed class AppearanceError {
            data class FailedToChangeTheme(val throwable: Throwable) : AppearanceError()
        }
    }

    sealed class UiEvent {
        data class SelectedThemeChanged(val themeName: String) : UiEvent()
    }
}