package net.primal.android.settings.feeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.settings.feeds.model.Feed
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.accounts.active.ActiveAccountStore

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

    private val events: MutableSharedFlow<FeedsSettingsContract.UiEvent> = MutableSharedFlow()
    fun setEvent(event: FeedsSettingsContract.UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeActiveUserAccount()
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is FeedsSettingsContract.UiEvent.FeedRemoved -> feedRemovedHandler(event = it)
                    is FeedsSettingsContract.UiEvent.RestoreDefaultFeeds -> restoreDefaultFeedsHandler()
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

    private fun feedRemovedHandler(event: FeedsSettingsContract.UiEvent.FeedRemoved) =
        viewModelScope.launch {
            try {
                settingsRepository.removeAndPersistUserFeed(
                    userId = activeAccountStore.activeUserId(),
                    directive = event.directive,
                )
            } catch (error: WssException) {
                setState {
                    copy(
                        error = FeedsSettingsContract.UiState.SettingsFeedsError.FailedToRemoveFeed(
                            error,
                        ),
                    )
                }
            }
        }

    private fun restoreDefaultFeedsHandler() =
        viewModelScope.launch {
            try {
                settingsRepository.restoreDefaultFeeds(userId = activeAccountStore.activeUserId())
            } catch (error: WssException) {
                setState {
                    copy(
                        error = FeedsSettingsContract.UiState.SettingsFeedsError.FailedToRestoreDefaultFeeds(
                            error,
                        ),
                    )
                }
            }
        }
}
