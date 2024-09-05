package net.primal.android.notes.feed

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import fr.acinq.lightning.utils.UUID
import net.primal.android.core.compose.feed.FeedNoteList
import net.primal.android.core.compose.feed.model.ZappingState
import net.primal.android.core.compose.feed.note.events.NoteCallbacks
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround

@Composable
fun NoteFeedList(
    feedSpec: String,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    previewMode: Boolean = false,
    header: @Composable (LazyItemScope.() -> Unit)? = null,
    stickyHeader: @Composable (LazyItemScope.() -> Unit)? = null,
) {
    val viewModel = hiltViewModel<NoteFeedViewModel, NoteFeedViewModel.Factory>(
        key = if (!previewMode) feedSpec else UUID.randomUUID().toString(),
        creationCallback = { factory -> factory.create(feedSpec = feedSpec) },
    )
    val uiState = viewModel.state.collectAsState()

    NoteFeedList(
        state = uiState.value,
        noteCallbacks = noteCallbacks,
        contentPadding = contentPadding,
        header = header,
        stickyHeader = stickyHeader,
        onGoToWallet = onGoToWallet,
    )
}

@Composable
private fun NoteFeedList(
    state: NoteFeedContract.UiState,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    header: @Composable (LazyItemScope.() -> Unit)? = null,
    stickyHeader: @Composable (LazyItemScope.() -> Unit)? = null,
) {
    val pagingItems = state.notes.collectAsLazyPagingItems()
    val feedListState = pagingItems.rememberLazyListStatePagingWorkaround()

    FeedNoteList(
        pagingItems = pagingItems,
        feedListState = feedListState,
        // state.zappingState
        zappingState = ZappingState(),
        noteCallbacks = noteCallbacks,
        onZapClick = { post, zapAmount, zapDescription ->
//            eventPublisher(
//                FeedContract.UiEvent.ZapAction(
//                    postId = post.postId,
//                    postAuthorId = post.authorId,
//                    zapAmount = zapAmount,
//                    zapDescription = zapDescription,
//                ),
//            )
        },
        onPostLikeClick = {
//            eventPublisher(
//                FeedContract.UiEvent.PostLikeAction(
//                    postId = it.postId,
//                    postAuthorId = it.authorId,
//                ),
//            )
        },
        onRepostClick = {
//            eventPublisher(
//                FeedContract.UiEvent.RepostAction(
//                    postId = it.postId,
//                    postAuthorId = it.authorId,
//                    postNostrEvent = it.rawNostrEventJson,
//                ),
//            )
        },
        onGoToWallet = onGoToWallet,
        paddingValues = contentPadding,
        onScrolledToTop = {
//            eventPublisher(FeedContract.UiEvent.FeedScrolledToTop)
        },
        onMuteClick = {
//            eventPublisher(FeedContract.UiEvent.MuteAction(it))
        },
        onBookmarkClick = {
//            eventPublisher(FeedContract.UiEvent.BookmarkAction(noteId = it))
        },
        onReportContentClick = { type, profileId, noteId ->
//            eventPublisher(
//                FeedContract.UiEvent.ReportAbuse(
//                    reportType = type,
//                    profileId = profileId,
//                    noteId = noteId,
//                ),
//            )
        },
        header = header,
        stickyHeader = stickyHeader,
    )
}
