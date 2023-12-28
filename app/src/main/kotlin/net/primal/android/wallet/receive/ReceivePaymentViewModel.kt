package net.primal.android.wallet.receive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.receive.ReceivePaymentContract.UiState

@HiltViewModel
class ReceivePaymentViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) =
        viewModelScope.launch { _state.getAndUpdate { it.reducer() } }

    init {
        observeActiveAccount()
    }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount
                .mapNotNull { it.primalWallet?.lightningAddress }
                .collect {
                    setState { copy(loading = false, lightningAddress = it) }
                }
        }
}
