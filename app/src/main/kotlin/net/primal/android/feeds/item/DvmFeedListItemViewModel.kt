package net.primal.android.feeds.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.errors.UiError
import net.primal.android.feeds.domain.DvmFeed
import net.primal.android.feeds.item.DvmFeedListItemContract.UiEvent
import net.primal.android.feeds.item.DvmFeedListItemContract.UiState
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.note.repository.NoteRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.domain.ZapTarget
import net.primal.android.wallet.zaps.InvalidZapRequestException
import net.primal.android.wallet.zaps.ZapFailureException
import net.primal.android.wallet.zaps.ZapHandler
import net.primal.android.wallet.zaps.hasWallet
import timber.log.Timber

@HiltViewModel
class DvmFeedListItemViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val noteRepository: NoteRepository,
    private val zapHandler: ZapHandler,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val event = MutableSharedFlow<UiEvent>()
    fun setEvent(e: UiEvent) = viewModelScope.launch { event.emit(e) }

    init {
        observeEvents()
        subscribeToActiveAccount()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            event.collect {
                when (it) {
                    is UiEvent.OnLikeClick -> onLikeClick(it.dvmFeed)
                    is UiEvent.OnZapClick -> onZapClick(it)
                    UiEvent.DismissError -> setState { copy(error = null) }
                }
            }
        }

    private fun subscribeToActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        zappingState = this.zappingState.copy(
                            walletConnected = it.hasWallet(),
                            walletPreference = it.walletPreference,
                            zapDefault = it.appSettings?.zapDefault ?: this.zappingState.zapDefault,
                            zapsConfig = it.appSettings?.zapsConfig ?: this.zappingState.zapsConfig,
                            walletBalanceInBtc = it.primalWalletState.balanceInBtc,
                        )
                    )
                }
            }
        }

    private fun onLikeClick(dvmFeed: DvmFeed) =
        viewModelScope.launch {
            try {
                noteRepository.likeEvent(
                    userId = activeAccountStore.activeUserId(),
                    eventId = dvmFeed.eventId,
                    eventAuthorId = dvmFeed.dvmPubkey,
                )
            } catch (error: NostrPublishException) {
                setState { copy(error = UiError.FailedToPublishLikeEvent(error)) }
                Timber.w(error)
            } catch (error: MissingRelaysException) {
                setState { copy(error = UiError.MissingRelaysConfiguration(error)) }
                Timber.w(error)
            }
        }

    private fun onZapClick(zapAction: UiEvent.OnZapClick) =
        viewModelScope.launch {
            if (zapAction.dvmFeed.lnUrlDecoded == null) {
                setState { copy(error = UiError.MissingLightningAddress(RuntimeException()))}
                return@launch
            }

            try {
                zapHandler.zap(
                    userId = activeAccountStore.activeUserId(),
                    comment = zapAction.zapDescription,
                    amountInSats = zapAction.zapAmount,
                    target = ZapTarget.Event(
                        id = zapAction.dvmFeed.eventId,
                        authorPubkey = zapAction.dvmFeed.dvmPubkey,
                        authorLnUrlDecoded = zapAction.dvmFeed.lnUrlDecoded,
                    )
                )
            } catch (error: ZapFailureException) {
                setState { copy(error = UiError.FailedToPublishZapEvent(error)) }
                Timber.w(error)
            } catch (error: MissingRelaysException) {
                setState { copy(error = UiError.MissingRelaysConfiguration(error)) }
                Timber.w(error)
            } catch (error: InvalidZapRequestException) {
                setState { copy(error = UiError.InvalidZapRequest(error)) }
                Timber.w(error)
            }
        }
}
