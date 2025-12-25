package net.primal.android.settings.connected.details.remote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.core.errors.UiError
import net.primal.android.core.push.PushNotificationsTokenUpdater
import net.primal.android.navigation.clientPubKeyOrThrow
import net.primal.android.nostrconnect.handler.RemoteSignerSessionHandler
import net.primal.android.settings.connected.details.remote.RemoteAppDetailsContract.SideEffect
import net.primal.android.settings.connected.details.remote.RemoteAppDetailsContract.UiEvent
import net.primal.android.settings.connected.details.remote.RemoteAppDetailsContract.UiState
import net.primal.android.settings.connected.model.SessionUi
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.onFailure
import net.primal.domain.account.model.AppSessionState
import net.primal.domain.account.model.TrustLevel
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.SessionRepository

@HiltViewModel
class RemoteAppDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: DispatcherProvider,
    private val connectionRepository: ConnectionRepository,
    private val sessionRepository: SessionRepository,
    private val sessionHandler: RemoteSignerSessionHandler,
    private val tokenUpdater: PushNotificationsTokenUpdater,
) : ViewModel() {

    private val clientPubKey: String = savedStateHandle.clientPubKeyOrThrow
    private var activeSessionId: String? = null

    private val _state = MutableStateFlow(UiState(clientPubKey = clientPubKey))
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
                    UiEvent.DeleteConnection -> deleteConnection()
                    is UiEvent.EditName -> updateAppName(event.name)
                    UiEvent.StartSession -> startSession()
                    UiEvent.EndSession -> endSession()
                    UiEvent.DismissError -> setState { copy(error = null) }
                    is UiEvent.UpdateTrustLevel -> updateTrustLevel(event.trustLevel)
                }
            }
        }

    private fun observeConnection() =
        viewModelScope.launch {
            connectionRepository.observeConnection(clientPubKey = clientPubKey).collect { connection ->
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
            connectionRepository.updateTrustLevel(clientPubKey, trustLevel)
                .onFailure {
                    setState { copy(error = UiError.GenericError(it.message)) }
                }
        }
    }

    private fun observeActiveSession() =
        viewModelScope.launch {
            sessionRepository.observeOngoingSessionForConnection(clientPubKey).collect { session ->
                activeSessionId = session?.sessionId
                setState {
                    copy(
                        sessionState = session?.sessionState ?: AppSessionState.Ended,
                    )
                }
            }
        }

    private fun observeRecentSessions() =
        viewModelScope.launch {
            sessionRepository.observeSessionsByAppIdentifier(appIdentifier = clientPubKey).collect { sessions ->
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
                connectionRepository.deleteConnectionAndData(clientPubKey)
                // Launching in a new scope to survive view model destruction
                CoroutineScope(dispatcherProvider.io()).launch {
                    runCatching { tokenUpdater.updateTokenForRemoteSigner() }
                }
            }.onSuccess {
                setEffect(SideEffect.ConnectionDeleted)
            }.onFailure {
                setState { copy(error = UiError.GenericError(it.message)) }
            }
        }
    }

    private fun updateAppName(name: String) {
        viewModelScope.launch {
            runCatching {
                connectionRepository.updateConnectionName(clientPubKey, name)
            }.onFailure {
                setState { copy(error = UiError.GenericError(it.message)) }
            }
        }
    }

    private fun updateAutoStartSession(enabled: Boolean) {
        viewModelScope.launch {
            runCatching {
                connectionRepository.updateConnectionAutoStart(clientPubKey, enabled)
            }.onSuccess {
                runCatching { tokenUpdater.updateTokenForRemoteSigner() }
            }.onFailure {
                setState { copy(error = UiError.GenericError(it.message)) }
            }
        }
    }

    private fun startSession() {
        viewModelScope.launch {
            sessionHandler.startSession(clientPubKey)
                .onFailure {
                    setState { copy(error = UiError.GenericError(it.message)) }
                }
        }
    }

    private fun endSession() {
        viewModelScope.launch {
            activeSessionId?.let {
                sessionHandler.endSessions(listOf(it))
                    .onFailure {
                        setState { copy(error = UiError.GenericError(it.message)) }
                    }
            }
        }
    }
}
