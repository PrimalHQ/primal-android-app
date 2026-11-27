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
import net.primal.android.notes.translation.NoteTranslationManager
import net.primal.android.notes.translation.TranslationEndpoints
import net.primal.android.notes.translation.TranslationErrorCode
import net.primal.android.notes.translation.TranslationException
import net.primal.android.settings.translation.TranslationSettingsContract.TestOutcome
import net.primal.android.settings.translation.TranslationSettingsContract.UiEvent
import net.primal.android.settings.translation.TranslationSettingsContract.UiState

@HiltViewModel
class TranslationSettingsViewModel @Inject constructor(
    private val translationManager: NoteTranslationManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = viewModelScope.launch { _uiState.update(reducer) }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeSettings()
        refreshCacheCount()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.UpdateEnabled -> translationManager.settingsStore.setEnabled(it.enabled)
                    is UiEvent.UpdateEndpointDraft -> setState { copy(endpointDraft = it.value, testOutcome = null) }
                    UiEvent.ConfirmEndpoint -> confirmEndpoint()
                    is UiEvent.UpdateApiKeyDraft -> setState { copy(apiKeyDraft = it.value) }
                    UiEvent.ConfirmApiKey -> translationManager.settingsStore.setApiKey(_uiState.value.apiKeyDraft.trim())
                    is UiEvent.UpdateTargetLanguageDraft -> setState { copy(targetLanguageDraft = it.value, testOutcome = null) }
                    UiEvent.ConfirmTargetLanguage -> confirmTargetLanguage()
                    is UiEvent.SelectLanguage -> translationManager.settingsStore.setTargetLanguage(it.code)
                    UiEvent.TestConnection -> testConnection()
                    UiEvent.ClearCache -> {
                        translationManager.clearCache()
                        refreshCacheCount()
                    }
                }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            translationManager.settingsStore.settingsFlow.collect { settings ->
                setState {
                    copy(
                        enabled = settings.enabled,
                        endpoint = settings.endpoint,
                        apiKey = settings.apiKey,
                        targetLanguage = settings.targetLanguage,
                        endpointDraft = if (endpointDraft == endpoint) settings.endpoint else endpointDraft,
                        apiKeyDraft = if (apiKeyDraft == apiKey) settings.apiKey else apiKeyDraft,
                        targetLanguageDraft = if (targetLanguageDraft == targetLanguage) {
                            settings.targetLanguage
                        } else {
                            targetLanguageDraft
                        },
                    )
                }
            }
        }
    }

    private fun confirmEndpoint() {
        val draft = _uiState.value.endpointDraft
        translationManager.settingsStore.setEndpoint(TranslationEndpoints.normalizeEndpoint(draft))
    }

    private fun confirmTargetLanguage() {
        // Preserve the tag's casing ("zh-Hans", "pt-BR"): endpoints match
        // language codes case-sensitively, and the service sends the tag
        // verbatim.
        val code = _uiState.value.targetLanguageDraft.trim()
        if (code.isNotEmpty()) {
            translationManager.settingsStore.setTargetLanguage(code)
        }
    }

    private fun testConnection() {
        viewModelScope.launch {
            setState { copy(testingConnection = true, testOutcome = null) }
            try {
                val languages = translationManager.listLanguages()
                setState {
                    copy(
                        testingConnection = false,
                        testOutcome = TestOutcome.Success,
                        languages = languages,
                    )
                }
            } catch (e: TranslationException) {
                setState {
                    copy(
                        testingConnection = false,
                        testOutcome = when (e.code) {
                            TranslationErrorCode.NotConfigured, TranslationErrorCode.Disabled ->
                                TestOutcome.NotConfigured
                            TranslationErrorCode.Unreachable -> TestOutcome.Unreachable
                            else -> TestOutcome.ProviderError
                        },
                    )
                }
            } catch (e: Exception) {
                setState { copy(testingConnection = false, testOutcome = TestOutcome.ProviderError) }
            }
        }
    }

    private fun refreshCacheCount() {
        setState { copy(cacheCount = translationManager.cache.count) }
    }
}
