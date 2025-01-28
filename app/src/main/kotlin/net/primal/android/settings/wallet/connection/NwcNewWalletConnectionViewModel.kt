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
import net.primal.android.settings.wallet.connection.NwcNewWalletConnectionContract.UiState

@HiltViewModel
class NwcNewWalletConnectionViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<NwcNewWalletConnectionContract.UiEvent>()
    fun setEvent(event: NwcNewWalletConnectionContract.UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is NwcNewWalletConnectionContract.UiEvent.AppNameChanged -> setState {
                        copy(
                            appName = it.appName,
                        )
                    }

                    NwcNewWalletConnectionContract.UiEvent.CreateWalletConnection -> {
                    }

                    is NwcNewWalletConnectionContract.UiEvent.DailyBudgetChanged -> setState {
                        copy(
                            dailyBudget = it.dailyBudget,
                        )
                    }
                }
            }
        }
    }
}
