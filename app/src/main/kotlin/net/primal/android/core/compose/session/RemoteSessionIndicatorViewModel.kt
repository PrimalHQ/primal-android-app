package net.primal.android.core.compose.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.asKeyPair
import net.primal.domain.account.repository.ConnectionRepository

@HiltViewModel
class RemoteSessionIndicatorViewModel @Inject constructor(
    private val credentialsStore: CredentialsStore,
    private val connectionRepository: ConnectionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RemoteSessionIndicatorContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: RemoteSessionIndicatorContract.UiState.() -> RemoteSessionIndicatorContract.UiState) =
        _state.getAndUpdate(reducer)

    init {
        observeRemoteSignerConnections()
    }

    private fun observeRemoteSignerConnections() =
        viewModelScope.launch {
            val signerKeyPair = credentialsStore.getOrCreateInternalSignerCredentials().asKeyPair()
            connectionRepository.observeAllConnections(signerPubKey = signerKeyPair.pubKey)
                .collect { connections ->
                    setState { copy(isRemoteSessionActive = connections.isNotEmpty()) }
                }
        }
}
