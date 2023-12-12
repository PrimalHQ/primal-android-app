package net.primal.android.wallet.store.inapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.wallet.repository.WalletRepository
import net.primal.android.wallet.store.inapp.InAppPurchaseBuyContract.UiState
import net.primal.android.wallet.store.play.GooglePlayBillingClient

@HiltViewModel
class InAppPurchaseBuyViewModel @Inject constructor(
    private val playBillingClient: GooglePlayBillingClient,
    private val walletRepository: WalletRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    init {
        fetchQuote()
    }

    private fun fetchQuote() =
        viewModelScope.launch {
        }
}
