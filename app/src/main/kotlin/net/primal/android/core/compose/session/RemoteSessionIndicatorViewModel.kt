package net.primal.android.core.compose.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.service.PrimalRemoteSignerService

@HiltViewModel
class RemoteSessionIndicatorViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(RemoteSessionIndicatorContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: RemoteSessionIndicatorContract.UiState.() -> RemoteSessionIndicatorContract.UiState) =
        _state.getAndUpdate(reducer)

    init {
        observeRemoteSignerServiceState()
    }

    private fun observeRemoteSignerServiceState() =
        viewModelScope.launch {
            PrimalRemoteSignerService.isServiceRunning.collect { isRunning ->
                setState { copy(isRemoteSessionActive = isRunning) }
            }
        }
}
