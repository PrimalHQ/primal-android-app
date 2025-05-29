package net.primal.android.wallet.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.WalletPreference
import net.primal.android.user.repository.UserRepository
import net.primal.android.user.subscriptions.SubscriptionsManager
import net.primal.android.wallet.dashboard.WalletDashboardContract.UiEvent
import net.primal.android.wallet.dashboard.WalletDashboardContract.UiState
import net.primal.android.wallet.repository.ExchangeRateHandler
import net.primal.android.wallet.repository.TransactionProfileData
import net.primal.android.wallet.repository.WalletRepository
import net.primal.android.wallet.store.PrimalBillingClient
import net.primal.android.wallet.store.domain.SatsPurchase
import net.primal.android.wallet.transactions.list.TransactionListItemDataUi
import net.primal.core.networking.nwc.NwcClientFactory
import net.primal.core.networking.sockets.errors.NostrNoticeException
import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.cryptography.SignatureException
import timber.log.Timber

@HiltViewModel
class WalletDashboardViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val walletRepository: WalletRepository,
    private val userRepository: UserRepository,
    private val primalBillingClient: PrimalBillingClient,
    private val subscriptionsManager: SubscriptionsManager,
    private val exchangeRateHandler: ExchangeRateHandler,
) : ViewModel() {

    private val activeUserId = activeAccountStore.activeUserId()

    private val _state = MutableStateFlow(
        value = UiState(
            transactions = walletRepository
                .getLatestTransactions(userId = activeUserId)
                .mapAsPagingDataOfTransactionUi(),
            isNpubLogin = userRepository.isNpubLogin(userId = activeUserId),
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvents(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        fetchWalletBalance()
        observeUsdExchangeRate()
        subscribeToEvents()
        subscribeToActiveAccount()
        subscribeToPurchases()
        subscribeToBadgesUpdates()
    }

    private fun subscribeToEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.DismissError -> setState { copy(error = null) }
                    UiEvent.EnablePrimalWallet -> enablePrimalWallet()
                }
            }
        }

    private fun subscribeToActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        activeAccountAvatarCdnImage = it.avatarCdnImage,
                        activeAccountLegendaryCustomization = it.primalLegendProfile?.asLegendaryCustomization(),
                        activeAccountBlossoms = it.blossomServers,
                        primalWallet = it.primalWallet,
                        walletPreference = it.walletPreference,
                        walletBalance = it.primalWalletState.balanceInBtc?.toBigDecimal(),
                        lastWalletUpdatedAt = it.primalWalletState.lastUpdatedAt,
                        lowBalance = it.primalWalletState.balanceInBtc?.toSats()?.toLong() == 0L,
                    )
                }
            }
        }

    private fun subscribeToPurchases() =
        viewModelScope.launch {
            primalBillingClient.satsPurchases.collect { purchase ->
                confirmPurchase(purchase = purchase)
            }
        }

    private fun subscribeToBadgesUpdates() =
        viewModelScope.launch {
            subscriptionsManager.badges.collect {
                setState {
                    copy(badges = it)
                }
            }
        }

    private fun fetchWalletBalance() =
        viewModelScope.launch {
            try {
                // This is just for concept
                activeAccountStore.activeUserAccount.collect {
                    it.nostrWallet?.let { nwc ->
                        val nwcClient = NwcClientFactory.createNwcApiClient(nwcData = nwc)
                        nwcClient.getBalance()
                    }
                }
//                walletRepository.fetchWalletBalance(userId = activeUserId)
            } catch (error: SignatureException) {
                Timber.w(error)
            } catch (error: NetworkException) {
                Timber.w(error)
            }
        }

    private fun observeUsdExchangeRate() {
        viewModelScope.launch {
            fetchExchangeRate()
            exchangeRateHandler.usdExchangeRate.collect {
                setState { copy(exchangeBtcUsdRate = it) }
            }
        }
    }

    private fun fetchExchangeRate() =
        viewModelScope.launch {
            exchangeRateHandler.updateExchangeRate(
                userId = activeAccountStore.activeUserId(),
            )
        }

    private fun enablePrimalWallet() =
        viewModelScope.launch {
            userRepository.updateWalletPreference(
                userId = activeUserId,
                walletPreference = WalletPreference.PrimalWallet,
            )
        }

    private fun confirmPurchase(purchase: SatsPurchase) =
        viewModelScope.launch {
            try {
                walletRepository.confirmInAppPurchase(
                    userId = activeAccountStore.activeUserId(),
                    quoteId = purchase.quote.quoteId,
                    purchaseToken = purchase.purchaseToken,
                )
            } catch (error: SignatureException) {
                Timber.w(error)
            } catch (error: NetworkException) {
                Timber.w(error)
                val dashboardError = if (error.cause is NostrNoticeException) {
                    UiState.DashboardError.InAppPurchaseNoticeError(message = error.message)
                } else {
                    UiState.DashboardError.InAppPurchaseConfirmationFailed(cause = error)
                }
                setErrorState(dashboardError)
            }
        }

    private fun setErrorState(error: UiState.DashboardError) {
        setState { copy(error = error) }
    }

    private fun Flow<PagingData<TransactionProfileData>>.mapAsPagingDataOfTransactionUi() =
        map { pagingData -> pagingData.map { it.mapAsTransactionDataUi() } }

    private fun TransactionProfileData.mapAsTransactionDataUi() =
        TransactionListItemDataUi(
            txId = this.transaction.id,
            txType = this.transaction.type,
            txState = this.transaction.state,
            txAmountInSats = this.transaction.amountInBtc.toBigDecimal().abs().toSats(),
            txCreatedAt = Instant.ofEpochSecond(this.transaction.createdAt),
            txUpdatedAt = Instant.ofEpochSecond(this.transaction.updatedAt),
            txCompletedAt = this.transaction.completedAt?.let { Instant.ofEpochSecond(it) },
            txNote = this.transaction.note,
            otherUserId = this.transaction.otherUserId,
            otherUserAvatarCdnImage = this.otherProfileData?.avatarCdnImage,
            otherUserDisplayName = this.otherProfileData?.authorNameUiFriendly(),
            otherUserLegendaryCustomization = this.otherProfileData?.primalPremiumInfo
                ?.legendProfile?.asLegendaryCustomization(),
            isZap = this.transaction.isZap,
            isStorePurchase = this.transaction.isStorePurchase,
            isOnChainPayment = this.transaction.onChainAddress != null,
        )
}
