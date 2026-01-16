package net.primal.android.settings.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.BuildConfig
import net.primal.android.core.logging.AppLogPreferences
import net.primal.android.settings.home.SettingsHomeContract.UiEvent
import net.primal.android.settings.home.SettingsHomeContract.UiState

@HiltViewModel
class SettingsHomeViewModel @Inject constructor(
    private val appLogPreferences: AppLogPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(
        UiState(
            version = BuildConfig.VERSION_NAME,
            developerToolsEnabled = appLogPreferences.developerToolsEnabled,
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private var versionTapCount = 0
    private var lastVersionTapTime = 0L

    init {
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    UiEvent.VersionTapped -> handleVersionTapped()
                }
            }
        }

    private fun handleVersionTapped() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastVersionTapTime > TAP_TIMEOUT_MS) {
            versionTapCount = 0
        }
        lastVersionTapTime = currentTime
        versionTapCount++

        if (versionTapCount >= TAPS_REQUIRED) {
            versionTapCount = 0
            appLogPreferences.setDeveloperToolsEnabled(true)
            setState { copy(developerToolsEnabled = true) }
        }
    }

    companion object {
        private const val TAPS_REQUIRED = 3
        private const val TAP_TIMEOUT_MS = 1000L
    }
}
