package net.primal.android.notes.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.errors.UiError
import net.primal.android.feeds.list.ui.model.asFeedUi
import net.primal.android.navigation.identifier
import net.primal.android.navigation.naddr
import net.primal.android.navigation.npub
import net.primal.android.navigation.primalName
import net.primal.android.notes.feed.model.asStreamPillUi
import net.primal.android.notes.home.HomeFeedContract.UiEvent
import net.primal.android.notes.home.HomeFeedContract.UiState
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.subscriptions.SubscriptionsManager
import net.primal.android.user.updater.UserDataUpdater
import net.primal.android.user.updater.UserDataUpdaterFactory
import net.primal.core.config.AppConfigHandler
import net.primal.core.networking.utils.retryNetworkCall
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.feeds.FeedSpecKind
import net.primal.domain.feeds.FeedsRepository
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nip19TLV.toNaddrString
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.cryptography.SigningKeyNotFoundException
import net.primal.domain.nostr.cryptography.SigningRejectedException
import net.primal.domain.nostr.utils.npubToPubkey
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.streams.StreamRepository
import timber.log.Timber

@HiltViewModel
class HomeFeedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val appConfigHandler: AppConfigHandler,
    private val subscriptionsManager: SubscriptionsManager,
    private val feedsRepository: FeedsRepository,
    private val profileRepository: ProfileRepository,
    private val userDataSyncerFactory: UserDataUpdaterFactory,
    private val streamRepository: StreamRepository,
) : ViewModel() {

    private val hostNpub = savedStateHandle.npub
    private val streamIdentifier = savedStateHandle.identifier
    private val hostPrimalName = savedStateHandle.primalName
    private val streamNaddr = savedStateHandle.naddr

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<HomeFeedContract.SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: HomeFeedContract.SideEffect) = viewModelScope.launch { _effects.send(effect) }

    private var userDataUpdater: UserDataUpdater? = null

    init {
        resolveStreamParams()
        observeLiveEventsFromFollows()
        observeEvents()
        observeActiveAccount()
        observeBadgesUpdates()
        observeFeeds()
        fetchAndPersistNoteFeeds()
    }

    private fun resolveStreamParams() =
        viewModelScope.launch {
            if (streamNaddr != null) {
                setEffect(HomeFeedContract.SideEffect.StartStream(naddr = streamNaddr))
                return@launch
            }

            if (streamIdentifier == null) return@launch

            val userId = when {
                hostNpub != null -> hostNpub.npubToPubkey()

                hostPrimalName != null ->
                    runCatching { profileRepository.fetchProfileId(primalName = hostPrimalName) }.getOrNull()

                else -> null
            }

            if (userId != null) {
                val naddr = Naddr(
                    kind = NostrEventKind.LiveActivity.value,
                    userId = userId,
                    identifier = streamIdentifier,
                )

                setEffect(HomeFeedContract.SideEffect.StartStream(naddr = naddr.toNaddrString()))
            } else {
                setState { copy(uiError = UiError.InvalidNaddr) }
            }
        }

    @OptIn(FlowPreview::class)
    private fun observeLiveEventsFromFollows() =
        viewModelScope.launch {
            streamRepository.observeLiveEventsFromFollows(userId = activeAccountStore.activeUserId())
                .collectLatest { streams -> setState { copy(streams = streams.map { it.asStreamPillUi() }) } }
        }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.RequestUserDataUpdate -> updateUserData()
                    UiEvent.RefreshNoteFeeds -> fetchAndPersistNoteFeeds()
                    UiEvent.RestoreDefaultNoteFeeds -> restoreDefaultNoteFeeds()
                    UiEvent.DismissError -> setState { copy(uiError = null) }
                }
            }
        }
    }

    private fun restoreDefaultNoteFeeds() =
        viewModelScope.launch {
            try {
                setState { copy(loading = true) }
                val userId = activeAccountStore.activeUserId()
                feedsRepository.fetchAndPersistDefaultFeeds(
                    userId = userId,
                    specKind = FeedSpecKind.Notes,
                    givenDefaultFeeds = emptyList(),
                )
            } catch (error: SignatureException) {
                Timber.w(error)
            } catch (error: NetworkException) {
                Timber.w(error)
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun fetchAndPersistNoteFeeds() =
        viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                val userId = activeAccountStore.activeUserId()
                retryNetworkCall {
                    feedsRepository.fetchAndPersistNoteFeeds(userId = userId)
                }
            } catch (error: SigningRejectedException) {
                Timber.w(error)
            } catch (error: SigningKeyNotFoundException) {
                restoreDefaultNoteFeeds()
                Timber.w(error)
            } catch (error: NetworkException) {
                Timber.w(error)
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun observeFeeds() =
        viewModelScope.launch {
            feedsRepository.observeNotesFeeds(userId = activeAccountStore.activeUserId())
                .collect { feeds ->
                    setState {
                        copy(
                            feeds = feeds
                                .filter { it.enabled }
                                .map { it.asFeedUi() },
                        )
                    }
                }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                initUserUpdater(activeUserId = it.pubkey)
                setState {
                    copy(
                        activeAccountAvatarCdnImage = it.avatarCdnImage,
                        activeAccountLegendaryCustomization = it.primalLegendProfile?.asLegendaryCustomization(),
                        activeAccountBlossoms = it.blossomServers,
                    )
                }
            }
        }

    private fun initUserUpdater(activeUserId: String) {
        userDataUpdater = if (userDataUpdater?.userId != activeUserId) {
            userDataSyncerFactory.create(userId = activeUserId)
        } else {
            userDataUpdater
        }
    }

    private fun observeBadgesUpdates() =
        viewModelScope.launch {
            subscriptionsManager.badges.collect {
                setState {
                    copy(badges = it)
                }
            }
        }

    private fun updateUserData() =
        viewModelScope.launch {
            withContext(dispatcherProvider.io()) {
                userDataUpdater?.updateUserDataWithDebounce(30.minutes)
                appConfigHandler.updateAppConfigWithDebounce(30.minutes)
            }
        }
}
