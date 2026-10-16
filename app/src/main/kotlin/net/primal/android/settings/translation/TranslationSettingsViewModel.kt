package net.primal.android.settings.translation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.primal.android.notes.translation.NoteTranslationConfig
import net.primal.android.settings.translation.TranslationSettingsContract.UiEvent
import net.primal.android.settings.translation.TranslationSettingsContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.NoteTranslationSettings
import net.primal.android.user.repository.UserRepository

@HiltViewModel
class TranslationSettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = viewModelScope.launch { _uiState.update(reducer) }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeUserAccount()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is UiEvent.UpdateEnabled -> persist { copy(enabled = event.enabled) }
                    is UiEvent.UpdateTargetLanguage -> persist { copy(targetLanguageCode = event.languageCode) }
                    is UiEvent.UpdateProvider -> persist { copy(provider = event.provider) }
                    is UiEvent.UpdateLibreTranslateUrl -> persist { copy(libreTranslateUrl = event.url) }
                    is UiEvent.UpdateApiKey -> persist { copy(apiKey = event.apiKey) }
                }
            }
        }
    }

    private fun observeUserAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect { account ->
                setState { account.contentDisplaySettings.noteTranslation.toUiState() }
            }
        }

    private fun persist(reducer: NoteTranslationSettings.() -> NoteTranslationSettings) {
        val updated = currentSettings().reducer()
        setState { updated.toUiState() }
        viewModelScope.launch {
            userRepository.updateContentDisplaySettings(userId = activeAccountStore.activeUserId()) {
                copy(noteTranslation = updated)
            }
        }
    }

    private fun currentSettings(): NoteTranslationSettings =
        NoteTranslationSettings(
            enabled = _uiState.value.enabled,
            targetLanguageCode = _uiState.value.targetLanguageCode,
            provider = _uiState.value.provider,
            libreTranslateUrl = _uiState.value.libreTranslateUrl,
            apiKey = _uiState.value.apiKey,
        )
}

private fun NoteTranslationSettings.toUiState() =
    UiState(
        enabled = enabled,
        targetLanguageCode = targetLanguageCode,
        provider = provider,
        libreTranslateUrl = libreTranslateUrl,
        apiKey = apiKey,
        ready = NoteTranslationConfig.isReady(this),
    )
