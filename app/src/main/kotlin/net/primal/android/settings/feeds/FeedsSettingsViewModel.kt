package net.primal.android.settings.feeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.model.primal.content.ContentFeedData
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import javax.inject.Inject

@HiltViewModel
class FeedsSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {
    private val _state = MutableStateFlow(FeedsSettingsContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: FeedsSettingsContract.UiState.() -> FeedsSettingsContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val _event: MutableSharedFlow<FeedsSettingsContract.UiEvent> = MutableSharedFlow()
    fun setEvent(event: FeedsSettingsContract.UiEvent) =
        viewModelScope.launch { _event.emit(event) }

    init {
        observeActiveUserAccount()
        observeEvents()
    }

    private fun observeEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                is FeedsSettingsContract.UiEvent.FeedRemoved -> feedRemovedHandler(event = it)
                is FeedsSettingsContract.UiEvent.FeedReordered -> feedReorderedHandler(event = it)
            }
        }
    }

    private fun observeActiveUserAccount() = viewModelScope.launch {
        activeAccountStore.activeUserAccount.collect {
            setState {
                copy(feeds = it.appSettings?.feeds?.map {
                    Feed(
                        name = it.name,
                        directive = it.directive,
                        isRemovable = it.directive != activeAccountStore.activeUserId()
                    )
                } ?: emptyList())
            }
        }
    }

    private suspend fun feedRemovedHandler(event: FeedsSettingsContract.UiEvent.FeedRemoved) {
        try {
            settingsRepository.removeAndPersistUserFeed(
                userId = activeAccountStore.activeUserId(),
                directive = event.directive
            )
        } catch (error: WssException) {
            setState {
                copy(
                    error = FeedsSettingsContract.UiState.SettingsFeedsError.FailedToRemoveFeed(
                        error
                    )
                )
            }
        }
    }

    private suspend fun feedReorderedHandler(event: FeedsSettingsContract.UiEvent.FeedReordered) {
        val fromItem = state.value.feeds[event.from]
        val toItem = state.value.feeds[event.to]

        val newFeeds = state.value.feeds.toMutableList()
        newFeeds[event.from] = toItem
        newFeeds[event.to] = fromItem

        try {
            settingsRepository.updateAndPersistFeeds(
                userId = activeAccountStore.activeUserId(),
                feeds = newFeeds.map { ContentFeedData(name = it.name, directive = it.directive) })
        } catch (error: WssException) {
            setState {
                copy(
                    error = FeedsSettingsContract.UiState.SettingsFeedsError.FailedToReorderFeeds(
                        error
                    )
                )
            }
        }
    }
}


