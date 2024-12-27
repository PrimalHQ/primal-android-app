package net.primal.android.wallet.transactions.receive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.navigation.asUrlEncoded
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.domain.Network
import net.primal.android.wallet.repository.ExchangeRateHandler
import net.primal.android.wallet.repository.WalletRepository
import net.primal.android.wallet.transactions.receive.ReceivePaymentContract.UiEvent
import net.primal.android.wallet.transactions.receive.ReceivePaymentContract.UiState
import net.primal.android.wallet.transactions.receive.model.PaymentDetails
import net.primal.android.wallet.transactions.receive.tabs.ReceivePaymentTab
import net.primal.android.wallet.transactions.send.create.MAXIMUM_SATS
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc
import net.primal.android.wallet.utils.CurrencyConversionUtils.toUsd
import timber.log.Timber

@HiltViewModel
class ReceivePaymentViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val walletRepository: WalletRepository,
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
        observeActiveAccount()
        observeUsdExchangeRate()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.OpenInvoiceCreation -> setState { copy(editMode = true) }
                    UiEvent.CancelInvoiceCreation -> setState { copy(editMode = false) }
                    is UiEvent.CreateInvoice -> createInvoice(amountInBtc = it.amountInBtc, comment = it.comment)
                    UiEvent.DismissError -> setState { copy(error = null) }
                    is UiEvent.ChangeNetwork -> changeNetwork(network = it.network)
                }
            }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount
                .collect {
                    val lightningAddress = it.primalWallet?.lightningAddress
                    setState {
                        copy(
                            lightningNetworkDetails = if (lightningAddress != null) {
                                this.lightningNetworkDetails.copy(address = lightningAddress)
                            } else {
                                this.lightningNetworkDetails
                            },
                            hasPremium = it.premiumMembership != null,
                        )
                    }
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

    private fun getMaximumUsdAmount(exchangeRate: Double?): BigDecimal {
        return (MAXIMUM_SATS)
            .toBigDecimal()
            .toBtc()
            .toBigDecimal()
            .toUsd(exchangeRate)
    }

    private fun fetchOnChainAddress() =
        viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                val response = walletRepository.generateOnChainAddress(userId = activeAccountStore.activeUserId())
                setState {
                    copy(bitcoinNetworkDetails = this.bitcoinNetworkDetails.copy(address = response.onChainAddress))
                }
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun createInvoice(amountInBtc: String, comment: String?) =
        viewModelScope.launch {
            setState { copy(creatingInvoice = true) }
            try {
                val response = walletRepository.createLightningInvoice(
                    userId = activeAccountStore.activeUserId(),
                    amountInBtc = amountInBtc,
                    comment = comment,
                )

                setState {
                    copy(
                        editMode = false,
                        paymentDetails = PaymentDetails(
                            amountInBtc = amountInBtc,
                            comment = comment,
                        ),
                        lightningNetworkDetails = this.lightningNetworkDetails.copy(
                            invoice = response.lnInvoice,
                        ),
                        bitcoinNetworkDetails = this.bitcoinNetworkDetails.copy(
                            invoice = "bitcoin:${this.bitcoinNetworkDetails.address}?amount=$amountInBtc".let {
                                if (!comment.isNullOrEmpty()) "$it&label=${comment.asUrlEncoded()}" else it
                            },
                        ),
                    )
                }
            } catch (error: WssException) {
                Timber.w(error)
                setState { copy(error = UiState.ReceivePaymentError.FailedToCreateLightningInvoice(cause = error)) }
            } finally {
                setState { copy(creatingInvoice = false) }
            }
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
