package net.primal.android.discuss.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.discuss.list.FeedListContract.UiState
import net.primal.android.discuss.list.model.FeedUi
import net.primal.android.feed.db.Feed
import net.primal.android.feed.repository.FeedRepository
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import timber.log.Timber

@HiltViewModel
class FeedListViewModel @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val feedRepository: FeedRepository,
    private val settingsRepository: SettingsRepository,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    init {
        observeFeeds()
        fetchLatestFeeds()
    }

    private fun observeFeeds() =
        viewModelScope.launch {
            feedRepository.observeFeeds().collect { feeds ->
                setState {
                    copy(feeds = feeds.map { it.asFeedUi() })
                }
            }
        }

    private fun fetchLatestFeeds() =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    settingsRepository.fetchAndPersistAppSettings(userId = activeAccountStore.activeUserId())
                }
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun Feed.asFeedUi() = FeedUi(directive = this.directive, name = this.name)
}
