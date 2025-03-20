package net.primal.android.feeds.dvm.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import net.primal.android.articles.feed.ArticleFeedList
import net.primal.android.core.errors.UiError
import net.primal.android.notes.feed.list.NoteFeedList
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.theme.AppTheme
import net.primal.domain.FeedSpecKind
import net.primal.domain.buildSpec

@Composable
fun DvmHeaderAndFeedList(
    dvmFeed: DvmFeedUi,
    modifier: Modifier = Modifier,
    extended: Boolean = false,
    showFollowsActionsAvatarRow: Boolean = false,
    clipShape: Shape? = AppTheme.shapes.small,
    onGoToWallet: (() -> Unit)? = null,
    onUiError: ((UiError) -> Unit)? = null,
) {
    Column(modifier = modifier) {
        val feedSpecKind = dvmFeed.data.kind
        when (feedSpecKind) {
            FeedSpecKind.Reads -> {
                ArticleFeedList(
                    feedSpec = dvmFeed.data.buildSpec(specKind = feedSpecKind),
                    previewMode = true,
                    pullToRefreshEnabled = false,
                    header = {
                        DvmFeedListItem(
                            data = dvmFeed,
                            extended = extended,
                            showFollowsActionsAvatarRow = showFollowsActionsAvatarRow,
                            clipShape = clipShape,
                            onGoToWallet = onGoToWallet,
                            onUiError = onUiError,
                        )
                    },
                    onArticleClick = {},
                    onUiError = {},
                    onGetPremiumClick = {},
                )
            }

            FeedSpecKind.Notes -> {
                NoteFeedList(
                    feedSpec = dvmFeed.data.buildSpec(specKind = feedSpecKind),
                    noteCallbacks = NoteCallbacks(),
                    previewMode = true,
                    pullToRefreshEnabled = false,
                    pollingEnabled = false,
                    showTopZaps = true,
                    header = {
                        DvmFeedListItem(
                            data = dvmFeed,
                            extended = extended,
                            showFollowsActionsAvatarRow = showFollowsActionsAvatarRow,
                            clipShape = clipShape,
                            onGoToWallet = onGoToWallet,
                        )
                    },
                    onGoToWallet = {},
                )
            }

            null -> {}
        }
    }
}
