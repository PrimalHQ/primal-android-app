package net.primal.android.discuss.feed

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.feed.FeedPostList
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.icons.primaliconpack.FeedPicker
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalBottomBarHeightDp
import net.primal.android.drawer.PrimalDrawerScaffold
import net.primal.android.theme.PrimalTheme

@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    onFeedsClick: () -> Unit,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    FeedScreen(
        state = uiState.value,
        eventPublisher = { viewModel.setEvent(it) },
        onFeedsClick = onFeedsClick,
        onPostClick = onPostClick,
        onProfileClick = onProfileClick,
        onPrimaryDestinationChanged = onTopLevelDestinationChanged,
        onDrawerDestinationClick = onDrawerScreenClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    state: FeedContract.UiState,
    eventPublisher: (FeedContract.UiEvent) -> Unit,
    onFeedsClick: () -> Unit,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)
    val feedListState = rememberLazyListState()

    val bottomBarHeight = PrimalBottomBarHeightDp
    val bottomBarOffsetHeightPx = remember { mutableStateOf(0f) }
    PrimalDrawerScaffold(
        drawerState = drawerState,
        activeDestination = PrimalTopLevelDestination.Feed,
        onActiveDestinationClick = { uiScope.launch { feedListState.animateScrollToItem(0) } },
        onPrimaryDestinationChanged = onPrimaryDestinationChanged,
        onDrawerDestinationClick = onDrawerDestinationClick,
        bottomBarHeight = bottomBarHeight,
        onBottomBarOffsetChange = { bottomBarOffsetHeightPx.value = it },
        topBar = {
            PrimalTopAppBar(
                title = state.feedTitle,
                avatarUrl = state.activeAccountAvatarUrl,
                navigationIcon = PrimalIcons.AvatarDefault,
                onNavigationIconClick = {
                    uiScope.launch { drawerState.open() }
                },
                actions = {
                    AppBarIcon(
                        icon = PrimalIcons.FeedPicker,
                        onClick = onFeedsClick,
                    )
                },
                scrollBehavior = it,
            )
        },
        content = { paddingValues ->
            FeedPostList(
                posts = state.posts,
                onPostClick = onPostClick,
                onProfileClick = onProfileClick,
                onPostLike = {
                    eventPublisher(
                        FeedContract.UiEvent.PostLikeAction(
                            postId = it.postId,
                            postAuthorId = it.authorId,
                        )
                    )
                },
                onReply = {

                },
                onRepost = {
                    eventPublisher(
                        FeedContract.UiEvent.RepostAction(
                            postId = it.postId,
                            postAuthorId = it.authorId,
                            postNostrEvent = it.rawNostrEventJson,
                        )
                    )
                },
                onQuote = {

                },
                syncStats = state.syncStats,
                paddingValues = paddingValues,
                feedListState = feedListState,
                bottomBarHeightPx = with(LocalDensity.current) {
                    bottomBarHeight.roundToPx().toFloat()
                },
                bottomBarOffsetHeightPx = bottomBarOffsetHeightPx.value,
                onScrolledToTop = {
                    eventPublisher(FeedContract.UiEvent.FeedScrolledToTop)
                },
            )
        },
    )
}

@Preview
@Composable
fun FeedScreenPreview() {
    PrimalTheme {
        FeedScreen(
            state = FeedContract.UiState(posts = flow { }),
            eventPublisher = {},
            onFeedsClick = {},
            onPostClick = {},
            onProfileClick = {},
            onPrimaryDestinationChanged = {},
            onDrawerDestinationClick = {},
        )
    }

}