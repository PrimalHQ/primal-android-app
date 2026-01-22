package net.primal.android.nostrconnect.active

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.drawer.multiaccount.model.asUserAccountUi
import net.primal.android.nostrconnect.active.ActiveSessionsContract.SideEffect
import net.primal.android.nostrconnect.active.ActiveSessionsContract.UiEvent
import net.primal.android.nostrconnect.active.ActiveSessionsContract.UiState
import net.primal.android.nostrconnect.handler.RemoteSignerSessionHandler
import net.primal.android.nostrconnect.model.ActiveSessionUi
import net.primal.android.nostrconnect.model.asUi
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.asKeyPair
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.data.account.repository.manager.RemoteAppConnectionManager
import net.primal.domain.account.repository.SessionRepository

@HiltViewModel
class ActiveSessionsViewModel @Inject constructor(
    private val signerSessionHandler: RemoteSignerSessionHandler,
    private val sessionRepository: SessionRepository,
    private val credentialsStore: CredentialsStore,
    private val userAccountsStore: UserAccountsStore,
    private val remoteAppConnectionManager: RemoteAppConnectionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _effect = Channel<SideEffect>()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }

    init {
        observeActiveSessions()
        observeEvents()
    }

    private fun observeActiveSessions() {
        viewModelScope.launch {
            val signerKeyPair = credentialsStore.getOrCreateInternalSignerCredentials().asKeyPair()
            combine(
                sessionRepository.observeOngoingSessions(signerPubKey = signerKeyPair.pubKey),
                remoteAppConnectionManager.sessionStates,
            ) { appSessions, connectionStates ->
                val userAccounts = userAccountsStore.userAccounts.value
                val userAccountsMap = userAccounts.associateBy { it.pubkey }
                appSessions.mapNotNull { appSession ->
                    userAccountsMap[appSession.userPubKey]?.let { userAccount ->
                        appSession.asUi(userAccount = userAccount.asUserAccountUi()).copy(
                            connectionStatus = connectionStates[appSession.sessionId]?.status,
                        )
                    }
                }
            }.collect { uiSessions ->
                setState {
                    copy(
                        sessions = uiSessions,
                        selectedSessions = resolveSelectedSessions(this.selectedSessions, uiSessions),
                    )
                }
            }
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.SessionClick -> handleSessionClick(it.sessionId)
                    is UiEvent.SelectAllClick -> handleSelectAllClick()
                    is UiEvent.DisconnectClick -> handleDisconnectClick()
                    is UiEvent.ReconnectClick -> handleReconnectClick()
                    is UiEvent.DismissError -> setState { copy(error = null) }
                }
            }
        }
    }

    private fun handleSessionClick(sessionId: String) {
        setState {
            val selected = this.selectedSessions.toMutableSet()
            if (selected.contains(sessionId)) {
                selected.remove(sessionId)
            } else {
                selected.add(sessionId)
            }
            copy(selectedSessions = selected)
        }
    }

    private fun handleSelectAllClick() {
        setState {
            if (allSessionsSelected) {
                copy(selectedSessions = emptySet())
            } else {
                copy(selectedSessions = sessions.map { it.sessionId }.toSet())
            }
        }
    }

    private fun handleDisconnectClick() {
        viewModelScope.launch {
            setState { copy(disconnecting = true) }
            signerSessionHandler.endSessions(sessionIds = state.value.selectedSessions.toList())
                .onSuccess { setEffect(SideEffect.SessionsDisconnected) }
                .onFailure { Napier.e(message = "Error disconnecting sessions", throwable = it) }

            setState { copy(disconnecting = false) }
        }
    }

    private fun handleReconnectClick() {
        remoteAppConnectionManager.requestReconnection(sessionIds = state.value.selectedSessions.toList())
    }

    private fun resolveSelectedSessions(
        currentSelection: Set<String>,
        newSessions: List<ActiveSessionUi>,
    ): Set<String> {
        val validSelection = currentSelection.intersect(newSessions.map { it.sessionId }.toSet())
        return if (validSelection.isEmpty() && newSessions.isNotEmpty()) {
            setOf(newSessions.first().sessionId)
        } else {
            validSelection
        }
    }
}
