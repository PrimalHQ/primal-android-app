package net.primal.android.settings.translation

import net.primal.android.notes.translation.SupportedLanguage

interface TranslationSettingsContract {

    data class UiState(
        val enabled: Boolean = false,
        val endpoint: String = "",
        val endpointDraft: String = "",
        val apiKey: String = "",
        val apiKeyDraft: String = "",
        val targetLanguage: String = "",
        val targetLanguageDraft: String = "",
        val languages: List<SupportedLanguage> = emptyList(),
        val testingConnection: Boolean = false,
        val testOutcome: TestOutcome? = null,
        val cacheCount: Int = 0,
    )

    enum class TestOutcome {
        Success,
        NotConfigured,
        Unreachable,
        ProviderError,
    }

    sealed class UiEvent {
        data class UpdateEnabled(val enabled: Boolean) : UiEvent()
        data class UpdateEndpointDraft(val value: String) : UiEvent()
        data object ConfirmEndpoint : UiEvent()
        data class UpdateApiKeyDraft(val value: String) : UiEvent()
        data object ConfirmApiKey : UiEvent()
        data class UpdateTargetLanguageDraft(val value: String) : UiEvent()
        data object ConfirmTargetLanguage : UiEvent()
        data class SelectLanguage(val code: String) : UiEvent()
        data object TestConnection : UiEvent()
        data object ClearCache : UiEvent()
    }
}
