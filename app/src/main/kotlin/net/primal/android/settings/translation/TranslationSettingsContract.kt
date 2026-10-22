package net.primal.android.settings.translation

interface TranslationSettingsContract {
    data class UiState(
        val enabled: Boolean = true,
        val endpoint: String = "",
        val apiKey: String = "",
        val targetLanguage: String = "",
        val saved: Boolean = false,
    )

    sealed class UiEvent {
        data class EnabledChanged(val enabled: Boolean) : UiEvent()
        data class EndpointChanged(val endpoint: String) : UiEvent()
        data class ApiKeyChanged(val apiKey: String) : UiEvent()
        data class TargetLanguageChanged(val targetLanguage: String) : UiEvent()
        data object Save : UiEvent()
        data object RestoreDefaults : UiEvent()
        data object SavedMessageShown : UiEvent()
    }
}
