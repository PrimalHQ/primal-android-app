package net.primal.android.core.compose.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnailListItemImage
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
    walletConnected: Boolean,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onPostLikeClick: (FeedPostUi) -> Unit,
    onZapClick: (FeedPostUi, Int?, String?) -> Unit,
    onRepostClick: (FeedPostUi) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onPostQuoteClick: (FeedPostUi) -> Unit,
    onHashtagClick: (String) -> Unit,
    onWalletUnavailable: () -> Unit,
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
            pagingItems = pagingItems,
            contentPadding = paddingValues,
            listState = feedListState,
            walletConnected = walletConnected,
            onPostClick = onPostClick,
            onProfileClick = onProfileClick,
            onPostLikeClick = onPostLikeClick,
            onZapClick = onZapClick,
            onRepostClick = onRepostClick,
            onPostReplyClick = onPostReplyClick,
            onPostQuoteClick = onPostQuoteClick,
            onHashtagClick = onHashtagClick,
            onWalletUnavailable = onWalletUnavailable,
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

@Composable
private fun NewPostsButton(
    syncStats: FeedPostsSyncStats,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        AppTheme.extraColorScheme.brand1,
                        AppTheme.extraColorScheme.brand2
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
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


