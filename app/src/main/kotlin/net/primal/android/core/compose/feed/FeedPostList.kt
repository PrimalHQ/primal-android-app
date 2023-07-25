package net.primal.android.core.compose.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnailListItemImage
import net.primal.android.core.compose.feed.model.FeedPostAction
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.FeedPostsSyncStats
import net.primal.android.core.compose.isEmpty
import net.primal.android.theme.AppTheme
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FeedPostList(
    posts: Flow<PagingData<FeedPostUi>>,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onPostLike: (FeedPostUi) -> Unit,
    onRepost: (FeedPostUi) -> Unit,
    onReply: (FeedPostUi) -> Unit,
    onQuote: (FeedPostUi) -> Unit,
    syncStats: FeedPostsSyncStats = FeedPostsSyncStats(),
    paddingValues: PaddingValues = PaddingValues(0.dp),
    feedListState: LazyListState = rememberLazyListState(),
    bottomBarHeightPx: Float = 0F,
    bottomBarOffsetHeightPx: Float = 0F,
    onScrolledToTop: (() -> Unit)? = null,
) {
    val uiScope = rememberCoroutineScope()
    val pagingItems = posts.collectAsLazyPagingItems()

    val seenPostIds = remember { mutableSetOf<String>() }
    LaunchedEffect(feedListState) {
        snapshotFlow { feedListState.layoutInfo.visibleItemsInfo }
            .mapNotNull { visibleItems ->
                visibleItems.mapNotNull {
                    if (!pagingItems.isEmpty() && it.index < pagingItems.itemCount) {
                        pagingItems.peek(it.index)?.postId
                    } else {
                        null
                    }
                }
            }
            .distinctUntilChanged()
            .collect {
                seenPostIds.addAll(it)
            }
    }

    val newPostsCount = syncStats.postsCount

    LaunchedEffect(feedListState) {
        snapshotFlow { feedListState.firstVisibleItemIndex == 0 }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                seenPostIds.clear()
                onScrolledToTop?.invoke()
            }
    }

    LaunchedEffect(pagingItems) {
        while (true) {
            val syncInterval = 30 + Random.nextInt(-5, 5)
            delay(syncInterval.seconds)
            if (newPostsCount < 100) {
                pagingItems.refresh()
            }
        }
    }

    Box {
        FeedLazyColumn(
            contentPadding = paddingValues,
            pagingItems = pagingItems,
            listState = feedListState,
            onPostClick = onPostClick,
            onProfileClick = onProfileClick,
            onPostLike = onPostLike,
            onRepost = onRepost,
            onReply = onReply,
            onQuote = onQuote,
        )

        AnimatedVisibility(
            visible = newPostsCount > 0,
            enter = fadeIn() + slideInVertically(),
            exit = slideOutVertically() + fadeOut(),
            modifier = Modifier
                .padding(paddingValues)
                .padding(top = 42.dp)
                .height(40.dp)
                .wrapContentWidth()
                .align(Alignment.TopCenter)
                .alpha(1 / bottomBarHeightPx * bottomBarOffsetHeightPx + 1f),
        ) {
            NewPostsButton(
                syncStats = syncStats,
                onClick = {
                    uiScope.launch {
                        feedListState.animateScrollToItem(0)
                    }
                },
            )
        }
    }
}

