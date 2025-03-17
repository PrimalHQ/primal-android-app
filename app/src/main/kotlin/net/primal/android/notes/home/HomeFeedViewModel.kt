package net.primal.android.notes.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.config.AppConfigHandler
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.feeds.list.ui.model.asFeedUi
import net.primal.android.feeds.repository.FeedsRepository
import net.primal.android.networking.primal.retryNetworkCall
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.notary.MissingPrivateKeyException
import net.primal.android.notes.home.HomeFeedContract.UiEvent
import net.primal.android.notes.home.HomeFeedContract.UiState
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.subscriptions.SubscriptionsManager
import net.primal.android.user.updater.UserDataUpdater
import net.primal.android.user.updater.UserDataUpdaterFactory
import timber.log.Timber

@HiltViewModel
class HomeFeedViewModel @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val appConfigHandler: AppConfigHandler,
    private val subscriptionsManager: SubscriptionsManager,
    private val feedsRepository: FeedsRepository,
    private val userDataSyncerFactory: UserDataUpdaterFactory,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private var userDataUpdater: UserDataUpdater? = null

    init {
        observeEvents()
        observeActiveAccount()
        observeBadgesUpdates()
        observeFeeds()
        fetchAndPersistNoteFeeds()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.RequestUserDataUpdate -> updateUserData()
                    UiEvent.RefreshNoteFeeds -> fetchAndPersistNoteFeeds()
                    UiEvent.RestoreDefaultNoteFeeds -> restoreDefaultNoteFeeds()
                }
            }
        }
    }

    private fun restoreDefaultNoteFeeds() =
        viewModelScope.launch {
            try {
                setState { copy(loading = true) }
                feedsRepository.fetchAndPersistDefaultFeeds(
                    userId = activeAccountStore.activeUserId(),
                    givenDefaultFeeds = emptyList(),
                    specKind = FeedSpecKind.Notes,
                )
            } catch (error: MissingPrivateKeyException) {
                Timber.w(error)
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun fetchAndPersistNoteFeeds() =
        viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                retryNetworkCall {
                    feedsRepository.fetchAndPersistNoteFeeds(userId = activeAccountStore.activeUserId())
                }
            } catch (error: MissingPrivateKeyException) {
                restoreDefaultNoteFeeds()
                Timber.w(error)
            } catch (error: WssException) {
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
