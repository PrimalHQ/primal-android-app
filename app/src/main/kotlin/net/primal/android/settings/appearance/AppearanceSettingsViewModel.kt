package net.primal.android.settings.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.theme.active.ActiveThemeStore
import net.primal.android.theme.domain.PrimalTheme

@HiltViewModel
class AppearanceSettingsViewModel @Inject constructor(
    private val activeThemeStore: ActiveThemeStore,
) : ViewModel() {
    private val _state = MutableStateFlow(AppearanceSettingsContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: AppearanceSettingsContract.UiState.() -> AppearanceSettingsContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events: MutableSharedFlow<AppearanceSettingsContract.UiEvent> = MutableSharedFlow()
    fun setEvent(event: AppearanceSettingsContract.UiEvent) {
        viewModelScope.launch { this@AppearanceSettingsViewModel.events.emit(event) }
    }

    init {
        initThemes()
        observeActiveThemeStore()
        observeEvents()
    }

    private fun initThemes() =
        viewModelScope.launch {
            setState { copy(themes = PrimalTheme.values().toList()) }
        }

    private fun observeActiveThemeStore() =
        viewModelScope.launch {
            activeThemeStore.userThemeState.filterNotNull().collect {
                setState { copy(selectedThemeName = it.themeName) }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is AppearanceSettingsContract.UiEvent.SelectedThemeChanged -> selectedThemeChanged(
                        themeName = event.themeName,
                    )
                }
            }
        }

    private suspend fun selectedThemeChanged(themeName: String) {
        activeThemeStore.setUserTheme(theme = themeName)
    }
}
