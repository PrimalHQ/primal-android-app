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
import net.primal.android.core.translation.TranslationPreferences
import net.primal.android.settings.translation.TranslationSettingsContract.UiEvent
import net.primal.android.settings.translation.TranslationSettingsContract.UiState

@HiltViewModel
class TranslationSettingsViewModel @Inject constructor(
    private val translationPreferences: TranslationPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = viewModelScope.launch { _uiState.update(reducer) }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observePreferences()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is UiEvent.SelectLanguage -> handleLanguageSelection(event.languageCode)
                }
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            translationPreferences.targetLanguage.collect { languageCode ->
                setState { copy(selectedLanguageCode = languageCode) }
            }
        }
    }

    private fun handleLanguageSelection(languageCode: String) {
        translationPreferences.setTargetLanguage(languageCode)
        setState { copy(selectedLanguageCode = languageCode) }
    }
}
