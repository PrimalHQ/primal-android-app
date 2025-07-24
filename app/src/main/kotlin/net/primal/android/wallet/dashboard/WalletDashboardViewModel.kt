package net.primal.android.wallet.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository
import net.primal.android.user.subscriptions.SubscriptionsManager
import net.primal.android.wallet.dashboard.WalletDashboardContract.UiEvent
import net.primal.android.wallet.dashboard.WalletDashboardContract.UiState
import net.primal.android.wallet.repository.ExchangeRateHandler
import net.primal.android.wallet.store.PrimalBillingClient
import net.primal.android.wallet.store.domain.SatsPurchase
import net.primal.android.wallet.transactions.list.TransactionListItemUi
import net.primal.core.networking.sockets.errors.NostrNoticeException
import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.core.utils.getIfTypeOrNull
import net.primal.core.utils.onFailure
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.billing.BillingRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.transactions.Transaction
import net.primal.domain.wallet.WalletRepository
import timber.log.Timber

@HiltViewModel
class WalletDashboardViewModel @Inject constructor(
    userRepository: UserRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val walletAccountRepository: WalletAccountRepository,
    private val walletRepository: WalletRepository,
    private val primalBillingClient: PrimalBillingClient,
    private val billingRepository: BillingRepository,
    private val subscriptionsManager: SubscriptionsManager,
    private val exchangeRateHandler: ExchangeRateHandler,
) : ViewModel() {

    private val activeUserId = activeAccountStore.activeUserId()

    private val _state = MutableStateFlow(
        value = UiState(isNpubLogin = userRepository.isNpubLogin(userId = activeUserId)),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvents(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeUsdExchangeRate()
        subscribeToEvents()
        subscribeToActiveWalletId()
        subscribeToActiveWalletData()
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

    private fun subscribeToActiveWalletId() =
        viewModelScope.launch {
            walletAccountRepository.observeActiveWalletId(userId = activeUserId)
                .filterNotNull()
                .collect { walletId ->
                    fetchWalletBalance(walletId = walletId)
                    setState {
                        copy(
                            transactions = walletRepository
                                .latestTransactions(walletId = walletId)
                                .mapAsPagingDataOfTransactionUi()
                                .cachedIn(viewModelScope),
                        )
                    }
                }
        }

    private fun subscribeToActiveWalletData() =
        viewModelScope.launch {
            walletAccountRepository.observeActiveWallet(userId = activeUserId)
                .collect { wallet ->
                    setState { copy(wallet = wallet, lowBalance = wallet?.balanceInBtc == 0.0) }
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

    private fun fetchWalletBalance(walletId: String) =
        viewModelScope.launch {
            walletRepository.fetchWalletBalance(walletId = walletId)
                .onFailure { Timber.w(it) }
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
            try {
                walletAccountRepository.fetchWalletAccountInfo(userId = activeUserId)
                walletAccountRepository.setActiveWallet(userId = activeUserId, walletId = activeUserId)
            } catch (error: NetworkException) {
                Timber.w(error)
            }
        }

    private fun confirmPurchase(purchase: SatsPurchase) =
        viewModelScope.launch {
            try {
                billingRepository.confirmInAppPurchase(
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

    private fun Flow<PagingData<Transaction>>.mapAsPagingDataOfTransactionUi() =
        map { pagingData -> pagingData.map { it.mapAsTransactionDataUi() } }
            .map { pagingData ->
                pagingData.insertSeparators { before, after ->
                    when {
                        before == null && after != null ->
                            TransactionListItemUi.Header(
                                day = after.txUpdatedAt.formatDay(
                                    todayTranslation = "today",
                                    yesterdayTranslation = "yesterday",
                                ),
                            )

                        before is TransactionListItemUi.TxData && after is TransactionListItemUi.TxData &&
                            !isOnSameDay(before.txUpdatedAt, after.txUpdatedAt) -> {
                            TransactionListItemUi.Header(after.txUpdatedAt.formatDay("today", "yesterday"))
                        }

                        else -> null
                    }
                }
            }

    private fun isOnSameDay(first: Instant, second: Instant) =
        first.formatDay("today", "yesterday") == second.formatDay("today", "yesterday")

    private fun Transaction.mapAsTransactionDataUi() =
        TransactionListItemUi.TxData(
            txId = this.transactionId,
            txType = this.type,
            txState = this.state,
            txAmountInSats = this.amountInBtc.toBigDecimal().abs().toSats(),
            txCreatedAt = Instant.ofEpochSecond(this.createdAt),
            txUpdatedAt = Instant.ofEpochSecond(this.updatedAt),
            txCompletedAt = this.completedAt?.let { Instant.ofEpochSecond(it) },
            txNote = this.note,
            isZap = this is Transaction.Zap,
            isStorePurchase = this is Transaction.StorePurchase,
            isOnChainPayment = this is Transaction.OnChain,
            otherUserId = this.getIfTypeOrNull(Transaction.Zap::otherUserId)
                ?: this.getIfTypeOrNull(Transaction.Lightning::otherUserId),
            otherUserAvatarCdnImage = this.getIfTypeOrNull(Transaction.Zap::otherUserProfile)?.avatarCdnImage
                ?: this.getIfTypeOrNull(Transaction.Lightning::otherUserProfile)?.avatarCdnImage,
            otherUserDisplayName = getIfTypeOrNull(Transaction.Zap::otherUserProfile)?.authorNameUiFriendly()
                ?: getIfTypeOrNull(Transaction.Lightning::otherUserProfile)?.authorNameUiFriendly(),
            otherUserLegendaryCustomization = this.getIfTypeOrNull(Transaction.Zap::otherUserProfile)
                ?.primalPremiumInfo?.legendProfile?.asLegendaryCustomization()
                ?: this.getIfTypeOrNull(Transaction.Lightning::otherUserProfile)
                    ?.primalPremiumInfo?.legendProfile?.asLegendaryCustomization(),
        )

    private fun Instant.formatDay(todayTranslation: String, yesterdayTranslation: String): String {
        val zoneId = ZoneId.systemDefault()
        val zonedDateTime: ZonedDateTime = this.atZone(zoneId)
        val now = ZonedDateTime.now(zoneId)

        return if (now.toLocalDate() == zonedDateTime.toLocalDate()) {
            todayTranslation
        } else if (now.minusDays(1).toLocalDate() == zonedDateTime.toLocalDate()) {
            yesterdayTranslation
        } else {
            zonedDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        }
    }
}
