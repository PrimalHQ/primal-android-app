package net.primal.android.settings.home

interface SettingsHomeContract {
    data class UiState(
        val version: String,
        val developerToolsEnabled: Boolean = false,
    )

    sealed class UiEvent {
        data object VersionTapped : UiEvent()
    }
}
