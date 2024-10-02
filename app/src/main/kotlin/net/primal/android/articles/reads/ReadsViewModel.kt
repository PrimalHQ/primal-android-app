package net.primal.android.articles.reads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.articles.reads.ReadsScreenContract.UiState
import net.primal.android.feeds.repository.FeedsRepository
import net.primal.android.feeds.ui.model.asFeedUi
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.subscriptions.SubscriptionsManager

@HiltViewModel
class ReadsViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val subscriptionsManager: SubscriptionsManager,
    private val feedsRepository: FeedsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
        observeActiveAccount()
        observeBadgesUpdates()
        observeFeeds()
    }

    private fun observeFeeds() =
        viewModelScope.launch {
            feedsRepository.observeReadsFeeds()
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
                setState {
                    copy(activeAccountAvatarCdnImage = it.avatarCdnImage)
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
