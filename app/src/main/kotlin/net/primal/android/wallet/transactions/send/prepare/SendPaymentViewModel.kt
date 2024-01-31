package net.primal.android.wallet.transactions.send.prepare

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.crypto.urlToLnUrlHrp
import net.primal.android.navigation.asUrlDecoded
import net.primal.android.navigation.sendPaymentTab
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.api.parseAsLNUrlOrNull
import net.primal.android.wallet.repository.WalletRepository
import net.primal.android.wallet.transactions.send.create.DraftTransaction
import net.primal.android.wallet.transactions.send.prepare.SendPaymentContract.SideEffect
import net.primal.android.wallet.transactions.send.prepare.SendPaymentContract.UiEvent
import net.primal.android.wallet.transactions.send.prepare.SendPaymentContract.UiState
import net.primal.android.wallet.transactions.send.prepare.domain.RecipientType
import net.primal.android.wallet.transactions.send.prepare.tabs.SendPaymentTab
import net.primal.android.wallet.utils.isLightningAddress
import net.primal.android.wallet.utils.isLightningAddressUri
import net.primal.android.wallet.utils.isLnInvoice
import net.primal.android.wallet.utils.isLnUrl
import timber.log.Timber

@HiltViewModel
class SendPaymentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatchers: CoroutineDispatcherProvider,
    private val walletRepository: WalletRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val requestedTab = savedStateHandle.sendPaymentTab ?: SendPaymentTab.Nostr

    private val _state = MutableStateFlow(UiState(initialTab = requestedTab))
    val state = _state.asStateFlow()
    fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effect.send(effect) }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        subscribeToEvents()
    }

    private fun subscribeToEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.ProcessTextData -> processTextData(text = it.text)
                    is UiEvent.ProcessProfileData -> processProfileData(profileId = it.profileId)
                    UiEvent.DismissError -> setState { copy(error = null) }
                }
            }
        }

    private fun processTextData(text: String) =
        viewModelScope.launch {
            setState { copy(parsing = true) }
            val userId = activeAccountStore.activeUserId()
            try {
                when (text.parseRecipientType()) {
                    RecipientType.LnInvoice -> {
                        val response = withContext(dispatchers.io()) {
                            walletRepository.parseLnInvoice(userId = userId, lnbc = text)
                        }
                        setEffect(
                            SideEffect.DraftTransactionReady(
                                draft = DraftTransaction(
                                    targetUserId = response.userId,
                                    lnInvoice = text,
                                    lnInvoiceData = response.lnInvoiceData,
                                    amountSats = (response.lnInvoiceData.amountMilliSats / 1000).toString(),
                                    note = response.comment.asUrlDecoded(),
                                ),
                            ),
                        )
                    }

                    RecipientType.LnUrl -> {
                        val response = withContext(dispatchers.io()) {
                            walletRepository.parseLnUrl(userId = userId, lnurl = text)
                        }
                        setEffect(
                            SideEffect.DraftTransactionReady(
                                draft = DraftTransaction(
                                    minSendable = response.minSendable,
                                    maxSendable = response.maxSendable,
                                    targetUserId = response.targetPubkey,
                                    targetLud16 = response.targetLud16,
                                    targetLnUrl = text,
                                    note = response.description,
                                ),
                            ),
                        )
                    }

                    RecipientType.LightningAddress -> {
                        val lud16Value = if (text.isLightningAddressUri()) text.split(":").last() else text
                        val lnurl = lud16Value.parseAsLNUrlOrNull()?.urlToLnUrlHrp()
                        if (lnurl != null) {
                            val response = withContext(dispatchers.io()) {
                                walletRepository.parseLnUrl(userId = userId, lnurl = lnurl)
                            }
                            setEffect(
                                SideEffect.DraftTransactionReady(
                                    draft = DraftTransaction(
                                        minSendable = response.minSendable,
                                        maxSendable = response.maxSendable,
                                        targetUserId = response.targetPubkey,
                                        targetLud16 = lud16Value,
                                        targetLnUrl = lnurl,
                                        note = response.description,
                                    ),
                                ),
                            )
                        }
                    }

                    else -> {
                        Unit // Handle not supported stuff
                    }
                }
            } catch (error: WssException) {
                Timber.w(error)
                setState { copy(error = UiState.SendPaymentError.ParseException(error)) }
            } finally {
                setState { copy(parsing = false) }
            }
        }

    private fun String.parseRecipientType(): RecipientType? {
        return when {
            isLnInvoice() -> RecipientType.LnInvoice
            isLnUrl() -> RecipientType.LnUrl
            isLightningAddress() || isLightningAddressUri() -> RecipientType.LightningAddress
            else -> null
        }
    }

    private fun processProfileData(profileId: String) =
        viewModelScope.launch {
            val profileData = withContext(dispatchers.io()) {
                profileRepository.findProfileDataOrNull(profileId = profileId)
            }

            val lud16 = profileData?.lightningAddress
            if (lud16 != null && lud16.isLightningAddress()) {
                setEffect(
                    SideEffect.DraftTransactionReady(
                        draft = DraftTransaction(
                            targetLud16 = lud16,
                            targetUserId = profileData.ownerId,
                        ),
                    ),
                )
            } else {
                setState {
                    copy(
                        error = UiState.SendPaymentError.LightningAddressNotFound(
                            userDisplayName = profileData?.authorNameUiFriendly(),
                        ),
                    )
                }
            }
        }
}
