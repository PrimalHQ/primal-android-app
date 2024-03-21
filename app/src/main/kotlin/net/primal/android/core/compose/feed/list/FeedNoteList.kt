package net.primal.android.core.compose.feed.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.paging.compose.LazyPagingItems
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.withContext
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.FeedPostsSyncStats
import net.primal.android.core.compose.feed.model.ZappingState
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.profile.report.OnReportContentClick

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FeedNoteList(
    feedListState: LazyListState,
    pagingItems: LazyPagingItems<FeedPostUi>,
    zappingState: ZappingState,
    syncStats: FeedPostsSyncStats = FeedPostsSyncStats(),
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onPostLikeClick: (FeedPostUi) -> Unit,
    onZapClick: (FeedPostUi, ULong?, String?) -> Unit,
    onRepostClick: (FeedPostUi) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onPostQuoteClick: (FeedPostUi) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
    onGoToWallet: () -> Unit,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    onScrolledToTop: (() -> Unit)? = null,
    onMuteClick: ((String) -> Unit)? = null,
    onReportContentClick: OnReportContentClick,
) {
    LaunchedEffect(feedListState, onScrolledToTop) {
        withContext(Dispatchers.IO) {
            snapshotFlow { feedListState.firstVisibleItemIndex == 0 }
                .distinctUntilChanged()
                .filter { it }
                .collect {
                    onScrolledToTop?.invoke()
                }
        }
    }

    LaunchedEffect(pagingItems, syncStats) {
        withContext(Dispatchers.IO) {
            while (true) {
                val syncInterval = 30 + Random.nextInt(-5, 5)
                delay(syncInterval.seconds)
                if (syncStats.postsCount < 100) {
                    pagingItems.refresh()
                }
            }
        }
    }

    DisposableLifecycleObserverEffect(pagingItems) {
        if (it == Lifecycle.Event.ON_RESUME) {
            pagingItems.refresh()
        }
    }

    FeedLazyColumn(
        pagingItems = pagingItems,
        contentPadding = paddingValues,
        listState = feedListState,
        zappingState = zappingState,
        onPostClick = onPostClick,
        onProfileClick = onProfileClick,
        onPostLikeClick = onPostLikeClick,
        onZapClick = onZapClick,
        onRepostClick = onRepostClick,
        onPostReplyClick = onPostReplyClick,
        onPostQuoteClick = onPostQuoteClick,
        onHashtagClick = onHashtagClick,
        onMediaClick = onMediaClick,
        onGoToWallet = onGoToWallet,
        onMuteClick = onMuteClick,
        onReportContentClick = onReportContentClick,
    )
}
