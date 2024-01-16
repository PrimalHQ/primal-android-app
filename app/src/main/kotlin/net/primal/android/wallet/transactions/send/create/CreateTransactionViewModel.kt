package net.primal.android.wallet.transactions.send.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.navigation.draftTransaction
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.api.model.WithdrawRequestBody
import net.primal.android.wallet.domain.SubWallet
import net.primal.android.wallet.repository.WalletRepository
import net.primal.android.wallet.transactions.send.create.CreateTransactionContract.UiEvent
import net.primal.android.wallet.transactions.send.create.CreateTransactionContract.UiState
import net.primal.android.wallet.utils.CurrencyConversionUtils.formatAsString
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc
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
        loadProfileData()
    }

    private fun subscribeToEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.SendTransaction -> sendTransaction(note = it.note)
                    is UiEvent.AmountChanged -> setState {
                        copy(transaction = transaction.copy(amountSats = it.amountInSats))
                    }
                }
            }
        }

    private fun loadProfileData() =
        viewModelScope.launch {
            state.value.transaction.targetUserId?.let {
                val profileData = withContext(dispatchers.io()) {
                    profileRepository.findProfileData(profileId = it)
                }

                setState {
                    copy(
                        profileAvatarCdnImage = profileData.avatarCdnImage,
                        profileDisplayName = profileData.authorNameUiFriendly(),
                        profileLightningAddress = profileData.lightningAddress,
                    )
                }
            }
        }

    private fun sendTransaction(note: String?) =
        viewModelScope.launch {
            try {
                setState { copy(transaction = transaction.copy(status = TransactionStatus.Sending)) }
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
                        amountBtc = if (draftTransaction.lnInvoice == null) {
                            draftTransaction.amountSats.toULong().toBtc().formatAsString()
                        } else {
                            null
                        },
                        noteRecipient = if (draftTransaction.lnInvoice == null) note else null,
                        noteSelf = note,
                    ),
                )
                setState { copy(transaction = transaction.copy(status = TransactionStatus.Sent)) }
            } catch (error: WssException) {
                Timber.e(error)
                setState {
                    copy(
                        error = error,
                        transaction = transaction.copy(status = TransactionStatus.Failed),
                    )
                }
            }
        }
}
