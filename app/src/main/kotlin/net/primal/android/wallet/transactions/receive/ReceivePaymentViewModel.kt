package net.primal.android.wallet.transactions.receive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.repository.WalletRepository
import net.primal.android.wallet.transactions.receive.ReceivePaymentContract.UiEvent
import net.primal.android.wallet.transactions.receive.ReceivePaymentContract.UiState

@HiltViewModel
class ReceivePaymentViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val walletRepository: WalletRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) =
        viewModelScope.launch {
            _state.getAndUpdate { it.reducer() }
        }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeActiveAccount()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.OpenInvoiceCreation -> setState { copy(editMode = true) }
                    UiEvent.CancelInvoiceCreation -> setState { copy(editMode = false) }
                    is UiEvent.CreateInvoice -> createInvoice(amountInBtc = it.amountInBtc, comment = it.comment)
                    UiEvent.DismissError -> setState { copy(error = null) }
                }
            }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount
                .mapNotNull { it.primalWallet?.lightningAddress }
                .collect {
                    setState {
                        copy(
                            loading = false,
                            paymentDetails = this.paymentDetails.copy(
                                lightningAddress = it,
                            ),
                        )
                    }
                }
        }

    private fun createInvoice(amountInBtc: String, comment: String?) =
        viewModelScope.launch {
            setState { copy(creating = true) }
            try {
                val response = walletRepository.deposit(
                    userId = activeAccountStore.activeUserId(),
                    amountInBtc = amountInBtc,
                    comment = comment,
                )
                setState {
                    copy(
                        editMode = false,
                        paymentDetails = PaymentDetails(
                            invoice = response.lnInvoice,
                            amountInBtc = amountInBtc,
                            comment = comment,
                            lightningAddress = this.paymentDetails.lightningAddress,
                        ),
                    )
                }
            } catch (error: WssException) {
                setState { copy(error = UiState.ReceivePaymentError.FailedToCreateInvoice(cause = error)) }
            } finally {
                setState { copy(creating = false) }
            }
        }
}
