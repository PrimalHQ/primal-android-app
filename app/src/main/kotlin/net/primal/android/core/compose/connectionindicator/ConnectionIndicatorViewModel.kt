package net.primal.android.core.compose.connectionindicator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.compose.connectionindicator.ConnectionIndicatorContract.UiState
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.core.networking.primal.PrimalApiClient

@HiltViewModel
class ConnectionIndicatorViewModel @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
        observeCachingServiceConnection()
    }

    companion object {
        private const val DELAY_ON_DISCONNECT = 2000L
    }

    private fun observeCachingServiceConnection() =
        viewModelScope.launch {
            primalApiClient.connectionStatus.collectLatest {
                if (!it.connected) {
                    delay(DELAY_ON_DISCONNECT)
                }

                setState { copy(hasConnection = it.connected) }
            }
        }
}
