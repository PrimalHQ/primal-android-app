package net.primal.android.core.compose.feed

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import net.primal.android.R
import net.primal.android.core.compose.feed.model.FeedPostAction
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.isEmpty

@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@Composable
fun FeedLazyColumn(
    pagingItems: LazyPagingItems<FeedPostUi>,
    contentPadding: PaddingValues,
    listState: LazyListState,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onPostLikeClick: (FeedPostUi) -> Unit,
    onRepostClick: (FeedPostUi) -> Unit,
    onZapClick: (FeedPostUi, Int?, String?) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onPostQuoteClick: (FeedPostUi) -> Unit,
    onHashtagClick: (String) -> Unit,
    onWalletUnavailable: () -> Unit,
    walletConnected: Boolean,
    shouldShowLoadingState: Boolean = true,
    shouldShowNoContentState: Boolean = true,
    header: @Composable (LazyItemScope.() -> Unit)? = null,
    stickyHeader: @Composable (LazyItemScope.() -> Unit)? = null,
) {

    var repostQuotePostConfirmation by remember { mutableStateOf<FeedPostUi?>(null) }
    if (repostQuotePostConfirmation != null) repostQuotePostConfirmation?.let { post ->
        RepostOrQuoteBottomSheet(
            onDismiss = { repostQuotePostConfirmation = null },
            onRepostClick = { onRepostClick(post) },
            onPostQuoteClick = { onPostQuoteClick(post) },
        )
    }

    var zapOptionsPostConfirmation by remember { mutableStateOf<FeedPostUi?>(null) }
    if (zapOptionsPostConfirmation != null) zapOptionsPostConfirmation?.let { post ->
        ZapBottomSheet(
            onDismissRequest = { zapOptionsPostConfirmation = null },
            receiverName = post.authorName,
            amount = 42,
            onZap = { zapAmount, zapDescription ->
                onZapClick(post, zapAmount, zapDescription)
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(4.dp),
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

        when (pagingItems.loadState.mediator?.prepend) {
            is LoadState.Error -> item(contentType = "Error") {
                FeedErrorLoading(
                    text = stringResource(R.string.feed_error_loading_prev_page)
                )
            }

            else -> Unit
        }

        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey(key = { "${it.postId}${it.repostId}" }),
            contentType = pagingItems.itemContentType()
        ) { index ->
            val item = pagingItems[index]

            when {
                item != null -> FeedPostListItem(
                    data = item,
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
                )

                else -> {}
            }
        }

        if (pagingItems.isEmpty()) {
            when (pagingItems.loadState.refresh) {
                LoadState.Loading -> {
                    if (shouldShowLoadingState) {
                        item(contentType = "LoadingRefresh") {
                            FeedLoading(
                                modifier = Modifier.fillParentMaxSize(),
                            )
                        }
                    }
                }

                is LoadState.NotLoading -> {
                    if (shouldShowNoContentState) {
                        item(contentType = "NoContent") {
                            FeedNoContent(
                                modifier = Modifier.fillParentMaxSize(),
                                onRefresh = { pagingItems.refresh() }
                            )
                        }
                    }
                }

                is LoadState.Error -> Unit
            }
        }

        when (pagingItems.loadState.mediator?.append) {
            LoadState.Loading -> item(contentType = "LoadingAppend") {
                FeedLoading(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                )
            }

            is LoadState.Error -> item(contentType = "Error") {
                FeedErrorLoading(
                    text = stringResource(R.string.feed_error_loading_next_page)
                )
            }

            else -> Unit
        }
    }
}
