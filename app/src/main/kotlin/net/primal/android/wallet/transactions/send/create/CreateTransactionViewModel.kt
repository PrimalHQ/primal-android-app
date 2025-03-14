package net.primal.android.wallet.transactions.send.create

import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.getMaximumUsdAmount
import net.primal.android.navigation.draftTransaction
import net.primal.android.navigation.lnbc
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.notary.MissingPrivateKeyException
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.profile.db.ProfileData
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.scanner.analysis.WalletTextParser
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.api.model.MiningFeeTier
import net.primal.android.wallet.api.model.WithdrawRequestBody
import net.primal.android.wallet.domain.CurrencyMode
import net.primal.android.wallet.domain.DraftTxStatus
import net.primal.android.wallet.domain.SubWallet
import net.primal.android.wallet.repository.ExchangeRateHandler
import net.primal.android.wallet.repository.WalletRepository
import net.primal.android.wallet.transactions.send.create.CreateTransactionContract.UiEvent
import net.primal.android.wallet.transactions.send.create.CreateTransactionContract.UiState
import net.primal.android.wallet.transactions.send.create.ui.model.MiningFeeUi
import net.primal.android.wallet.utils.CurrencyConversionUtils.formatAsString
import net.primal.android.wallet.utils.CurrencyConversionUtils.fromSatsToUsd
import net.primal.android.wallet.utils.CurrencyConversionUtils.fromUsdToSats
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc
import net.primal.android.wallet.utils.isLightningAddress
import timber.log.Timber

