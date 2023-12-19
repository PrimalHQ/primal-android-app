package net.primal.android.wallet.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
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
import net.primal.android.networking.sockets.errors.NostrNoticeException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.WalletPreference
import net.primal.android.user.subscriptions.SubscriptionsManager
import net.primal.android.wallet.dashboard.WalletDashboardContract.UiEvent
import net.primal.android.wallet.dashboard.WalletDashboardContract.UiState
import net.primal.android.wallet.db.WalletTransaction
import net.primal.android.wallet.repository.WalletRepository
import net.primal.android.wallet.store.domain.SatsPurchase
import net.primal.android.wallet.store.play.BillingClientHandler
import net.primal.android.wallet.transactions.TransactionDataUi
import net.primal.android.wallet.utils.CurrencyConversionUtils.toSats
import timber.log.Timber

@HiltViewModel
class WalletDashboardViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val walletRepository: WalletRepository,
    private val subscriptionsManager: SubscriptionsManager,
    private val billingClientHandler: BillingClientHandler,
) : ViewModel() {

    private val activeUserId = activeAccountStore.activeUserId()

    private val _state = MutableStateFlow(
        value = UiState(
            transactions = walletRepository
                .latestTransactions(userId = activeUserId)
                .mapAsPagingDataOfTransactionUi(),
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvents(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        subscribeToEvents()
        subscribeToActiveAccount()
        subscribeToWalletBalance()
        subscribeToPurchases()
    }

    private fun subscribeToEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.UpdateWalletPreference -> updateWalletPreference(it.walletPreference)
                    UiEvent.DismissError -> setState { copy(error = null) }
                }
            }
        }

    private fun subscribeToActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        activeAccountAvatarCdnImage = it.avatarCdnImage,
                        primalWallet = it.primalWallet,
                        walletPreference = it.walletPreference,
                    )
                }
            }
        }

    private fun subscribeToWalletBalance() =
        viewModelScope.launch {
            subscriptionsManager.badges.collect {
                setState { copy(walletBalance = it.walletBalanceInBtc?.toBigDecimal()) }
            }
        }

    private fun subscribeToPurchases() =
        viewModelScope.launch {
            billingClientHandler.purchases.collect { purchase ->
                confirmPurchase(purchase = purchase)
            }
        }

    private fun confirmPurchase(purchase: SatsPurchase) =
        viewModelScope.launch {
            try {
                walletRepository.confirmInAppPurchase(
                    userId = activeAccountStore.activeUserId(),
                    quoteId = purchase.quote.quoteId,
                    purchaseToken = purchase.purchaseToken,
                )
            } catch (error: WssException) {
                Timber.e(error)
                val dashboardError = if (error.cause is NostrNoticeException) {
                    UiState.DashboardError.InAppPurchaseNoticeError(message = error.message)
                } else {
                    UiState.DashboardError.InAppPurchaseConfirmationFailed(cause = error)
                }
                setErrorState(dashboardError)
            }
        }

    private suspend fun updateWalletPreference(walletPreference: WalletPreference) =
        viewModelScope.launch {
            walletRepository.updateWalletPreference(
                userId = activeAccountStore.activeUserId(),
                walletPreference = walletPreference,
            )
        }

    private fun setErrorState(error: UiState.DashboardError) {
        setState { copy(error = error) }
    }

    private fun Flow<PagingData<WalletTransaction>>.mapAsPagingDataOfTransactionUi() =
        map { pagingData -> pagingData.map { it.mapAsTransactionUi() } }

    private fun WalletTransaction.mapAsTransactionUi() =
        TransactionDataUi(
            txId = this.data.id,
            txType = this.data.type,
            txAmountInSats = this.data.amountInBtc.toBigDecimal().abs().toSats(),
            txInstant = Instant.ofEpochSecond(this.data.completedAt),
            txNote = this.data.note,
            otherUserId = this.data.otherUserId,
            otherUserAvatarCdnImage = this.otherProfileData?.avatarCdnImage,
            otherUserDisplayName = this.otherProfileData?.authorNameUiFriendly(),
            isZap = this.data.isZap,
            isStorePurchase = this.data.isStorePurchase,
        )
}
