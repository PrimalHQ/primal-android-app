package net.primal.android.settings.connected

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.service.PrimalRemoteSignerService
import net.primal.android.settings.connected.model.asAppConnectionUi
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.asKeyPair
import net.primal.domain.account.repository.ConnectionRepository
import timber.log.Timber

@HiltViewModel
class ConnectedAppsViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val credentialsStore: CredentialsStore,
    private val accountsStore: UserAccountsStore,
) : ViewModel() {

    private val _state = MutableStateFlow(ConnectedAppsContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: ConnectedAppsContract.UiState.() -> ConnectedAppsContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<ConnectedAppsContract.UiEvent>()
    fun setEvent(event: ConnectedAppsContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    init {
        observeConnections()
        observeRemoteSignerStatus()
    }

    private fun observeConnections() {
        viewModelScope.launch {
            runCatching {
                val signerKeyPair = credentialsStore.getOrCreateInternalSignerCredentials().asKeyPair()
                connectionRepository.observeAllConnections(signerPubKey = signerKeyPair.pubKey)
                    .collect { connections ->
                        val userAccounts = accountsStore.userAccounts.value
                        val userAccountMap = userAccounts.associateBy { it.pubkey }
                        val isRemoteSignerActive = PrimalRemoteSignerService.isServiceRunning.value

                        val uiConnections = connections.map { connection ->
                            val userAccount = userAccountMap[connection.userPubKey]
                            connection.asAppConnectionUi(userAccount, isActive = isRemoteSignerActive)
                        }
                        setState { copy(connections = uiConnections, loading = false) }
                    }
            }.onFailure {
                Timber.w(it)
                setState { copy(loading = false) }
            }
        }
    }

    private fun observeRemoteSignerStatus() {
        viewModelScope.launch {
            PrimalRemoteSignerService.isServiceRunning.collect { isRemoteSignerActive ->
                setState {
                    val updatedConnections = this.connections.map { it.copy(isActive = isRemoteSignerActive) }
                    copy(connections = updatedConnections)
                }
            }
        }
    }
}
