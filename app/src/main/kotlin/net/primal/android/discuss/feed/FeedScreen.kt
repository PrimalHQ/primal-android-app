package net.primal.android.discuss.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.feed.FeedPostList
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.icons.primaliconpack.FeedPicker
import net.primal.android.crypto.hexToNoteHrp
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalBottomBarHeightDp
import net.primal.android.drawer.PrimalDrawerScaffold
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme

@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    onFeedsClick: () -> Unit,
    onNewPostClick: (String?) -> Unit,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.setEvent(FeedContract.UiEvent.RequestSyncSettings)
    }

    FeedScreen(
        state = uiState.value,
        eventPublisher = { viewModel.setEvent(it) },
        onFeedsClick = onFeedsClick,
        onNewPostClick = onNewPostClick,
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
    onNewPostClick: (String?) -> Unit,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)
    val feedListState = rememberLazyListState()

    val bottomBarHeight = PrimalBottomBarHeightDp
    var bottomBarOffsetHeightPx by remember { mutableStateOf(0f) }

    val focusMode by remember { derivedStateOf { bottomBarOffsetHeightPx < 0f } }

    PrimalDrawerScaffold(
        drawerState = drawerState,
        activeDestination = PrimalTopLevelDestination.Feed,
        onActiveDestinationClick = { uiScope.launch { feedListState.animateScrollToItem(0) } },
        onPrimaryDestinationChanged = onPrimaryDestinationChanged,
        onDrawerDestinationClick = onDrawerDestinationClick,
        bottomBarHeight = bottomBarHeight,
        onBottomBarOffsetChange = { bottomBarOffsetHeightPx = it },
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
                onPostLikeClick = {
                    eventPublisher(
                        FeedContract.UiEvent.PostLikeAction(
                            postId = it.postId,
                            postAuthorId = it.authorId,
                        )
                    )
                },
                onPostReplyClick = {

                },
                onRepostClick = {
                    eventPublisher(
                        FeedContract.UiEvent.RepostAction(
                            postId = it.postId,
                            postAuthorId = it.authorId,
                            postNostrEvent = it.rawNostrEventJson,
                        )
                    )
                },
                onPostQuoteClick = {
                    onNewPostClick("\n\nnostr:${it.postId.hexToNoteHrp()}")
                },
                syncStats = state.syncStats,
                paddingValues = paddingValues,
                feedListState = feedListState,
                bottomBarHeightPx = with(LocalDensity.current) {
                    bottomBarHeight.roundToPx().toFloat()
                },
                bottomBarOffsetHeightPx = bottomBarOffsetHeightPx,
                onScrolledToTop = {
                    eventPublisher(FeedContract.UiEvent.FeedScrolledToTop)
                },
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !focusMode,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                FloatingActionButton(
                    onClick = { onNewPostClick(null) },
                    modifier = Modifier
                        .size(bottomBarHeight)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    AppTheme.extraColorScheme.brand1,
                                    AppTheme.extraColorScheme.brand2
                                ),
                            ),
                            shape = FloatingActionButtonDefaults.shape,
                        ),
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                    containerColor = Color.Unspecified,
                    content = {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                    },
                )
            }
        }
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
            onNewPostClick = {},
            onPostClick = {},
            onProfileClick = {},
            onPrimaryDestinationChanged = {},
            onDrawerDestinationClick = {},
        )
    }

}
