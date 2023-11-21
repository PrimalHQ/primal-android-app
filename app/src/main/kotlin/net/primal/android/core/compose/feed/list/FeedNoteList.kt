package net.primal.android.core.compose.feed.list

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnailsRow
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.FeedPostsSyncStats
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FeedNoteList(
    feedListState: LazyListState,
    pagingItems: LazyPagingItems<FeedPostUi>,
    walletConnected: Boolean,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onPostLikeClick: (FeedPostUi) -> Unit,
    onZapClick: (FeedPostUi, ULong?, String?) -> Unit,
    onRepostClick: (FeedPostUi) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onPostQuoteClick: (FeedPostUi) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
    onWalletUnavailable: () -> Unit,
    defaultZapAmount: ULong? = null,
    zapOptions: List<ULong>? = null,
    syncStats: FeedPostsSyncStats = FeedPostsSyncStats(),
    paddingValues: PaddingValues = PaddingValues(0.dp),
    bottomBarHeightPx: Float = 0F,
    bottomBarOffsetHeightPx: Float = 0F,
    onScrolledToTop: (() -> Unit)? = null,
    onMuteClick: ((String) -> Unit)? = null,
) {
    val uiScope = rememberCoroutineScope()

    LaunchedEffect(feedListState) {
        snapshotFlow { feedListState.firstVisibleItemIndex == 0 }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                onScrolledToTop?.invoke()
            }
    }

    val newPostsCount = syncStats.postsCount

    LaunchedEffect(pagingItems) {
        while (true) {
            val syncInterval = 30 + Random.nextInt(-5, 5)
            delay(syncInterval.seconds)
            if (newPostsCount < 100) {
                pagingItems.refresh()
            }
        }
    }

    val canScrollUp by remember(feedListState) {
        derivedStateOf {
            feedListState.firstVisibleItemIndex > 0
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
            onMediaClick = onMediaClick,
            onWalletUnavailable = onWalletUnavailable,
            defaultZapAmount = defaultZapAmount,
            zapOptions = zapOptions,
            onMuteClick = onMuteClick,
        )

        AnimatedVisibility(
            visible = canScrollUp && newPostsCount > 0,
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
private fun NewPostsButton(syncStats: FeedPostsSyncStats, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .background(
                color = AppTheme.colorScheme.primary,
                shape = AppTheme.shapes.extraLarge,
            )
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarThumbnailsRow(
            modifier = Modifier.padding(start = 6.dp),
            avatarCdnImages = syncStats.avatarCdnImages,
            onClick = { onClick() },
        )

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
