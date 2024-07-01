package net.primal.android.settings.feeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.model.primal.content.ContentFeedData
import net.primal.android.settings.feeds.FeedsSettingsContract.UiState.SettingsFeedsError
import net.primal.android.settings.feeds.model.Feed
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import timber.log.Timber

@HiltViewModel
class FeedsSettingsViewModel @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val settingsRepository: SettingsRepository,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {
    private val _state = MutableStateFlow(FeedsSettingsContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: FeedsSettingsContract.UiState.() -> FeedsSettingsContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events: MutableSharedFlow<FeedsSettingsContract.UiEvent> = MutableSharedFlow()
    fun setEvent(event: FeedsSettingsContract.UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        fetchLatestAppSettings()
        observeActiveUserAccount()
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is FeedsSettingsContract.UiEvent.FeedRemoved -> removeFeedFromState(event)
                    is FeedsSettingsContract.UiEvent.FeedReordered -> reorderFeedInState(event)
                    is FeedsSettingsContract.UiEvent.RestoreDefaultFeeds -> restoreDefaultFeedsHandler()
                    FeedsSettingsContract.UiEvent.PersistFeeds -> publishFeedChanges()
                }
            }
        }

    private fun observeActiveUserAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        feeds = it.appSettings?.feeds?.map {
                            Feed(
                                name = it.name,
                                directive = it.directive,
                                isRemovable = !it.directive.contains(activeAccountStore.activeUserId()),
                            )
                        } ?: emptyList(),
                    )
                }
            }
        }

    private fun fetchLatestAppSettings() =
        viewModelScope.launch {
            try {
                settingsRepository.fetchAndPersistAppSettings(userId = activeAccountStore.activeUserId())
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun reorderFeedInState(event: FeedsSettingsContract.UiEvent.FeedReordered) {
        setState { copy(feeds = event.feeds) }
    }

    private fun removeFeedFromState(event: FeedsSettingsContract.UiEvent.FeedRemoved) {
        setState {
            copy(
                feeds = feeds.toMutableList().apply {
                    removeAll { it.directive == event.directive && it.name == event.name }
                }.toList(),
            )
        }
    }

    private fun restoreDefaultFeedsHandler() =
        viewModelScope.launch {
            try {
                settingsRepository.restoreDefaultUserFeeds(userId = activeAccountStore.activeUserId())
            } catch (error: WssException) {
                setState { copy(error = SettingsFeedsError.FailedToRestoreDefaultFeeds(error)) }
            }
        }

    private fun publishFeedChanges() =
        // Launching in a new scope to survive view model destruction
        CoroutineScope(dispatcherProvider.io()).launch {
            try {
                settingsRepository.reorderAndPersistUserFeeds(
                    userId = activeAccountStore.activeUserId(),
                    newOrder = _state.value.feeds.map { feed ->
                        ContentFeedData(
                            directive = feed.directive,
                            name = feed.name,
                        )
                    },
                )
            } catch (error: WssException) {
                Timber.w(error)
            }
        }
}
