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
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.NoteAppearance
import net.primal.android.user.repository.UserRepository

class AppearanceSettingsViewModel @AssistedInject constructor(
    @Assisted private var lastUserPickedPrimalTheme: PrimalTheme,
    private val activeThemeStore: ActiveThemeStore,
    private val activeAccountStore: ActiveAccountStore,
    private val userRepository: UserRepository,
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
                    is AppearanceSettingsContract.UiEvent.ChangeTheme ->
                        setTheme(themeName = event.themeName)

                    is AppearanceSettingsContract.UiEvent.ToggleAutoAdjustDarkTheme -> {
                        if (event.enabled) {
                            setTheme(themeName = "")
                        } else {
                            val accent = lastUserPickedPrimalTheme.accent
                            val newTheme = findThemeOrDefault(isDark = event.isSystemInDarkTheme, accent = accent)
                            setTheme(themeName = newTheme.themeName)
                        }
                    }

                    is AppearanceSettingsContract.UiEvent.ChangeNoteAppearance -> {
                        setNoteAppearance(noteAppearance = event.noteAppearance)
                    }
                }
            }
        }

    private suspend fun setTheme(themeName: String) {
        activeThemeStore.setUserTheme(theme = themeName)
        val theme = PrimalTheme.valueOf(themeName = themeName)
        if (theme != null) {
            lastUserPickedPrimalTheme = theme
        }
    }

    private fun setNoteAppearance(noteAppearance: NoteAppearance) {
        viewModelScope.launch {
            userRepository.updateContentDisplaySettings(userId = activeAccountStore.activeUserId()) {
                copy(noteAppearance = noteAppearance)
            }
        }
    }
}
