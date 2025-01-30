package net.primal.android.settings.wallet.link

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.navigation.appIcon
import net.primal.android.navigation.appName
import net.primal.android.navigation.callback
import net.primal.android.settings.wallet.link.LinkPrimalWalletContract.UiEvent
import net.primal.android.settings.wallet.link.LinkPrimalWalletContract.UiState

class LinkPrimalWalletViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val appName: String? = savedStateHandle.appName
    private val appIcon: String? = savedStateHandle.appIcon
    private val callback: String = savedStateHandle.callback

    private val _state = MutableStateFlow(
        UiState(
            appName = appName,
            appIcon = appIcon,
            callback = callback,
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.DailyBudgetChanged -> {
                    }
                }
            }
        }
    }
}
