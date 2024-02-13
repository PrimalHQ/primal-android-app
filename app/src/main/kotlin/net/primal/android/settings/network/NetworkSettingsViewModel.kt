package net.primal.android.settings.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.relays.RelaysManager
import net.primal.android.settings.network.NetworkSettingsContract.UiEvent
import net.primal.android.settings.network.NetworkSettingsContract.UiState

@HiltViewModel
class NetworkSettingsViewModel @Inject constructor(
    private val relaysManager: RelaysManager,
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _uiState.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        ensureRelayPoolConnected()
        observeCachingServiceConnection()
        observeRelayPoolConnections()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.RestoreDefaultRelays -> Unit
                    is UiEvent.DeleteRelay -> Unit
                    is UiEvent.AddRelay -> Unit
                }
            }
        }

    private fun ensureRelayPoolConnected() =
        viewModelScope.launch {
            relaysManager.ensureUserRelayPoolConnected()
        }

    private fun observeCachingServiceConnection() =
        viewModelScope.launch {
            primalApiClient.connectionStatus.collect {
                setState { copy(cachingService = SocketDestinationUiState(url = it.url, it.connected)) }
            }
        }

    private fun observeRelayPoolConnections() =
        viewModelScope.launch {
            relaysManager.regularRelayPoolStatus.collect { poolStatus ->
                setState {
                    copy(
                        relays = poolStatus.map {
                            SocketDestinationUiState(url = it.key, connected = it.value)
                        },
                    )
                }
            }
        }
}