@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@Composable
fun FeedLazyColumn(
    contentPadding: PaddingValues,
    pagingItems: LazyPagingItems<FeedPostUi>,
    listState: LazyListState,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onPostLike: (FeedPostUi) -> Unit,
    onRepost: (FeedPostUi) -> Unit,
    onReply: (FeedPostUi) -> Unit,
    onQuote: (FeedPostUi) -> Unit,
    shouldShowLoadingState: Boolean = true,
    shouldShowNoContentState: Boolean = true,
    header: @Composable (LazyItemScope.() -> Unit)? = null,
    stickyHeader: @Composable (LazyItemScope.() -> Unit)? = null,
) {

    var repostQuotePostConfirmation by remember { mutableStateOf<FeedPostUi?>(null) }
    if (repostQuotePostConfirmation != null) repostQuotePostConfirmation?.let { post ->
        RepostOrQuoteBottomSheet(
            onDismiss = { repostQuotePostConfirmation = null },
            onRepost = { onRepost(post) },
            onQuote = { onQuote(post) },
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

        when (val prependLoadState = pagingItems.loadState.mediator?.prepend) {
            is LoadState.Error -> item(contentType = "Error") {
                ErrorItem(
                    text = stringResource(
                        R.string.feed_error_loading_prev_page,
                        prependLoadState.error.message
                            ?: prependLoadState.error.javaClass.simpleName
                    )
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
                            FeedPostAction.Reply -> onReply(item)
                            FeedPostAction.Zap -> Unit
                            FeedPostAction.Like -> onPostLike(item)
                            FeedPostAction.Repost -> {
                                repostQuotePostConfirmation = item
                            }
                        }
                    },
                )

                else -> {}
            }
        }

        if (pagingItems.isEmpty()) {
            when (pagingItems.loadState.refresh) {
                LoadState.Loading -> {
                    if (shouldShowLoadingState) {
                        item(contentType = "LoadingRefresh") {
                            LoadingItem(
                                modifier = Modifier.fillParentMaxSize(),
                            )
                        }
                    }
                }

                is LoadState.NotLoading -> {
                    if (shouldShowNoContentState) {
                        item(contentType = "NoContent") {
                            NoFeedContent(
                                modifier = Modifier.fillParentMaxSize(),
                                onRefresh = { pagingItems.refresh() }
                            )
                        }
                    }
                }

                is LoadState.Error -> Unit
            }
        }

        when (val appendLoadState = pagingItems.loadState.mediator?.append) {
            LoadState.Loading -> item(contentType = "LoadingAppend") {
                LoadingItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                )
            }

            is LoadState.Error -> item(contentType = "Error") {
                ErrorItem(
                    text = stringResource(
                        R.string.feed_error_loading_next_page,
                        appendLoadState.error.message
                            ?: appendLoadState.error.javaClass.simpleName
                    )
                )
            }

            else -> Unit
        }
    }
}

@Composable
fun LoadingItem(
    modifier: Modifier,
) {
    Box(
        modifier = modifier,
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Center)
        )
    }
}

@Composable
fun NoFeedContent(
    modifier: Modifier,
    onRefresh: () -> Unit,
) {
    val visible = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500L)
        visible.value = true
    }

    AnimatedVisibility(
        visible = visible.value,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 32.dp),
                text = stringResource(id = R.string.feed_no_content),
                textAlign = TextAlign.Center,
            )

            TextButton(
                modifier = Modifier.padding(vertical = 8.dp),
                onClick = onRefresh,
            ) {
                Text(
                    text = stringResource(id = R.string.feed_refresh_button).uppercase(),
                )
            }
        }
    }
}

@Composable
private fun NewPostsButton(
    syncStats: FeedPostsSyncStats,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        AppTheme.extraColorScheme.brand1,
                        AppTheme.extraColorScheme.brand2
                    ),
                ),
                shape = AppTheme.shapes.extraLarge
            )
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.padding(start = 6.dp)) {
            syncStats.avatarUrls.forEachIndexed { index, imageUrl ->
                Row {
                    Spacer(
                        modifier = Modifier
                            .height(10.dp)
                            .width((index * 24).dp)
                    )

                    AvatarThumbnailListItemImage(
                        modifier = Modifier.size(32.dp),
                        source = imageUrl,
                        hasBorder = true,
                        borderGradientColors = listOf(
                            Color.White,
                            Color.White
                        ),
                    )
                }
            }
        }

        Text(
            modifier = Modifier
                .padding(start = 12.dp, end = 16.dp)
                .padding(bottom = 4.dp)
                .wrapContentHeight(),
            text = stringResource(id = R.string.feed_new_posts_notice_general),
            style = AppTheme.typography.bodySmall,
            color = Color.White,
        )
    }
}

@Composable
fun ErrorItem(
    text: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Text(
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .align(Alignment.Center),
            text = text,
            textAlign = TextAlign.Center,
            style = AppTheme.typography.bodySmall,
        )
    }
}
