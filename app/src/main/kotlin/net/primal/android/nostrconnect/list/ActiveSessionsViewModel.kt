package net.primal.android.nostrconnect.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
import net.primal.android.user.accounts.UserAccountsStore
import timber.log.Timber

@HiltViewModel
class ActiveSessionsViewModel @Inject constructor(
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
        loadMockSessions()
        observeEvents()
    }

    private fun loadMockSessions() {
        viewModelScope.launch {
            val userAccounts = userAccountsStore.userAccounts.value
            if (userAccounts.isEmpty()) return@launch

            val mockSessions = buildList {
                add(
                    ActiveSessionsContract.NwcSessionUi(
                        connectionId = "mock1",
                        appName = "Primal Web App",
                        appUrl = "https://www.primal.net",
                        appImageUrl = "https://primal.net/assets/favicon-51789dff.ico",
                        userAccount = userAccounts.first().asUserAccountUi(),
                    ),
                )
                add(
                    ActiveSessionsContract.NwcSessionUi(
                        connectionId = "mock2",
                        appName = "Highlighter",
                        appUrl = "https://highlighter.com",
                        appImageUrl = "https://primal.net/assets/favicon-51789dff.ico",
                        userAccount = userAccounts.getOrElse(1) { userAccounts.first() }.asUserAccountUi(),
                    ),
                )
                add(
                    ActiveSessionsContract.NwcSessionUi(
                        connectionId = "mock3",
                        appName = "Nostrarious",
                        appUrl = "https://nostrarious.io",
                        appImageUrl = "https://primal.net/assets/favicon-51789dff.ico",
                        userAccount = userAccounts.first().asUserAccountUi(),
                    ),
                )
                add(
                    ActiveSessionsContract.NwcSessionUi(
                        connectionId = "mock4",
                        appName = "Purple Palace",
                        appUrl = "https://purplepalace.com",
                        appImageUrl = "https://primal.net/assets/favicon-51789dff.ico",
                        userAccount = userAccounts.first().asUserAccountUi(),
                    ),
                )
                add(
                    ActiveSessionsContract.NwcSessionUi(
                        connectionId = "mock5",
                        appName = "Birdcage web app",
                        appUrl = "https://www.birdcage.biz",
                        appImageUrl = "https://primal.net/assets/favicon-51789dff.ico",
                        userAccount = userAccounts.getOrElse(1) { userAccounts.first() }.asUserAccountUi(),
                    ),
                )
                add(
                    ActiveSessionsContract.NwcSessionUi(
                        connectionId = "mock6",
                        appName = "Lamirp app",
                        appUrl = "https://www.primal.net",
                        appImageUrl = "https://primal.net/assets/favicon-51789dff.ico",
                        userAccount = userAccounts.first().asUserAccountUi(),
                    ),
                )
                add(
                    ActiveSessionsContract.NwcSessionUi(
                        connectionId = "mock7",
                        appName = "Lighterhigh",
                        appUrl = "https://www.lighterhigh.com",
                        appImageUrl = "https://primal.net/assets/favicon-51789dff.ico",
                        userAccount = userAccounts.first().asUserAccountUi(),
                    ),
                )
            }
            setState { copy(sessions = mockSessions) }
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.SessionClick -> handleSessionClick(it.connectionId)
                    is UiEvent.SelectAllClick -> handleSelectAllClick()
                    is UiEvent.DisconnectClick -> handleDisconnectClick()
                    is UiEvent.DismissError -> setState { copy(error = null) }
                }
            }
        }
    }

    private fun handleSessionClick(connectionId: String) {
        setState {
            val selected = this.selectedSessions.toMutableSet()
            if (selected.contains(connectionId)) {
                selected.remove(connectionId)
            } else {
                selected.add(connectionId)
            }
            copy(selectedSessions = selected)
        }
    }

    private fun handleSelectAllClick() {
        setState {
            if (allSessionsSelected) {
                copy(selectedSessions = emptySet())
            } else {
                copy(selectedSessions = sessions.map { it.connectionId }.toSet())
            }
        }
    }

    private fun handleDisconnectClick() {
        viewModelScope.launch {
            setState { copy(disconnecting = true) }
            runCatching {
                Timber.d("Disconnecting sessions: ${state.value.selectedSessions}")
                setState {
                    val remainingSessions = sessions.filterNot { it.connectionId in selectedSessions }
                    copy(sessions = remainingSessions, selectedSessions = emptySet())
                }
                setEffect(SideEffect.SessionsDisconnected)
            }.onFailure { e ->
                Timber.e(e, "Error disconnecting sessions")
            }.also {
                setState { copy(disconnecting = false) }
            }
        }
    }
}
