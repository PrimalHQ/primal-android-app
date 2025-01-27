package net.primal.android.settings.wallet.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository

@HiltViewModel
class NwcNewWalletConnectionViewModel @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(NwcNewWalletConnectionContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: NwcNewWalletConnectionContract.UiState.() -> NwcNewWalletConnectionContract.UiState) =
        _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<NwcNewWalletConnectionContract.UiEvent>()
    fun setEvent(event: NwcNewWalletConnectionContract.UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<NwcNewWalletConnectionContract.SideEffect>()
    private fun setEffect(effect: NwcNewWalletConnectionContract.SideEffect) =
        viewModelScope.launch { _effects.send(effect) }
    val effects = _effects.receiveAsFlow()

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
