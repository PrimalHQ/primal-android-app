package net.primal.android.settings.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.theme.active.ActiveThemeStore
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.theme.findThemeOrDefault

class AppearanceSettingsViewModel @AssistedInject constructor(
    @Assisted private var lastUserPickedPrimalTheme: PrimalTheme,
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
            setState {
                copy(
                    themes = listOf(
                        PrimalTheme.Sunset,
                        PrimalTheme.Midnight,
                        PrimalTheme.Sunrise,
                        PrimalTheme.Ice,
                    ),
                )
            }
        }

    private fun observeActiveThemeStore() =
        viewModelScope.launch {
            activeThemeStore.userThemeState.collect {
                setState { copy(selectedThemeName = it?.themeName) }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is AppearanceSettingsContract.UiEvent.SelectedThemeChanged ->
                        selectedThemeChanged(themeName = event.themeName)

                    is AppearanceSettingsContract.UiEvent.ToggleAutoAdjustDarkTheme -> {
                        if (event.enabled) {
                            selectedThemeChanged(themeName = "")
                        } else {
                            val accent = lastUserPickedPrimalTheme.accent
                            val newTheme = findThemeOrDefault(isDark = event.isSystemInDarkTheme, accent = accent)
                            selectedThemeChanged(themeName = newTheme.themeName)
                        }
                    }
                }
            }
        }

    private suspend fun selectedThemeChanged(themeName: String) {
        activeThemeStore.setUserTheme(theme = themeName)
        val theme = PrimalTheme.valueOf(themeName = themeName)
        if (theme != null) {
            lastUserPickedPrimalTheme = theme
        }
    }
}
