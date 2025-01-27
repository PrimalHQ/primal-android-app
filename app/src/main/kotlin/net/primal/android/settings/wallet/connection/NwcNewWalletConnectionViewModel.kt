package net.primal.android.settings.wallet.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch

@HiltViewModel
class NwcNewWalletConnectionViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(NwcNewWalletConnectionContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: NwcNewWalletConnectionContract.UiState.() -> NwcNewWalletConnectionContract.UiState) =
        _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<NwcNewWalletConnectionContract.UiEvent>()
    fun setEvent(event: NwcNewWalletConnectionContract.UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is NwcNewWalletConnectionContract.UiEvent.AppNameChangedEvent -> setState {
                        copy(
                            appName = it.appName,
                        )
                    }

                    NwcNewWalletConnectionContract.UiEvent.CreateWalletConnection -> {
                    }
                }
            }
        }
    }
}
