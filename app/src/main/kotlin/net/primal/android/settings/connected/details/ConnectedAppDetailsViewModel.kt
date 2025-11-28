package net.primal.android.settings.connected.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.withTimeout
import net.primal.android.core.errors.UiError
import net.primal.android.core.push.PushNotificationsTokenUpdater
import net.primal.android.navigation.connectionIdOrThrow
import net.primal.android.nostrconnect.handler.RemoteSignerSessionHandler
import net.primal.android.settings.connected.details.ConnectedAppDetailsContract.SideEffect
import net.primal.android.settings.connected.details.ConnectedAppDetailsContract.UiEvent
import net.primal.android.settings.connected.details.ConnectedAppDetailsContract.UiState
import net.primal.android.settings.connected.model.SessionUi
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.account.model.TrustLevel
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.SessionRepository

@HiltViewModel
class ConnectedAppDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: DispatcherProvider,
    private val connectionRepository: ConnectionRepository,
    private val sessionRepository: SessionRepository,
    private val sessionHandler: RemoteSignerSessionHandler,
    private val tokenUpdater: PushNotificationsTokenUpdater,
) : ViewModel() {

    private val connectionId: String = savedStateHandle.connectionIdOrThrow
    private var activeSessionId: String? = null

    private val _state = MutableStateFlow(UiState(connectionId = connectionId))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val _effect = Channel<SideEffect>()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effect.send(effect) }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeConnection()
        observeActiveSession()
        observeRecentSessions()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
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
                    UiEvent.DismissError -> setState { copy(error = null) }
                    is UiEvent.UpdateTrustLevel -> updateTrustLevel(event.trustLevel)
                }
            }
        }

    private fun observeConnection() =
        viewModelScope.launch {
            connectionRepository.observeConnection(connectionId = connectionId).collect { connection ->
                setState {
                    copy(
                        appName = connection?.name,
                        appIconUrl = connection?.image,
                        autoStartSession = connection?.autoStart ?: false,
                        trustLevel = connection?.trustLevel ?: TrustLevel.Low,
                        loading = false,
                    )
                }
            }
        }

    private fun updateTrustLevel(trustLevel: TrustLevel) {
        viewModelScope.launch {
            runCatching {
                connectionRepository.updateTrustLevel(connectionId, trustLevel)
            }.onFailure {
                setState { copy(error = UiError.GenericError(it.message)) }
            }
        }
    }

    private fun observeActiveSession() =
        viewModelScope.launch {
            sessionRepository.observeActiveSessionForConnection(connectionId).collect { session ->
                activeSessionId = session?.sessionId
                setState {
                    copy(
                        isSessionActive = session != null,
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

    private fun deleteConnection() {
        viewModelScope.launch {
            runCatching {
                connectionRepository.deleteConnection(connectionId)
                // Launching in a new scope to survive view model destruction
                CoroutineScope(dispatcherProvider.io()).launch {
                    runCatching { tokenUpdater.updateTokenForRemoteSigner() }
                }
            }.onSuccess {
                setState { copy(confirmingDeletion = false) }
                setEffect(SideEffect.ConnectionDeleted)
            }.onFailure {
                setState { copy(error = UiError.GenericError(it.message)) }
            }
        }
    }

    private fun updateAppName(name: String) {
        viewModelScope.launch {
            runCatching {
                connectionRepository.updateConnectionName(connectionId, name)
            }.onSuccess {
                setState { copy(editingName = false) }
            }.onFailure {
                setState { copy(error = UiError.GenericError(it.message)) }
            }
        }
    }

    private fun updateAutoStartSession(enabled: Boolean) {
        viewModelScope.launch {
            runCatching {
                connectionRepository.updateConnectionAutoStart(connectionId, enabled)
            }.onSuccess {
                runCatching { tokenUpdater.updateTokenForRemoteSigner() }
            }.onFailure {
                setState { copy(error = UiError.GenericError(it.message)) }
            }
        }
    }

    private fun startSession() {
        viewModelScope.launch {
            runCatching {
                sessionHandler.startSession(connectionId)
            }.onFailure {
                setState { copy(error = UiError.GenericError(it.message)) }
            }
        }
    }

    private fun endSession() {
        viewModelScope.launch {
            activeSessionId?.let {
                runCatching {
                    sessionHandler.endSession(it)
                }.onFailure {
                    setState { copy(error = UiError.GenericError(it.message)) }
                }
            }
        }
    }
}
