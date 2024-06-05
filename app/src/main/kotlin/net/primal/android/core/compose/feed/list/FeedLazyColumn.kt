package net.primal.android.core.compose.feed.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import net.primal.android.BuildConfig
import net.primal.android.R
import net.primal.android.core.compose.ListLoading
import net.primal.android.core.compose.ListLoadingError
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.feed.RepostOrQuoteBottomSheet
import net.primal.android.core.compose.feed.model.FeedPostAction
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.ZappingState
import net.primal.android.core.compose.feed.note.FeedNoteCard
import net.primal.android.core.compose.feed.note.events.InvoicePayClickEvent
import net.primal.android.core.compose.feed.note.events.MediaClickEvent
import net.primal.android.core.compose.feed.zaps.UnableToZapBottomSheet
import net.primal.android.core.compose.feed.zaps.ZapBottomSheet
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.compose.isNotEmpty
import net.primal.android.profile.report.OnReportContentClick
import net.primal.android.wallet.zaps.canZap
import timber.log.Timber

@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@Composable
fun FeedLazyColumn(
    modifier: Modifier = Modifier,
    pagingItems: LazyPagingItems<FeedPostUi>,
    contentPadding: PaddingValues,
    listState: LazyListState,
    zappingState: ZappingState,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onPostLikeClick: (FeedPostUi) -> Unit,
    onRepostClick: (FeedPostUi) -> Unit,
    onZapClick: (FeedPostUi, ULong?, String?) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onPostQuoteClick: (FeedPostUi) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (MediaClickEvent) -> Unit,
    onPayInvoiceClick: ((InvoicePayClickEvent) -> Unit)? = null,
    onGoToWallet: () -> Unit,
    onReportContentClick: OnReportContentClick,
    onMuteClick: ((String) -> Unit)? = null,
    onBookmarkClick: (noteId: String) -> Unit,
    shouldShowLoadingState: Boolean = true,
    shouldShowNoContentState: Boolean = true,
    showReplyTo: Boolean = true,
    noContentText: String = stringResource(id = R.string.feed_no_content),
    header: @Composable (LazyItemScope.() -> Unit)? = null,
    stickyHeader: @Composable (LazyItemScope.() -> Unit)? = null,
) {
    var repostQuotePostConfirmation by remember { mutableStateOf<FeedPostUi?>(null) }
    if (repostQuotePostConfirmation != null) {
        repostQuotePostConfirmation?.let { post ->
            RepostOrQuoteBottomSheet(
                onDismiss = { repostQuotePostConfirmation = null },
                onRepostClick = { onRepostClick(post) },
                onPostQuoteClick = { onPostQuoteClick(post) },
            )
        }
    }

    var showCantZapWarning by remember { mutableStateOf(false) }
    if (showCantZapWarning) {
        UnableToZapBottomSheet(
            zappingState = zappingState,
            onDismissRequest = { showCantZapWarning = false },
            onGoToWallet = onGoToWallet,
        )
    }

    var zapOptionsPostConfirmation by remember { mutableStateOf<FeedPostUi?>(null) }
    if (zapOptionsPostConfirmation != null) {
        zapOptionsPostConfirmation?.let { post ->
            ZapBottomSheet(
                onDismissRequest = { zapOptionsPostConfirmation = null },
                receiverName = post.authorName,
                zappingState = zappingState,
                onZap = { zapAmount, zapDescription ->
                    if (zappingState.canZap(zapAmount)) {
                        onZapClick(post, zapAmount.toULong(), zapDescription)
                    } else {
                        showCantZapWarning = true
                    }
                },
            )
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        state = listState,
    ) {
        if (stickyHeader != null) {
            stickyHeader {
                stickyHeader()
            }
        }

        if (header != null) {
            item {
                header()
            }
        }

        if (BuildConfig.FEATURE_PRIMAL_CRASH_REPORTER) {
            when (val prependMediatorLoadState = pagingItems.loadState.mediator?.prepend) {
                is LoadState.Error -> {
                    item(contentType = "PrependError") {
                        val error = prependMediatorLoadState.error
                        Timber.w(error)
                        ListLoadingError(
                            text = stringResource(R.string.app_error_loading_prev_page) + "\n${error.message}",
                        )
                    }
                }

                else -> Unit
            }
        }

        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey(key = { "${it.postId}${it.repostId}" }),
            contentType = pagingItems.itemContentType(),
        ) { index ->
            val item = pagingItems[index]

            when {
                item != null -> Column {
                    FeedNoteCard(
                        data = item,
                        shape = RectangleShape,
                        cardPadding = PaddingValues(all = 0.dp),
                        showReplyTo = showReplyTo,
                        onPostClick = { postId -> onPostClick(postId) },
                        onProfileClick = { profileId -> onProfileClick(profileId) },
                        onPostAction = { postAction ->
                            when (postAction) {
                                FeedPostAction.Reply -> onPostReplyClick(item.postId)
                                FeedPostAction.Zap -> {
                                    if (zappingState.canZap()) {
                                        onZapClick(item, null, null)
                                    } else {
                                        showCantZapWarning = true
                                    }
                                }

                                FeedPostAction.Like -> onPostLikeClick(item)
                                FeedPostAction.Repost -> repostQuotePostConfirmation = item
                            }
                        },
                        onPostLongClickAction = { postAction ->
                            when (postAction) {
                                FeedPostAction.Zap -> {
                                    if (zappingState.walletConnected) {
                                        zapOptionsPostConfirmation = item
                                    } else {
                                        showCantZapWarning = true
                                    }
                                }

                                else -> Unit
                            }
                        },
                        onHashtagClick = onHashtagClick,
                        onMuteUserClick = { onMuteClick?.invoke(item.authorId) },
                        onMediaClick = onMediaClick,
                        onReportContentClick = onReportContentClick,
                        onBookmarkClick = { onBookmarkClick(item.postId) },
                        onPayInvoiceClick = onPayInvoiceClick,
                    )

                    PrimalDivider()
                }

                else -> {}
            }
        }

        if (pagingItems.isEmpty()) {
            when (val refreshLoadState = pagingItems.loadState.refresh) {
                LoadState.Loading -> {
                    if (shouldShowLoadingState) {
                        item(contentType = "LoadingRefresh") {
                            ListLoading(
                                modifier = Modifier.fillParentMaxSize(),
                            )
                        }
                    }
                }

                is LoadState.NotLoading -> {
                    if (shouldShowNoContentState) {
                        item(contentType = "NoContent") {
                            ListNoContent(
                                modifier = Modifier.fillParentMaxSize(),
                                noContentText = noContentText,
                                onRefresh = { pagingItems.refresh() },
                            )
                        }
                    }
                }

                is LoadState.Error -> {
                    val error = refreshLoadState.error
                    Timber.w(error)
                    item(contentType = "RefreshError") {
                        ListNoContent(
                            modifier = Modifier.fillParentMaxSize(),
                            noContentText = stringResource(id = R.string.feed_error_loading),
                            onRefresh = { pagingItems.refresh() },
                        )
                    }
                }
            }
        }

        when (val appendMediatorLoadState = pagingItems.loadState.mediator?.append) {
            LoadState.Loading -> item(contentType = "LoadingAppend") {
                ListLoading(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(vertical = 8.dp),
                )
            }

            is LoadState.Error -> if (BuildConfig.FEATURE_PRIMAL_CRASH_REPORTER) {
                item(contentType = "AppendError") {
                    val error = appendMediatorLoadState.error
                    Timber.w(error)
                    ListLoadingError(
                        text = stringResource(R.string.app_error_loading_next_page) + "\n${error.message}",
                    )
                }
            }

            else -> Unit
        }

        if (pagingItems.isNotEmpty()) {
            item(contentType = "Footer") {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}
