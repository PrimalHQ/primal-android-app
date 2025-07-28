package net.primal.android.wallet.transactions.receive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.navigation.asUrlEncoded
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.repository.ExchangeRateHandler
import net.primal.android.wallet.transactions.receive.ReceivePaymentContract.UiEvent
import net.primal.android.wallet.transactions.receive.ReceivePaymentContract.UiState
import net.primal.android.wallet.transactions.receive.model.PaymentDetails
import net.primal.android.wallet.transactions.receive.tabs.ReceivePaymentTab
import net.primal.core.utils.getMaximumUsdAmount
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.wallet.Network
import net.primal.domain.wallet.WalletRepository
import timber.log.Timber

@HiltViewModel
class ReceivePaymentViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val walletRepository: WalletRepository,
    private val walletAccountRepository: WalletAccountRepository,
    private val exchangeRateHandler: ExchangeRateHandler,
    private val activeUserStore: ActiveAccountStore,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState(initialTab = ReceivePaymentTab.Lightning))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) =
        viewModelScope.launch {
            _state.getAndUpdate { it.reducer() }
        }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        fetchOnChainAddress()
        observeEvents()
        observeActiveWallet()
        observeActiveAccount()
        observeUsdExchangeRate()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.OpenInvoiceCreation -> setState { copy(editMode = true) }
                    UiEvent.CancelInvoiceCreation -> setState { copy(editMode = false) }
                    is UiEvent.CreateInvoice -> createInvoice(
                        amountInBtc = it.amountInBtc,
                        amountInUsd = it.amountInUsd,
                        comment = it.comment,
                    )

                    UiEvent.DismissError -> setState { copy(error = null) }
                    is UiEvent.ChangeNetwork -> changeNetwork(network = it.network)
                }
            }
        }

    private fun observeActiveWallet() =
        viewModelScope.launch {
            walletAccountRepository.observeActiveWallet(userId = activeAccountStore.activeUserId())
                .collect { wallet ->
                    setState {
                        copy(
                            activeWallet = wallet,
                            lightningNetworkDetails = lightningNetworkDetails.copy(
                                address = wallet?.lightningAddress ?: lightningNetworkDetails.address,
                            ),
                        )
                    }
                }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount
                .collect {
                    setState { copy(hasPremium = it.premiumMembership != null) }
                }
        }

    private fun observeUsdExchangeRate() =
        viewModelScope.launch {
            fetchExchangeRate()
            exchangeRateHandler.usdExchangeRate.collect {
                setState {
                    copy(
                        currentExchangeRate = it,
                        maximumUsdAmount = getMaximumUsdAmount(it),
                    )
                }
            }
        }

    private fun fetchExchangeRate() =
        viewModelScope.launch {
            exchangeRateHandler.updateExchangeRate(
                userId = activeUserStore.activeUserId(),
            )
        }

    private fun fetchOnChainAddress() =
        viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                val response = walletRepository.createOnChainAddress(userId = activeAccountStore.activeUserId())
                setState { copy(bitcoinNetworkDetails = this.bitcoinNetworkDetails.copy(address = response.address)) }
            } catch (error: SignatureException) {
                Timber.w(error)
            } catch (error: NetworkException) {
                Timber.w(error)
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun createInvoice(
        amountInBtc: String,
        amountInUsd: String,
        comment: String?,
    ) = viewModelScope.launch {
        setState { copy(creatingInvoice = true) }
        val activeWalletId = state.value.activeWallet?.walletId ?: return@launch

        walletRepository.createLightningInvoice(
            walletId = activeWalletId,
            amountInBtc = amountInBtc,
            comment = comment,
        ).onSuccess { result ->
            setState {
                copy(
                    editMode = false,
                    paymentDetails = PaymentDetails(
                        amountInBtc = amountInBtc,
                        amountInUsd = amountInUsd,
                        comment = comment,
                    ),
                    lightningNetworkDetails = this.lightningNetworkDetails.copy(
                        invoice = result.invoice,
                    ),
                    bitcoinNetworkDetails = this.bitcoinNetworkDetails.copy(
                        invoice = "bitcoin:${this.bitcoinNetworkDetails.address}?amount=$amountInBtc".let {
                            if (!comment.isNullOrEmpty()) "$it&label=${comment.asUrlEncoded()}" else it
                        },
                    ),
                )
            }
        }.onFailure { error ->
            Timber.w(error)
            setState { copy(error = UiState.ReceivePaymentError.FailedToCreateLightningInvoice(cause = error)) }
        }

        setState { copy(creatingInvoice = false) }
    }

    private fun changeNetwork(network: Network) =
        viewModelScope.launch {
            when (network) {
                Network.Lightning -> setState {
                    copy(currentTab = ReceivePaymentTab.Lightning)
                }

                Network.Bitcoin -> {
                    val state = _state.value
                    if (state.bitcoinNetworkDetails.address == null && !state.loading) {
                        fetchOnChainAddress()
                    }

                    setState {
                        copy(currentTab = ReceivePaymentTab.Bitcoin)
                    }
                }
            }
        }
}