@HiltViewModel
class CreateTransactionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatchers: CoroutineDispatcherProvider,
    private val activeUserStore: ActiveAccountStore,
    private val profileRepository: ProfileRepository,
    private val walletRepository: WalletRepository,
    private val walletTextParser: WalletTextParser,
    private val exchangeRateHandler: ExchangeRateHandler,
) : ViewModel() {

    private val argLnbc = savedStateHandle.lnbc

    private val _state = MutableStateFlow(UiState(transaction = savedStateHandle.draftTransaction))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        subscribeToEvents()
        updateMiningFees()
        observeUsdExchangeRate()

        if (argLnbc != null) {
            viewModelScope.launch {
                parseInvoiceAndUpdateState(text = argLnbc)
                observeProfileData()
                fetchProfileData()
            }
        } else {
            observeProfileData()
            fetchProfileData()
        }
    }

    private fun observeUsdExchangeRate() {
        viewModelScope.launch {
            fetchExchangeRate()
            exchangeRateHandler.usdExchangeRate.collect {
                setState {
                    copy(
                        currentExchangeRate = it,
                        maximumUsdAmount = getMaximumUsdAmount(it),
                        amountInUsd = BigDecimal(_state.value.transaction.amountSats)
                            .fromSatsToUsd(it)
                            .let { amount ->
                                if (amount.compareTo(BigDecimal.ZERO) == 0) {
                                    "0"
                                } else {
                                    amount.toString()
                                }
                            },
                    )
                }
            }
        }
    }

    private fun fetchExchangeRate() =
        viewModelScope.launch {
            exchangeRateHandler.updateExchangeRate(
                userId = activeUserStore.activeUserId(),
            )
        }

    private fun subscribeToEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is UiEvent.SendTransaction -> {
                        sendTransaction(noteRecipient = event.noteRecipient, noteSelf = event.noteSelf)
                    }

                    is UiEvent.AmountChanged -> {
                        updateAmount(amount = event.amount)
                    }

                    is UiEvent.ChangeCurrencyMode -> {
                        setState {
                            copy(currencyMode = event.currencyMode)
                        }
                    }

                    is UiEvent.MiningFeeChanged -> {
                        setState {
                            copy(selectedFeeTierIndex = this.miningFeeTiers.indexOfFirst { it.id == event.tierId })
                        }
                    }

                    UiEvent.AmountApplied -> {
                        updateMiningFees()
                    }

                    UiEvent.ReloadMiningFees -> {
                        updateMiningFees()
                    }
                }
            }
        }

    private fun updateAmount(amount: String) {
        when (_state.value.currencyMode) {
            CurrencyMode.SATS -> {
                setState {
                    copy(
                        transaction = transaction.copy(amountSats = amount),
                        amountInUsd = BigDecimal(amount.toDouble())
                            .fromSatsToUsd(state.value.currentExchangeRate)
                            .stripTrailingZeros()
                            .let { if (it.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO else it }
                            .toPlainString(),
                    )
                }
            }

            CurrencyMode.FIAT -> {
                setState {
                    copy(
                        amountInUsd = amount,
                        transaction = transaction.copy(
                            amountSats = BigDecimal(amount)
                                .fromUsdToSats(state.value.currentExchangeRate)
                                .toString(),
                        ),
                    )
                }
            }
        }
    }

    private fun fetchProfileData() =
        viewModelScope.launch {
            state.value.transaction.targetUserId?.let { targetUserId ->
                try {
                    withContext(dispatchers.io()) {
                        profileRepository.requestProfileUpdate(profileId = targetUserId)
                    }
                } catch (error: WssException) {
                    Timber.w(error)
                }
            }
        }

    private fun updateMiningFees() {
        val uiState = _state.value
        val btcAddress = uiState.transaction.targetOnChainAddress
        val amountInSats = uiState.transaction.amountSats.toLong()
        if (btcAddress == null || amountInSats == 0L) return

        viewModelScope.launch {
            val lastTierIndex = uiState.selectedFeeTierIndex
            setState { copy(miningFeeTiers = emptyList(), selectedFeeTierIndex = null, fetchingMiningFees = true) }
            val activeUserId = activeUserStore.activeUserId()
            try {
                withContext(dispatchers.io()) {
                    val tiers = walletRepository.fetchMiningFees(
                        userId = activeUserId,
                        onChainAddress = btcAddress,
                        amountInBtc = amountInSats.toBtc().formatAsString(),
                    )

                    setState {
                        copy(
                            miningFeeTiers = tiers.map { it.asMiningFeeUi() },
                            selectedFeeTierIndex = when {
                                tiers.isNotEmpty() -> when {
                                    lastTierIndex != null && lastTierIndex < tiers.size -> lastTierIndex
                                    else -> 0
                                }

                                else -> null
                            },
                        )
                    }
                }
            } catch (error: MissingPrivateKeyException) {
                Timber.w(error)
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(fetchingMiningFees = false) }
            }
        }
    }

    private fun MiningFeeTier.asMiningFeeUi(): MiningFeeUi {
        return MiningFeeUi(
            id = this.id,
            label = this.label,
            confirmationEstimateInMin = this.estimatedDeliveryDurationInMin,
            feeInBtc = this.estimatedFee.amount,
            minAmountInBtc = this.minimumAmount?.amount,
        )
    }

    private suspend fun parseInvoiceAndUpdateState(text: String) {
        setState { copy(parsingInvoice = true) }
        val userId = activeUserStore.activeUserId()
        try {
            val draftTx = walletTextParser.parseAndQueryText(userId = userId, text = text)
            if (draftTx != null) {
                setState { copy(transaction = draftTx) }
            } else {
                Timber.w("Unable to parse text. [text=$text]")
            }
        } catch (error: MissingPrivateKeyException) {
            Timber.w(error)
        } catch (error: WssException) {
            Timber.w(error)
            setState { copy(error = error) }
        } finally {
            setState { copy(parsingInvoice = false) }
        }
    }

    private fun observeProfileData() =
        viewModelScope.launch {
            state.value.transaction.targetUserId?.let { targetUserId ->
                profileRepository.observeProfile(targetUserId).collect { profile ->
                    profile.metadata?.updateStateWithProfileData()
                }
            }
        }

    private fun ProfileData.updateStateWithProfileData() {
        setState {
            copy(
                profileAvatarCdnImage = this@updateStateWithProfileData.avatarCdnImage,
                profileLegendaryCustomization = this@updateStateWithProfileData.primalPremiumInfo
                    ?.legendProfile?.asLegendaryCustomization(),
                profileDisplayName = this@updateStateWithProfileData.authorNameUiFriendly(),
                profileLightningAddress = transaction.targetLud16 ?: this@updateStateWithProfileData.lightningAddress,
                transaction = if (transaction.targetLud16 == null &&
                    this@updateStateWithProfileData.lightningAddress?.isLightningAddress() == true
                ) {
                    this.transaction.copy(targetLud16 = this@updateStateWithProfileData.lightningAddress)
                } else {
                    this.transaction
                },
            )
        }
    }

    private fun sendTransaction(noteRecipient: String?, noteSelf: String?) =
        viewModelScope.launch {
            try {
                setState { copy(transaction = transaction.copy(status = DraftTxStatus.Sending)) }
                val uiState = _state.value
                val activeUserId = activeUserStore.activeUserId()
                val miningFeeTier = uiState.selectedFeeTierIndex?.let { uiState.miningFeeTiers.getOrNull(it) }
                val draftTransaction = uiState.transaction
                walletRepository.withdraw(
                    userId = activeUserId,
                    body = WithdrawRequestBody(
                        subWallet = SubWallet.Open,
                        targetLud16 = draftTransaction.targetLud16,
                        targetLnUrl = draftTransaction.targetLnUrl,
                        targetPubKey = draftTransaction.targetUserId,
                        lnInvoice = draftTransaction.lnInvoice,
                        targetBtcAddress = draftTransaction.targetOnChainAddress,
                        amountBtc = if (draftTransaction.lnInvoice == null) {
                            draftTransaction.amountSats.toULong().toBtc().formatAsString()
                        } else {
                            null
                        },
                        onChainTier = miningFeeTier?.id,
                        noteRecipient = noteRecipient,
                        noteSelf = noteSelf,
                    ),
                )
                setState { copy(transaction = transaction.copy(status = DraftTxStatus.Sent)) }
            } catch (error: MissingPrivateKeyException) {
                Timber.w(error)
                setState {
                    copy(
                        error = error,
                        transaction = transaction.copy(status = DraftTxStatus.Failed),
                    )
                }
            } catch (error: WssException) {
                Timber.w(error)
                setState {
                    copy(
                        error = error,
                        transaction = transaction.copy(status = DraftTxStatus.Failed),
                    )
                }
            }
        }
}
