package net.primal.android.settings.connected.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.navigation.connectionIdOrThrow
import net.primal.android.nostrconnect.handler.RemoteSignerSessionHandler
import net.primal.android.settings.connected.details.ConnectedAppDetailsContract.SessionUi
import net.primal.android.settings.connected.details.ConnectedAppDetailsContract.SideEffect
import net.primal.android.settings.connected.details.ConnectedAppDetailsContract.UiEvent
import net.primal.android.settings.connected.details.ConnectedAppDetailsContract.UiState
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.SessionRepository

@HiltViewModel
class ConnectedAppDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val connectionRepository: ConnectionRepository,
    private val sessionRepository: SessionRepository,
    private val sessionHandler: RemoteSignerSessionHandler,
) : ViewModel() {

    private val connectionId: String = savedStateHandle.connectionIdOrThrow

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val _effect = Channel<SideEffect>()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effect.send(effect) }

    fun setEvent(event: UiEvent) {
        viewModelScope.launch {
            when (event) {
                is UiEvent.AutoStartSessionChange -> updateAutoStartSession(event.enabled)
                UiEvent.DeleteConnection -> setState { copy(confirmingDeletion = true) }
                UiEvent.ConfirmDeletion -> deleteConnection()
                UiEvent.DismissDeletionConfirmation -> setState { copy(confirmingDeletion = false) }
                UiEvent.EditName -> setState { copy(editingName = true) }
                is UiEvent.NameChange -> updateAppName(event.name)
                UiEvent.DismissEditNameDialog -> setState { copy(editingName = false) }
                UiEvent.StartSession -> startSession()
                UiEvent.EndSession -> endSession()
            }
        }
    }

    init {
        observeConnection()
        observeActiveSession()
        observeRecentSessions()
    }

    private fun observeConnection() =
        viewModelScope.launch {
            connectionRepository.observeConnection(connectionId = connectionId).collect { connection ->
                setState {
                    copy(
                        appName = connection?.name,
                        appIconUrl = connection?.image,
                        autoStartSession = connection?.autoStart ?: false,
                        loading = false,
                    )
                }
            }
        }

    private fun observeActiveSession() =
        viewModelScope.launch {
            sessionRepository.observeActiveSessionForConnection(connectionId).collect { session ->
                setState {
                    copy(
                        isSessionActive = session != null,
                        activeSessionId = session?.sessionId,
                    )
                }
            }
        }

    private fun observeRecentSessions() =
        viewModelScope.launch {
            sessionRepository.observeSessionsByConnectionId(connectionId).collect { sessions ->
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

    private suspend fun deleteConnection() {
        connectionRepository.deleteConnection(connectionId)
        setState { copy(confirmingDeletion = false) }
        setEffect(SideEffect.ConnectionDeleted)
    }

    private fun updateAppName(name: String) {
        viewModelScope.launch {
            connectionRepository.updateConnectionName(connectionId, name)
            setState { copy(editingName = false) }
        }
    }

    private fun updateAutoStartSession(enabled: Boolean) {
        viewModelScope.launch {
            connectionRepository.updateConnectionAutoStart(connectionId, enabled)
        }
    }

    private fun startSession() {
        viewModelScope.launch {
            sessionHandler.startSession(connectionId)
        }
    }

    private fun endSession() {
        viewModelScope.launch {
            val activeSessionId = state.value.activeSessionId
            if (activeSessionId != null) {
                sessionHandler.endSession(activeSessionId)
            }
        }
    }
}
