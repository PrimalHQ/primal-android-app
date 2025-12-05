package net.primal.android.settings.connected

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.settings.connected.ConnectedAppsContract.UiState
import net.primal.android.settings.connected.model.asAppConnectionUi
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.asKeyPair
import net.primal.domain.account.repository.ConnectionRepository
import net.primal.domain.account.repository.SessionRepository

@HiltViewModel
class ConnectedAppsViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val sessionRepository: SessionRepository,
    private val credentialsStore: CredentialsStore,
    private val accountsStore: UserAccountsStore,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    init {
        observeConnections()
        observeActiveSessions()
    }

    private fun observeConnections() {
        viewModelScope.launch {
            val signerKeyPair = credentialsStore.getOrCreateInternalSignerCredentials().asKeyPair()
            connectionRepository.observeAllConnections(signerPubKey = signerKeyPair.pubKey)
                .collect { connections ->
                    val userAccounts = accountsStore.userAccounts.value
                    val userAccountMap = userAccounts.associateBy { it.pubkey }

                    val uiConnections = connections.map { connection ->
                        val userAccount = userAccountMap[connection.userPubKey]
                        connection.asAppConnectionUi(userAccount = userAccount)
                    }
                    setState { copy(connections = uiConnections, loading = false) }
                }
        }
    }

    private fun observeActiveSessions() {
        viewModelScope.launch {
            val signerKeyPair = credentialsStore.getOrCreateInternalSignerCredentials().asKeyPair()
            sessionRepository.observeActiveSessions(signerPubKey = signerKeyPair.pubKey)
                .collect { activeSessions ->
                    val activeClientPubKeys = activeSessions.map { it.clientPubKey }.toSet()
                    setState { copy(activeClientPubKeys = activeClientPubKeys) }
                }
        }
    }
}
