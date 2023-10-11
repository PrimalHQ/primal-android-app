package net.primal.android.settings.feeds

interface FeedsSettingsContract {
    data class UiState(
        val feeds: List<Feed> = emptyList(),
        val error: SettingsFeedsError? = null
    ) {
        sealed class SettingsFeedsError {
            data class FailedToRemoveFeed(val throwable: Throwable) : SettingsFeedsError()
            data class FailedToRestoreDefaultFeeds(val throwable: Throwable) : SettingsFeedsError()
        }
    }

    sealed class UiEvent {
        data class FeedRemoved(val directive: String) : UiEvent()
        data object RestoreDefaultFeeds : UiEvent()
    }
}

data class Feed(val name: String, val directive: String, val isRemovable: Boolean)

sealed class FeedAction {
    data class ConfirmRemove(var directive: String, var name: String, var openDialog: Boolean) : FeedAction()
    data object ConfirmRestoreDefaults : FeedAction()
    data object Inactive : FeedAction()
}

//data class RemoveFeedPrompt(var directive: String, var name: String, var openDialog: Boolean)