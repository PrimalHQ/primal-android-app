package net.primal.android.wallet.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.Job
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
import net.primal.android.wallet.transactions.list.TransactionListItemDataUi
import net.primal.android.wallet.utils.shouldShowBackup
import net.primal.core.networking.sockets.errors.NostrNoticeException
import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.core.utils.getIfTypeOrNull
import net.primal.core.utils.onFailure
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.billing.BillingRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.transactions.Transaction
import net.primal.domain.usecase.EnsureSparkWalletExistsUseCase
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletRepository
import net.primal.domain.wallet.distinctUntilWalletIdChanged
import net.primal.wallet.data.repository.handler.MigratePrimalTransactionsHandler

@Suppress("LongParameterList")
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
    private val ensureSparkWalletExistsUseCase: EnsureSparkWalletExistsUseCase,
    private val sparkWalletAccountRepository: SparkWalletAccountRepository,
    private val migratePrimalTransactionsHandler: MigratePrimalTransactionsHandler,
) : ViewModel() {

    private val activeUserId = activeAccountStore.activeUserId()

    private val _state = MutableStateFlow(
        value = UiState(isNpubLogin = userRepository.isNpubLogin(userId = activeUserId)),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvents(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private var userWalletsObserveJob: Job? = null

    init {
        observeUsdExchangeRate()
        subscribeToEvents()
        subscribeToActiveWalletId()
        subscribeToActiveWalletData()
        subscribeToActiveAccount()
        subscribeToPurchases()
        subscribeToBadgesUpdates()
        checkForPersistedSparkWallet()
    }

    private fun checkForPersistedSparkWallet() =
        viewModelScope.launch {
            val existingWalletId = sparkWalletAccountRepository.findPersistedWalletId(activeUserId)
            setState { copy(hasPersistedSparkWallet = existingWalletId != null) }
        }

    private fun migratePrimalTransactionsIfNeeded(sparkWalletId: String) =
        viewModelScope.launch {
            migratePrimalTransactionsHandler.invoke(
                userId = activeUserId,
                targetSparkWalletId = sparkWalletId,
            )
        }

    private fun subscribeToEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.DismissError -> setState { copy(error = null) }
                    UiEvent.RequestWalletBalanceUpdate -> state.value.wallet?.let { wallet ->
                        fetchWalletBalance(walletId = wallet.walletId)
                    }

                    is UiEvent.ChangeActiveWallet -> changeActiveWallet(wallet = it.wallet)

                    UiEvent.CreateWallet -> createWallet()
                }
            }
        }

    private fun createWallet() =
        viewModelScope.launch {
            ensureSparkWalletExistsUseCase.invoke(userId = activeUserId)
                .onFailure {
                    Napier.e(it) { "Failed to create wallet." }
                    setErrorState(UiState.DashboardError.WalletCreationFailed(it))
                }
        }

    private fun observeUserWallets(userId: String) {
        userWalletsObserveJob?.cancel()
        userWalletsObserveJob = viewModelScope.launch {
            walletAccountRepository
                .observeWalletsByUser(userId = userId)
                .collect { setState { copy(userWallets = it) } }
        }
    }

    private fun changeActiveWallet(wallet: Wallet) =
        viewModelScope.launch {
            walletAccountRepository.setActiveWallet(userId = activeUserId, walletId = wallet.walletId)
        }

    private fun subscribeToActiveWalletId() =
        viewModelScope.launch {
            walletAccountRepository.observeActiveWallet(userId = activeUserId)
                .distinctUntilWalletIdChanged()
                .filterNotNull()
                .collect { wallet ->
                    fetchWalletBalance(walletId = wallet.walletId)
                    if (wallet is Wallet.Spark) {
                        migratePrimalTransactionsIfNeeded(sparkWalletId = wallet.walletId)
                    }
                    setState {
                        copy(
                            transactions = walletRepository
                                .latestTransactions(walletId = wallet.walletId)
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
                    setState {
                        copy(
                            wallet = wallet,
                            lowBalance = wallet?.balanceInBtc == 0.0,
                            isWalletBackedUp = !wallet.shouldShowBackup,
                        )
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
                    )
                }
                observeUserWallets(userId = it.pubkey)
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
                .onFailure { Napier.w(throwable = it) { "Failed to fetch wallet balance." } }
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

    private fun confirmPurchase(purchase: SatsPurchase) =
        viewModelScope.launch {
            try {
                billingRepository.confirmInAppPurchase(
                    userId = activeAccountStore.activeUserId(),
                    quoteId = purchase.quote.quoteId,
                    purchaseToken = purchase.purchaseToken,
                )
            } catch (error: SignatureException) {
                Napier.w(throwable = error) { "Failed to confirm purchase due to signature error." }
            } catch (error: NetworkException) {
                Napier.w(throwable = error) { "Failed to confirm purchase due to network error." }
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

    private fun Transaction.mapAsTransactionDataUi() =
        TransactionListItemDataUi(
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
}
