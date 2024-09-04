package net.primal.android.notes.feed

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import fr.acinq.lightning.utils.UUID
import net.primal.android.core.compose.feed.FeedNoteList
import net.primal.android.core.compose.feed.model.ZappingState
import net.primal.android.core.compose.feed.note.events.InvoicePayClickEvent
import net.primal.android.core.compose.feed.note.events.MediaClickEvent
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.crypto.hexToNoteHrp

@Composable
fun NoteFeedList(
    feedSpec: String,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onNewPostClick: (content: TextFieldValue?) -> Unit,
    onPostClick: (String) -> Unit,
    onArticleClick: (naddr: String) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (MediaClickEvent) -> Unit,
    onGoToWallet: () -> Unit,
    onPayInvoiceClick: ((InvoicePayClickEvent) -> Unit)? = null,
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
        contentPadding = contentPadding,
        header = header,
        stickyHeader = stickyHeader,
        onNewPostClick = onNewPostClick,
        onPostClick = onPostClick,
        onArticleClick = onArticleClick,
        onPostReplyClick = onPostReplyClick,
        onProfileClick = onProfileClick,
        onHashtagClick = onHashtagClick,
        onMediaClick = onMediaClick,
        onPayInvoiceClick = onPayInvoiceClick,
        onGoToWallet = onGoToWallet,
    )
}

@Composable
private fun NoteFeedList(
    state: NoteFeedContract.UiState,
    onNewPostClick: (content: TextFieldValue?) -> Unit,
    onPostClick: (String) -> Unit,
    onArticleClick: (naddr: String) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (MediaClickEvent) -> Unit,
    onGoToWallet: () -> Unit,
    onPayInvoiceClick: ((InvoicePayClickEvent) -> Unit)? = null,
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
        onPostClick = onPostClick,
        onArticleClick = onArticleClick,
        onProfileClick = onProfileClick,
        onPostReplyClick = onPostReplyClick,
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
        onPostQuoteClick = { onNewPostClick(TextFieldValue(text = "\n\nnostr:${it.postId.hexToNoteHrp()}")) },
        onHashtagClick = onHashtagClick,
        onGoToWallet = onGoToWallet,
        paddingValues = contentPadding,
        onScrolledToTop = {
//            eventPublisher(FeedContract.UiEvent.FeedScrolledToTop)
        },
        onMuteClick = {
//            eventPublisher(FeedContract.UiEvent.MuteAction(it))
        },
        onMediaClick = onMediaClick,
        onPayInvoiceClick = onPayInvoiceClick,
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
