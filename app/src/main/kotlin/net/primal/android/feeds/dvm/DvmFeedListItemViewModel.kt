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
import net.primal.android.feeds.dvm.DvmFeedListItemContract.UiEvent
import net.primal.android.feeds.dvm.DvmFeedListItemContract.UiState
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.zaps.ZapHandler
import net.primal.core.utils.CurrencyConversionUtils.formatAsString
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.events.EventInteractionRepository
import net.primal.domain.feeds.DvmFeed
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.asReplaceableEventTag
import net.primal.domain.nostr.cryptography.SigningKeyNotFoundException
import net.primal.domain.nostr.cryptography.SigningRejectedException
import net.primal.domain.nostr.publisher.MissingRelaysException
import net.primal.domain.nostr.zaps.ZapError
import net.primal.domain.nostr.zaps.ZapResult
import net.primal.domain.nostr.zaps.ZapTarget
import net.primal.domain.utils.isConfigured
import timber.log.Timber

@HiltViewModel
class DvmFeedListItemViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val walletAccountRepository: WalletAccountRepository,
    private val eventInteractionRepository: EventInteractionRepository,
    private val zapHandler: ZapHandler,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val event = MutableSharedFlow<UiEvent>()
    fun setEvent(e: UiEvent) = viewModelScope.launch { event.emit(e) }

    init {
        observeEvents()
        observeActiveWallet()
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

    private fun observeActiveWallet() =
        viewModelScope.launch {
            walletAccountRepository.observeActiveWallet(userId = activeAccountStore.activeUserId())
                .collect {
                    setState {
                        copy(
                            zappingState = zappingState.copy(
                                walletConnected = it.isConfigured(),
                                walletBalanceInBtc = it?.balanceInBtc?.formatAsString(),
                            ),
                        )
                    }
                }
        }

    private fun subscribeToActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        zappingState = this.zappingState.copy(
                            zapDefault = it.appSettings?.zapDefault ?: this.zappingState.zapDefault,
                            zapsConfig = it.appSettings?.zapsConfig ?: this.zappingState.zapsConfig,
                        ),
                    )
                }
            }
        }

    private fun onLikeClick(dvmFeed: DvmFeed) =
        viewModelScope.launch {
            try {
                val aTagValue = "${NostrEventKind.AppHandler.value}:${dvmFeed.dvmPubkey}:${dvmFeed.dvmId}"
                eventInteractionRepository.likeEvent(
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
            } catch (error: SigningKeyNotFoundException) {
                setState { copy(error = UiError.MissingPrivateKey) }
                Timber.w(error)
            } catch (error: SigningRejectedException) {
                setState { copy(error = UiError.NostrSignUnauthorized) }
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
            val walletId = walletAccountRepository.getActiveWallet(userId = activeAccountStore.activeUserId())?.walletId
                ?: return@launch

            val result = zapHandler.zap(
                userId = activeAccountStore.activeUserId(),
                walletId = walletId,
                comment = zapAction.zapDescription,
                amountInSats = zapAction.zapAmount,
                target = ZapTarget.ReplaceableEvent(
                    naddr = zapAction.dvmFeed.data.dvmNaddr,
                    eventId = zapAction.dvmFeed.data.eventId,
                    recipientUserId = zapAction.dvmFeed.data.dvmPubkey,
                    recipientLnUrlDecoded = dvmLnUrlDecoded,
                ),
            )

            if (result is ZapResult.Failure) {
                when (result.error) {
                    is ZapError.InvalidZap, is ZapError.FailedToFetchZapPayRequest,
                    is ZapError.FailedToFetchZapInvoice,
                    -> setState { copy(error = UiError.InvalidZapRequest()) }

                    ZapError.FailedToPublishEvent, ZapError.FailedToSignEvent -> {
                        setState { copy(error = UiError.FailedToPublishZapEvent()) }
                    }

                    is ZapError.Unknown -> {
                        setState { copy(error = UiError.GenericError()) }
                    }
                }
            }
        }
}
