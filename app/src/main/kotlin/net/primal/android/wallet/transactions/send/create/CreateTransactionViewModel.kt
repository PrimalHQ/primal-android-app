package net.primal.android.wallet.transactions.send.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.navigation.draftTransaction
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.profile.db.ProfileData
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.api.model.MiningFeeTier
import net.primal.android.wallet.api.model.WithdrawRequestBody
import net.primal.android.wallet.domain.DraftTxStatus
import net.primal.android.wallet.domain.SubWallet
import net.primal.android.wallet.repository.WalletRepository
import net.primal.android.wallet.transactions.send.create.CreateTransactionContract.UiEvent
import net.primal.android.wallet.transactions.send.create.CreateTransactionContract.UiState
import net.primal.android.wallet.transactions.send.create.ui.model.MiningFeeUi
import net.primal.android.wallet.utils.CurrencyConversionUtils.formatAsString
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc
import net.primal.android.wallet.utils.CurrencyConversionUtils.toSats
import net.primal.android.wallet.utils.isLightningAddress
import timber.log.Timber

@HiltViewModel
class CreateTransactionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatchers: CoroutineDispatcherProvider,
    private val activeUserStore: ActiveAccountStore,
    private val profileRepository: ProfileRepository,
    private val walletRepository: WalletRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        UiState(transaction = savedStateHandle.draftTransaction),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        subscribeToEvents()
        observeProfileData()
        fetchProfileData()
        updateMiningFees()
        fetchMinBtcTxAmount()
        observeDebouncedAmountChanges()
    }

    private fun subscribeToEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.SendTransaction -> {
                        sendTransaction(noteRecipient = it.noteRecipient, noteSelf = it.noteSelf)
                    }

                    is UiEvent.AmountChanged -> {
                        setState { copy(transaction = transaction.copy(amountSats = it.amountInSats)) }
                        clearAndRestartMiningFees()
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

    @OptIn(FlowPreview::class)
    private fun observeDebouncedAmountChanges() =
        viewModelScope.launch {
            events.filterIsInstance<UiEvent.AmountChanged>()
                .debounce(1.seconds)
                .collect {
                    updateMiningFees()
                }
        }

    private fun updateMiningFees() {
        val draftTx = _state.value.transaction
        val btcAddress = draftTx.targetOnChainAddress
        val amountInSats = draftTx.amountSats.toLong()
        if (btcAddress == null || amountInSats == 0L) return

        viewModelScope.launch {
            clearAndRestartMiningFees()
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
                            selectedFeeTierIndex = if (tiers.isNotEmpty()) 0 else null,
                        )
                    }
                }
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(fetchingMiningFees = false) }
            }
        }
    }

    private fun clearAndRestartMiningFees() {
        setState {
            copy(miningFeeTiers = emptyList(), fetchingMiningFees = true)
        }
    }

    private fun fetchMinBtcTxAmount() {
        val draftTx = _state.value.transaction
        val btcAddress = draftTx.targetOnChainAddress ?: return

        viewModelScope.launch {
            val activeUserId = activeUserStore.activeUserId()
            try {
                withContext(dispatchers.io()) {
                    val tiers = walletRepository.fetchMiningFees(
                        userId = activeUserId,
                        onChainAddress = btcAddress,
                        amountInBtc = "0.01",
                    )

                    val minBtcTxAmountInBtc = tiers.find { it.minimumAmount != null }?.minimumAmount?.amount
                    if (minBtcTxAmountInBtc != null) {
                        setState { copy(minBtcTxAmountInSats = minBtcTxAmountInBtc.toSats().toString()) }
                    }
                }
            } catch (error: WssException) {
                Timber.w(error)
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
                val activeUserId = activeUserStore.activeUserId()
                val draftTransaction = _state.value.transaction
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
                        noteRecipient = noteRecipient,
                        noteSelf = noteSelf,
                    ),
                )
                setState { copy(transaction = transaction.copy(status = DraftTxStatus.Sent)) }
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
