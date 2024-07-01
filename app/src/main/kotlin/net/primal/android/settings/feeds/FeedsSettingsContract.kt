package net.primal.android.settings.feeds

import net.primal.android.settings.feeds.model.Feed

interface FeedsSettingsContract {
    data class UiState(
        val feeds: List<Feed> = emptyList(),
        val error: SettingsFeedsError? = null,
    ) {
        sealed class SettingsFeedsError {
            data class FailedToRestoreDefaultFeeds(val throwable: Throwable) : SettingsFeedsError()
        }
    }

    sealed class UiEvent {
        data class FeedRemoved(val name: String, val directive: String) : UiEvent()
        data class FeedReordered(val feeds: List<Feed>) : UiEvent()
        data object RestoreDefaultFeeds : UiEvent()
        data object PersistFeeds : UiEvent()
    }
}
