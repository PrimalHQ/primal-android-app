package net.primal.android.settings.connected.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.navigation.connectionIdOrThrow
import net.primal.domain.account.repository.ConnectionRepository

@HiltViewModel
class ConnectedAppDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val connectionRepository: ConnectionRepository,
) : ViewModel() {

    private val connectionId: String = savedStateHandle.connectionIdOrThrow

    private val _state = MutableStateFlow(ConnectedAppDetailsContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: ConnectedAppDetailsContract.UiState.() -> ConnectedAppDetailsContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    init {
        observeConnectionDetails()
    }

    private fun observeConnectionDetails() {
        viewModelScope.launch {
            connectionRepository.observeConnection(connectionId = connectionId)
                .collect { connection ->
                    setState { copy(connection = connection, loading = false) }
                }
        }
    }
}
