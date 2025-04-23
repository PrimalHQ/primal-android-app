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
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.navigation.sendPaymentTab
import net.primal.android.scanner.analysis.WalletTextParser
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.domain.DraftTx
import net.primal.android.wallet.transactions.send.prepare.SendPaymentContract.SideEffect
import net.primal.android.wallet.transactions.send.prepare.SendPaymentContract.UiEvent
import net.primal.android.wallet.transactions.send.prepare.SendPaymentContract.UiState
import net.primal.android.wallet.transactions.send.prepare.tabs.SendPaymentTab
import net.primal.android.wallet.utils.isLightningAddress
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.profile.ProfileRepository
import timber.log.Timber

@HiltViewModel
class SendPaymentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatchers: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val profileRepository: ProfileRepository,
    private val walletTextParser: WalletTextParser,
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
                val draftTx = walletTextParser.parseAndQueryText(userId = userId, text = text)
                if (draftTx != null) {
                    setEffect(SideEffect.DraftTransactionReady(draft = draftTx))
                } else {
                    Timber.w("Unable to parse text. [text=$text]")
                }
            } catch (error: SignatureException) {
                Timber.w(error)
            } catch (error: NetworkException) {
                Timber.w(error)
                setState { copy(error = UiState.SendPaymentError.ParseException(error)) }
            } finally {
                setState { copy(parsing = false) }
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
                        draft = DraftTx(
                            targetLud16 = lud16,
                            targetUserId = profileData.profileId,
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
