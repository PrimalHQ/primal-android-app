package net.primal.android.core.compose.feed.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import net.primal.android.R
import net.primal.android.core.compose.ListLoading
import net.primal.android.core.compose.ListLoadingError
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.feed.RepostOrQuoteBottomSheet
import net.primal.android.core.compose.feed.ZapBottomSheet
import net.primal.android.core.compose.feed.model.FeedPostAction
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.note.FeedNoteCard
import net.primal.android.core.compose.isEmpty

@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@Composable
fun FeedLazyColumn(
    pagingItems: LazyPagingItems<FeedPostUi>,
    contentPadding: PaddingValues,
    listState: LazyListState,
    onMuteClick: ((String) -> Unit)? = null,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onPostLikeClick: (FeedPostUi) -> Unit,
    onRepostClick: (FeedPostUi) -> Unit,
    onZapClick: (FeedPostUi, ULong?, String?) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onPostQuoteClick: (FeedPostUi) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
    onWalletUnavailable: () -> Unit,
    walletConnected: Boolean,
    defaultZapAmount: ULong? = null,
    zapOptions: List<ULong>? = null,
    shouldShowLoadingState: Boolean = true,
    shouldShowNoContentState: Boolean = true,
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

    var zapOptionsPostConfirmation by remember { mutableStateOf<FeedPostUi?>(null) }
    if (zapOptionsPostConfirmation != null) {
        zapOptionsPostConfirmation?.let { post ->
            ZapBottomSheet(
                onDismissRequest = { zapOptionsPostConfirmation = null },
                receiverName = post.authorName,
                defaultZapAmount = defaultZapAmount ?: 42.toULong(),
                userZapOptions = zapOptions,
                onZap = { zapAmount, zapDescription ->
                    onZapClick(post, zapAmount, zapDescription)
                },
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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

        if (pagingItems.loadState.mediator?.prepend is LoadState.Error) {
            item(contentType = "Error") {
                ListLoadingError(
                    text = stringResource(R.string.app_error_loading_prev_page),
                )
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
                        onPostClick = { postId -> onPostClick(postId) },
                        onProfileClick = { profileId -> onProfileClick(profileId) },
                        onPostAction = { postAction ->
                            when (postAction) {
                                FeedPostAction.Reply -> onPostReplyClick(item.postId)
                                FeedPostAction.Zap -> {
                                    if (walletConnected) {
                                        onZapClick(item, null, null)
                                    } else {
                                        onWalletUnavailable()
                                    }
                                }

                                FeedPostAction.Like -> onPostLikeClick(item)
                                FeedPostAction.Repost -> repostQuotePostConfirmation = item
                            }
                        },
                        onPostLongClickAction = { postAction ->
                            when (postAction) {
                                FeedPostAction.Zap -> {
                                    if (walletConnected) {
                                        zapOptionsPostConfirmation = item
                                    } else {
                                        onWalletUnavailable()
                                    }
                                }

                                else -> Unit
                            }
                        },
                        onHashtagClick = onHashtagClick,
                        onMuteUserClick = { onMuteClick?.invoke(item.authorId) },
                        onMediaClick = onMediaClick,
                    )

                    PrimalDivider()
                }

                else -> {}
            }
        }

        if (pagingItems.isEmpty()) {
            when (pagingItems.loadState.refresh) {
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
                                noContentText = stringResource(id = R.string.feed_no_content),
                                onRefresh = { pagingItems.refresh() },
                            )
                        }
                    }
                }

                is LoadState.Error -> Unit
            }
        }

        when (pagingItems.loadState.mediator?.append) {
            LoadState.Loading -> item(contentType = "LoadingAppend") {
                ListLoading(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                )
            }

            is LoadState.Error -> item(contentType = "Error") {
                ListLoadingError(
                    text = stringResource(R.string.app_error_loading_next_page),
                )
            }

            else -> Unit
        }

        item(contentType = "Footer") {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}
