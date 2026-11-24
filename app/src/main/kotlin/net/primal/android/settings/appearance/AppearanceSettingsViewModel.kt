package net.primal.android.settings.appearance

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.notes.feed.note.ui.TranslationPreferences
import net.primal.android.theme.active.ActiveThemeStore
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.theme.findThemeOrDefault
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.ContentAppearance
import net.primal.android.user.repository.UserRepository

class AppearanceSettingsViewModel @AssistedInject constructor(
    @Assisted private var lastUserPickedPrimalTheme: PrimalTheme,
    @ApplicationContext private val context: Context,
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
        setState {
            copy(translationLanguage = TranslationPreferences.getTranslateLanguage(context))
        }
    }

    private fun initThemes() =
        viewModelScope.launch {
            setState {
                copy(
                    themes = listOf(
                        PrimalTheme.Midnight,
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
                            val newTheme = findThemeOrDefault(isDark = event.isSystemInDarkTheme)
                            setTheme(themeName = newTheme.themeName)
                        }
                    }

                    is AppearanceSettingsContract.UiEvent.ChangeContentAppearance -> {
                        setContentAppearance(contentAppearance = event.contentAppearance)
                    }

                    is AppearanceSettingsContract.UiEvent.ChangeTranslationLanguage -> {
                        TranslationPreferences.setTranslateLanguage(context, event.language)
                        setState { copy(translationLanguage = event.language) }
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

    private fun setContentAppearance(contentAppearance: ContentAppearance) {
        viewModelScope.launch {
            userRepository.updateContentDisplaySettings(userId = activeAccountStore.activeUserId()) {
                copy(contentAppearance = contentAppearance)
            }
        }
    }
}
