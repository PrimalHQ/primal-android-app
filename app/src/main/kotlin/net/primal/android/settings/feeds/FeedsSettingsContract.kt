package net.primal.android.settings.feeds

interface FeedsSettingsContract {
    data class UiState(
        val feeds: List<Feed> = emptyList(),
        val error: SettingsFeedsError? = null
    ) {
        sealed class SettingsFeedsError {
            data class FailedToRemoveFeed(val throwable: Throwable) : SettingsFeedsError()
            data class FailedToReorderFeeds(val throwable: Throwable) : SettingsFeedsError()
        }
    }

    sealed class UiEvent {
        data class FeedRemoved(val directive: String) : UiEvent()
        data class FeedReordered(val from: Int, val to: Int) : UiEvent()
    }
}

data class Feed(val name: String, val directive: String, val isRemovable: Boolean)