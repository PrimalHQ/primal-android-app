package net.primal.android.articles.reads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.articles.reads.ReadsScreenContract.UiEvent
import net.primal.android.articles.reads.ReadsScreenContract.UiState
import net.primal.android.feeds.list.ui.model.asFeedUi
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.subscriptions.SubscriptionsManager
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.networking.utils.retryNetworkCall
import net.primal.domain.FeedSpecKind
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.cryptography.SigningKeyNotFoundException
import net.primal.domain.nostr.cryptography.SigningRejectedException
import net.primal.domain.repository.FeedsRepository
import timber.log.Timber

@HiltViewModel
class ReadsViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val subscriptionsManager: SubscriptionsManager,
    private val feedsRepository: FeedsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeActiveAccount()
        observeBadgesUpdates()
        observeFeeds()
        observeEvents()
        fetchAndPersistReadsFeeds()
    }

    private fun observeFeeds() =
        viewModelScope.launch {
            feedsRepository.observeReadsFeeds(userId = activeAccountStore.activeUserId())
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

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.RefreshReadsFeeds -> fetchAndPersistReadsFeeds()
                    UiEvent.RestoreDefaultFeeds -> restoreDefaultReadsFeeds()
                }
            }
        }

    private fun restoreDefaultReadsFeeds() =
        viewModelScope.launch {
            try {
                setState { copy(loading = true) }
                val userId = activeAccountStore.activeUserId()
                feedsRepository.fetchAndPersistDefaultFeeds(
                    userId = userId,
                    specKind = FeedSpecKind.Reads,
                    givenDefaultFeeds = emptyList(),
                )
            } catch (error: SignatureException) {
                Timber.w(error)
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun fetchAndPersistReadsFeeds() =
        viewModelScope.launch {
            setState { copy(loading = true) }
            val userId = activeAccountStore.activeUserId()
            try {
                retryNetworkCall {
                    feedsRepository.fetchAndPersistArticleFeeds(userId = userId)
                }
            } catch (error: SigningRejectedException) {
                Timber.w(error)
            } catch (error: SigningKeyNotFoundException) {
                restoreDefaultReadsFeeds()
                Timber.w(error)
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        activeAccountAvatarCdnImage = it.avatarCdnImage,
                        activeAccountLegendaryCustomization = it.primalLegendProfile?.asLegendaryCustomization(),
                    )
                }
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
}
