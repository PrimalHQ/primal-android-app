package net.primal.android.settings.feeds.model

sealed class FeedAction {
    data class ConfirmRemove(
        var directive: String,
        var name: String,
        var openDialog: Boolean,
    ) : FeedAction()
    data object ConfirmRestoreDefaults : FeedAction()
    data object Inactive : FeedAction()
}
