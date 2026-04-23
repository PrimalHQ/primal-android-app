package net.primal.android.main.explore.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.compose.profile.model.mapAsUserProfileUi
import net.primal.android.feeds.DvmFeedListHandler
import net.primal.android.feeds.dvm.ui.DvmFeedUi
import net.primal.android.main.explore.home.NewExploreContract.UiEvent
import net.primal.android.main.explore.home.NewExploreContract.UiState
import net.primal.android.main.explore.people.model.asFollowPackUi
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.explore.ExploreRepository
import net.primal.domain.feeds.DvmFeed
import net.primal.domain.feeds.FeedsRepository
import net.primal.domain.feeds.buildSpec
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.posts.FeedRepository

@HiltViewModel
class NewExploreViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val userRepository: UserRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val dvmFeedListHandler: DvmFeedListHandler,
    private val feedsRepository: FeedsRepository,
    private val feedRepository: FeedRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeRecentUsers()
        observePopularUsers()
        fetchPopularUsers()
        observeFollowPacks()
        fetchFollowPacks()
        fetchAndObserveExploreFeeds()
        observeAllUserFeeds()
        observeEvents()
    }

    private fun observeRecentUsers() =
        viewModelScope.launch {
            userRepository.observeRecentUsers(ownerId = activeAccountStore.activeUserId())
                .distinctUntilChanged()
                .collect { users ->
                    setState { copy(recentUsers = users.map { it.mapAsUserProfileUi() }) }
                }
        }

    private fun observePopularUsers() =
        viewModelScope.launch {
            exploreRepository.observePopularUsers()
                .distinctUntilChanged()
                .collect { users ->
                    setState { copy(popularUsers = users.map { it.mapAsUserProfileUi() }) }
                }
        }

    private fun fetchPopularUsers() =
        viewModelScope.launch {
            runCatching { exploreRepository.fetchPopularUsers() }
                .onFailure { error ->
                    if (error is NetworkException) {
                        Napier.w(throwable = error) { "Failed to fetch popular users." }
                    }
                }
        }

    private fun observeFollowPacks() =
        viewModelScope.launch {
            exploreRepository.observeExploreFollowPacks()
                .distinctUntilChanged()
                .collect { packs ->
                    setState { copy(followPacks = packs.map { it.asFollowPackUi() }) }
                }
        }

    private fun fetchFollowPacks() =
        viewModelScope.launch {
            runCatching { exploreRepository.fetchExploreFollowPacks() }
                .onFailure { error ->
                    if (error is NetworkException) {
                        Napier.w(throwable = error) { "Failed to fetch follow packs." }
                    }
                }
        }

    private fun fetchAndObserveExploreFeeds() =
        viewModelScope.launch {
            runCatching {
                dvmFeedListHandler.fetchDvmFeedsAndObserveStatsUpdates(
                    scope = viewModelScope,
                    userId = activeAccountStore.activeUserId(),
                ) { dvmFeeds -> setState { copy(feeds = dvmFeeds) } }
            }.onFailure { error ->
                if (error is NetworkException) {
                    Napier.w(throwable = error) { "Failed to fetch explore feeds due to network error." }
                }
            }
        }

    private fun observeAllUserFeeds() =
        viewModelScope.launch {
            feedsRepository.observeAllFeeds(userId = activeAccountStore.activeUserId())
                .collect { feeds ->
                    setState { copy(userFeedSpecs = feeds.map { it.spec }) }
                }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.AddToUserFeeds -> addToUserFeeds(it.dvmFeed.data)
                    is UiEvent.RemoveFromUserFeeds -> removeFromUserFeeds(it.dvmFeed.data)
                    is UiEvent.ClearDvmFeed -> scheduleClearingDvmFeed(it.dvmFeed)
                }
            }
        }

    private fun scheduleClearingDvmFeed(dvmFeed: DvmFeedUi) =
        viewModelScope.launch {
            dvmFeed.data.kind?.let {
                feedRepository.removeFeedSpec(
                    userId = activeAccountStore.activeUserId(),
                    feedSpec = dvmFeed.data.buildSpec(specKind = it),
                )
            }
        }

    private fun addToUserFeeds(dvmFeed: DvmFeed) =
        viewModelScope.launch {
            runCatching {
                val userId = activeAccountStore.activeUserId()
                dvmFeed.kind?.let { feedSpecKind ->
                    feedsRepository.addDvmFeedLocally(
                        userId = userId,
                        dvmFeed = dvmFeed,
                        specKind = feedSpecKind,
                    )
                }
                feedsRepository.persistRemotelyAllLocalUserFeeds(userId = userId)
            }.onFailure { error ->
                when (error) {
                    is SignatureException ->
                        Napier.w(throwable = error) { "Failed to add DVM feed due to signature error." }
                    is NetworkException ->
                        Napier.w(throwable = error) { "Failed to add DVM feed due to network error." }
                }
            }
        }

    private fun removeFromUserFeeds(dvmFeed: DvmFeed) =
        viewModelScope.launch {
            runCatching {
                val userId = activeAccountStore.activeUserId()
                dvmFeed.kind?.let { feedSpecKind ->
                    feedsRepository.removeFeedLocally(
                        userId = userId,
                        feedSpec = dvmFeed.buildSpec(specKind = feedSpecKind),
                    )
                }
                feedsRepository.persistRemotelyAllLocalUserFeeds(userId = userId)
            }.onFailure { error ->
                when (error) {
                    is SignatureException ->
                        Napier.w(throwable = error) { "Failed to remove DVM feed due to signature error." }
                    is NetworkException ->
                        Napier.w(throwable = error) { "Failed to remove DVM feed due to network error." }
                }
            }
        }
}
