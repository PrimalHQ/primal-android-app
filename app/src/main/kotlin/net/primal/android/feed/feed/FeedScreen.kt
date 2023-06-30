package net.primal.android.feed.feed

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.PrimalNavigationBar
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.icons.primaliconpack.FeedPicker
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalDrawer
import net.primal.android.theme.PrimalTheme
import kotlin.math.roundToInt

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PrimalDrawer(
                drawerState = drawerState,
                onDrawerDestinationClick = onDrawerDestinationClick,
            )
        },
        content = {
            val topAppBarState = rememberTopAppBarState()
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)

            val bottomBarHeight = 64.dp
            val bottomBarHeightPx = with(LocalDensity.current) {
                bottomBarHeight.roundToPx().toFloat()
            }
            val bottomBarOffsetHeightPx = remember { mutableStateOf(0f) }
            val bottomBarNestedScrollConnection = remember {
                object : NestedScrollConnection {
                    override fun onPreScroll(
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        val delta = available.y
                        val newOffset = bottomBarOffsetHeightPx.value + delta
                        bottomBarOffsetHeightPx.value = newOffset.coerceIn(-bottomBarHeightPx, 0f)
                        return Offset.Zero
                    }
                }
            }

            Scaffold(
                modifier = Modifier
                    .nestedScroll(bottomBarNestedScrollConnection)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
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
                        scrollBehavior = scrollBehavior,
                    )
                },
                content = { paddingValues ->
                    FeedPostList(
                        posts = state.posts,
                        onPostClick = onPostClick,
                        onProfileClick = onProfileClick,
                        syncStats = state.syncStats,
                        paddingValues = paddingValues,
                        feedListState = feedListState,
                        bottomBarHeightPx = bottomBarHeightPx,
                        bottomBarOffsetHeightPx = bottomBarOffsetHeightPx.value,
                        onScrolledToTop = {
                            eventPublisher(FeedContract.UiEvent.FeedScrolledToTop)
                        },
                    )
                },
                bottomBar = {
                    PrimalNavigationBar(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .height(bottomBarHeight)
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = -bottomBarOffsetHeightPx.value.roundToInt()
                                )
                            },
                        activeDestination = PrimalTopLevelDestination.Feed,
                        onTopLevelDestinationChanged = onPrimaryDestinationChanged,
                        onActiveDestinationClick = {
                            uiScope.launch {
                                feedListState.animateScrollToItem(0)
                            }
                        }
                    )
                }
            )
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
            onPostClick = {},
            onProfileClick = {},
            onPrimaryDestinationChanged = {},
            onDrawerDestinationClick = {},
        )
    }

}