package net.primal.android.feeds.dvm

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
import net.primal.android.events.repository.EventRepository
import net.primal.android.feeds.dvm.DvmFeedListItemContract.UiEvent
import net.primal.android.feeds.dvm.DvmFeedListItemContract.UiState
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.ext.asReplaceableEventTag
import net.primal.android.nostr.notary.exceptions.MissingPrivateKey
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.domain.ZapTarget
import net.primal.android.wallet.zaps.InvalidZapRequestException
import net.primal.android.wallet.zaps.ZapFailureException
import net.primal.android.wallet.zaps.ZapHandler
import net.primal.android.wallet.zaps.hasWallet
import net.primal.domain.DvmFeed
import net.primal.domain.nostr.NostrEventKind
import timber.log.Timber

@HiltViewModel
class DvmFeedListItemViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val eventRepository: EventRepository,
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
                    is UiEvent.OnLikeClick -> onLikeClick(it.dvmFeed.data)
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
                        ),
                    )
                }
            }
        }

    private fun onLikeClick(dvmFeed: DvmFeed) =
        viewModelScope.launch {
            try {
                val aTagValue = "${NostrEventKind.AppHandler.value}:${dvmFeed.dvmPubkey}:${dvmFeed.dvmId}"
                eventRepository.likeEvent(
                    userId = activeAccountStore.activeUserId(),
                    eventId = dvmFeed.eventId,
                    eventAuthorId = dvmFeed.dvmPubkey,
                    optionalTags = listOf(aTagValue.asReplaceableEventTag()),
                )
            } catch (error: NostrPublishException) {
                setState { copy(error = UiError.FailedToPublishLikeEvent(error)) }
                Timber.w(error)
            } catch (error: MissingRelaysException) {
                setState { copy(error = UiError.MissingRelaysConfiguration(error)) }
                Timber.w(error)
            } catch (error: MissingPrivateKey) {
                setState { copy(error = UiError.MissingPrivateKey) }
                Timber.w(error)
            }
        }

    private fun onZapClick(zapAction: UiEvent.OnZapClick) =
        viewModelScope.launch {
            val dvmLnUrlDecoded = zapAction.dvmFeed.data.dvmLnUrlDecoded
            if (dvmLnUrlDecoded == null) {
                setState { copy(error = UiError.MissingLightningAddress(IllegalStateException("Missing ln url"))) }
                return@launch
            }

            try {
                zapHandler.zap(
                    userId = activeAccountStore.activeUserId(),
                    comment = zapAction.zapDescription,
                    amountInSats = zapAction.zapAmount,
                    target = ZapTarget.ReplaceableEvent(
                        kind = NostrEventKind.AppHandler.value,
                        identifier = zapAction.dvmFeed.data.dvmId,
                        eventId = zapAction.dvmFeed.data.eventId,
                        eventAuthorId = zapAction.dvmFeed.data.dvmPubkey,
                        eventAuthorLnUrlDecoded = dvmLnUrlDecoded,
                    ),
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
