package net.primal.android.core.compose.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.FeedPostsSyncStats
import net.primal.android.theme.AppTheme
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@Composable
fun FeedPostList(
    posts: Flow<PagingData<FeedPostUi>>,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
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

    when {
        pagingItems.isEmpty() -> {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.Center)
                )
            }
        }

        else -> {
            Box {
                FeedLazyColumn(
                    contentPadding = paddingValues,
                    pagingItems = pagingItems,
                    listState = feedListState,
                    onPostClick = onPostClick,
                    onProfileClick = onProfileClick,
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
    }
}

@Composable
private fun FeedLazyColumn(
    contentPadding: PaddingValues,
    pagingItems: LazyPagingItems<FeedPostUi>,
    listState: LazyListState,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        state = listState,
    ) {
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
                    onProfileClick = { profileId -> onProfileClick(profileId) }
                )

                else -> {}
            }
        }

        when (val appendLoadState = pagingItems.loadState.mediator?.append) {
            LoadState.Loading -> item(contentType = "Loading") {
                LoadingItem()
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
private fun ErrorItem(
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

@Composable
private fun LoadingItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Center)
        )
    }
}
