package net.primal.android.settings.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import net.primal.android.theme.active.ActiveThemeStore
import javax.inject.Inject

@HiltViewModel
class AppearanceSettingsViewModel @Inject constructor(
    private val activeThemeStore: ActiveThemeStore
) : ViewModel() {
    private val _state = MutableStateFlow(AppearanceSettingsContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: AppearanceSettingsContract.UiState.() -> AppearanceSettingsContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val _event: MutableSharedFlow<AppearanceSettingsContract.UiEvent> = MutableSharedFlow()
    fun setEvent(event: AppearanceSettingsContract.UiEvent) {
        viewModelScope.launch { _event.emit(event) }
    }

    init {
        observeActiveThemeStore()
        observeEvents()
    }

    private fun observeActiveThemeStore() = viewModelScope.launch {
        activeThemeStore.userThemeState.mapNotNull { it }.collect {
            setState { copy(selectedThemeName = it.themeName) }
        }
    }

    private fun observeEvents() = viewModelScope.launch {
        _event.collect { event ->
            when (event) {
                is AppearanceSettingsContract.UiEvent.SelectedThemeChanged -> selectedThemeChanged(
                    themeName = event.themeName
                )
            }
        }
    }

    private suspend fun selectedThemeChanged(themeName: String) {
        activeThemeStore.setUserTheme(theme = themeName)
    }
}