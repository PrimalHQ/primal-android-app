package net.primal.android.settings.connected.details.local

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.core.errors.UiError
import net.primal.android.navigation.identifierOrThrow
import net.primal.android.settings.connected.model.SessionUi
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.domain.account.model.TrustLevel
import net.primal.domain.account.repository.LocalAppRepository
import net.primal.domain.account.repository.SessionRepository

@HiltViewModel
class LocalAppDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val localAppRepository: LocalAppRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val identifier: String = savedStateHandle.identifierOrThrow

    private val _state = MutableStateFlow(LocalAppDetailsContract.UiState(identifier = identifier))
    val state = _state.asStateFlow()
    private fun setState(reducer: LocalAppDetailsContract.UiState.() -> LocalAppDetailsContract.UiState) =
        _state.getAndUpdate(reducer)

    private val _effect = Channel<LocalAppDetailsContract.SideEffect>()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: LocalAppDetailsContract.SideEffect) = viewModelScope.launch { _effect.send(effect) }

    private val events = MutableSharedFlow<LocalAppDetailsContract.UiEvent>()
    fun setEvent(event: LocalAppDetailsContract.UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeApp()
        observeRecentSessions()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    LocalAppDetailsContract.UiEvent.DeleteConnection -> deleteApp()
                    LocalAppDetailsContract.UiEvent.DismissError -> setState { copy(error = null) }
                    is LocalAppDetailsContract.UiEvent.UpdateTrustLevel -> updateTrustLevel(event.trustLevel)
                }
            }
        }

    private fun observeApp() =
        viewModelScope.launch {
            localAppRepository.observeApp(identifier).collect { app ->
                setState {
                    copy(
                        appPackageName = app?.packageName,
                        trustLevel = app?.trustLevel ?: TrustLevel.Low,
                        loading = false,
                    )
                }
            }
        }

    private fun observeRecentSessions() =
        viewModelScope.launch {
            sessionRepository.observeSessionsByApp(appIdentifier = identifier)
                .collect { sessions ->
                    setState {
                        copy(
                            recentSessions = sessions.map {
                                SessionUi(
                                    sessionId = it.sessionId,
                                    startedAt = it.sessionStartedAt,
                                )
                            },
                            lastSessionStartedAt = sessions.firstOrNull()?.sessionStartedAt,
                        )
                    }
                }
        }

    private fun updateTrustLevel(trustLevel: TrustLevel) {
        viewModelScope.launch {
            localAppRepository.updateTrustLevel(identifier = identifier, trustLevel = trustLevel)
                .onFailure {
                    setState { copy(error = UiError.GenericError(it.message)) }
                }
        }
    }

    private fun deleteApp() {
        viewModelScope.launch {
            localAppRepository.deleteApp(identifier)
                .onSuccess {
                    setEffect(LocalAppDetailsContract.SideEffect.ConnectionDeleted)
                }
                .onFailure {
                    setState { copy(error = UiError.GenericError(it.message)) }
                }
        }
    }
}
