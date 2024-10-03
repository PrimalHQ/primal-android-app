package net.primal.android.feeds.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import net.primal.android.articles.feed.ArticleFeedList
import net.primal.android.feeds.domain.DvmFeed
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.feeds.domain.buildSpec
import net.primal.android.feeds.item.DvmFeedListItem
import net.primal.android.notes.feed.NoteFeedList
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.theme.AppTheme

@Composable
fun DvmHeaderAndFeedList(
    dvmFeed: DvmFeed,
    modifier: Modifier = Modifier,
    extended: Boolean = false,
    showFollowsActionsAvatarRow: Boolean = false,
    clipShape: Shape? = AppTheme.shapes.small,
) {
    Column(modifier = modifier) {
        when (dvmFeed.kind) {
            FeedSpecKind.Reads -> {
                ArticleFeedList(
                    feedSpec = dvmFeed.buildSpec(specKind = dvmFeed.kind),
                    previewMode = true,
                    pullToRefreshEnabled = false,
                    header = {
                        DvmFeedListItem(
                            data = dvmFeed,
                            extended = extended,
                            showFollowsActionsAvatarRow = showFollowsActionsAvatarRow,
                            clipShape = clipShape,
                        )
                    },
                    onArticleClick = {},
                )
            }

            FeedSpecKind.Notes -> {
                NoteFeedList(
                    feedSpec = dvmFeed.buildSpec(specKind = dvmFeed.kind),
                    noteCallbacks = NoteCallbacks(),
                    previewMode = true,
                    pullToRefreshEnabled = false,
                    pollingEnabled = false,
                    header = {
                        DvmFeedListItem(
                            data = dvmFeed,
                            extended = extended,
                            showFollowsActionsAvatarRow = showFollowsActionsAvatarRow,
                            clipShape = clipShape,
                        )
                    },
                    onGoToWallet = {},
                )
            }

            null -> {}
        }
    }
}
