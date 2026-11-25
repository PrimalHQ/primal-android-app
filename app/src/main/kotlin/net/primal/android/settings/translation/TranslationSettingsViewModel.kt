package net.primal.android.settings.translation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import net.primal.android.notes.feed.note.translation.NoteTranslationRepository
import net.primal.android.notes.feed.note.translation.NoteTranslationSettings
import net.primal.android.settings.translation.TranslationSettingsContract.UiEvent
import net.primal.android.settings.translation.TranslationSettingsContract.UiState

@HiltViewModel
class TranslationSettingsViewModel @Inject constructor(
    private val repository: NoteTranslationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(repository.getSettings().asUiState())
    val state = _state.asStateFlow()

    fun setEvent(event: UiEvent) {
        when (event) {
            is UiEvent.EnabledChanged -> setState { copy(enabled = event.enabled, saved = false) }
            is UiEvent.EndpointChanged -> setState { copy(endpoint = event.endpoint, saved = false) }
            is UiEvent.ApiKeyChanged -> setState { copy(apiKey = event.apiKey, saved = false) }
            is UiEvent.TargetLanguageChanged -> setState { copy(targetLanguage = event.targetLanguage, saved = false) }
            UiEvent.Save -> saveSettings()
            UiEvent.RestoreDefaults -> restoreDefaults()
            UiEvent.SavedMessageShown -> setState { copy(saved = false) }
        }
    }

    private fun saveSettings() {
        repository.saveSettings(state.value.asSettings())
        setState { copy(saved = true) }
    }

    private fun restoreDefaults() {
        val defaults = repository.restoreDefaults()
        setState { defaults.asUiState(saved = true) }
    }

    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private fun UiState.asSettings() =
        NoteTranslationSettings(
            enabled = enabled,
            endpoint = endpoint,
            apiKey = apiKey,
            targetLanguage = targetLanguage,
        )

    private fun NoteTranslationSettings.asUiState(saved: Boolean = false) =
        UiState(
            enabled = enabled,
            endpoint = endpoint,
            apiKey = apiKey,
            targetLanguage = targetLanguage,
            saved = saved,
        )
}
