package net.primal.android.nostrconnect.list

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
import net.primal.android.drawer.multiaccount.model.asUserAccountUi
import net.primal.android.nostrconnect.list.ActiveSessionsContract.SideEffect
import net.primal.android.nostrconnect.list.ActiveSessionsContract.UiEvent
import net.primal.android.nostrconnect.list.ActiveSessionsContract.UiState
import net.primal.android.nostrconnect.model.ActiveSessionUi
import net.primal.android.nostrconnect.model.asUi
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.asKeyPair
import net.primal.domain.account.repository.SessionRepository
import timber.log.Timber

@HiltViewModel
class ActiveSessionsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val credentialsStore: CredentialsStore,
    private val userAccountsStore: UserAccountsStore,
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
            sessionRepository.observeActiveSessions(signerPubKey = signerKeyPair.pubKey)
                .collect { appSessions ->
                    val userAccounts = userAccountsStore.userAccounts.value
                    val userAccountsMap = userAccounts.associateBy { it.pubkey }
                    val uiSessions = appSessions.mapNotNull { appSession ->
                        userAccountsMap[appSession.userPubKey]?.let { userAccount ->
                            appSession.asUi(userAccount = userAccount.asUserAccountUi())
                        }
                    }
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
                    is UiEvent.SettingsClick -> handleSettingsClick()
                    is UiEvent.DismissError -> setState { copy(error = null) }
                }
            }
        }
    }

    private fun handleSettingsClick() {
        val selectedSessionIds = state.value.selectedSessions
        if (selectedSessionIds.size == 1) {
            val sessionId = selectedSessionIds.first()
            val session = state.value.sessions.find { it.sessionId == sessionId }
            setEffect(SideEffect.NavigateToConnectedApps(connectionId = session?.connectionId))
        } else {
            setEffect(SideEffect.NavigateToConnectedApps(connectionId = null))
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
            runCatching {
                val selectedSessions = state.value.selectedSessions
                Timber.d("Disconnecting sessions: $selectedSessions")
                selectedSessions.forEach { sessionId -> sessionRepository.endSession(sessionId) }
            }
                .onSuccess {
                    setEffect(SideEffect.SessionsDisconnected)
                }
                .onFailure {
                    Timber.e(it, "Error disconnecting sessions")
                }
                .also {
                    setState { copy(disconnecting = false) }
                }
        }
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
