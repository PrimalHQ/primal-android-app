package net.primal.android.discuss.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.feed.list.FeedNoteList
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.icons.primaliconpack.FeedPicker
import net.primal.android.crypto.hexToNoteHrp
import net.primal.android.discuss.feed.FeedContract.UiState.FeedError
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalBottomBarHeightDp
import net.primal.android.drawer.PrimalDrawerScaffold
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    onFeedsClick: () -> Unit,
    onNewPostClick: (String?) -> Unit,
    onPostClick: (String) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
    onWalletUnavailable: () -> Unit,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.setEvent(FeedContract.UiEvent.RequestUserDataUpdate)
    }

    FeedScreen(
        state = uiState.value,
        eventPublisher = { viewModel.setEvent(it) },
        onFeedsClick = onFeedsClick,
        onNewPostClick = onNewPostClick,
        onPostClick = onPostClick,
        onPostReplyClick = onPostReplyClick,
        onProfileClick = onProfileClick,
        onHashtagClick = onHashtagClick,
        onMediaClick = onMediaClick,
        onWalletUnavailable = onWalletUnavailable,
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
    onPostReplyClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
    onWalletUnavailable: () -> Unit,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)

    val feedPagingItems = state.posts.collectAsLazyPagingItems()
    val feedListState = feedPagingItems.rememberLazyListStatePagingWorkaround()

    val bottomBarHeight = PrimalBottomBarHeightDp
    var bottomBarOffsetHeightPx by remember { mutableFloatStateOf(0f) }

    val focusMode by remember { derivedStateOf { bottomBarOffsetHeightPx < 0f } }

    val snackbarHostState = remember { SnackbarHostState() }

    ErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
    )

    PrimalDrawerScaffold(
        drawerState = drawerState,
        activeDestination = PrimalTopLevelDestination.Feed,
        onActiveDestinationClick = { uiScope.launch { feedListState.animateScrollToItem(0) } },
        onPrimaryDestinationChanged = onPrimaryDestinationChanged,
        onDrawerDestinationClick = onDrawerDestinationClick,
        badges = state.badges,
        bottomBarHeight = bottomBarHeight,
        onBottomBarOffsetChange = { bottomBarOffsetHeightPx = it },
        topBar = {
            PrimalTopAppBar(
                title = state.feedTitle,
                avatarCdnImage = state.activeAccountAvatarCdnImage,
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
            FeedNoteList(
                pagingItems = feedPagingItems,
                feedListState = feedListState,
                walletConnected = state.walletConnected,
                onPostClick = onPostClick,
                onProfileClick = onProfileClick,
                onPostReplyClick = onPostReplyClick,
                onZapClick = { post, zapAmount, zapDescription ->
                    eventPublisher(
                        FeedContract.UiEvent.ZapAction(
                            postId = post.postId,
                            postAuthorId = post.authorId,
                            zapAmount = zapAmount,
                            zapDescription = zapDescription,
                        ),
                    )
                },
                onPostLikeClick = {
                    eventPublisher(
                        FeedContract.UiEvent.PostLikeAction(
                            postId = it.postId,
                            postAuthorId = it.authorId,
                        ),
                    )
                },
                onRepostClick = {
                    eventPublisher(
                        FeedContract.UiEvent.RepostAction(
                            postId = it.postId,
                            postAuthorId = it.authorId,
                            postNostrEvent = it.rawNostrEventJson,
                        ),
                    )
                },
                onPostQuoteClick = {
                    onNewPostClick("\n\nnostr:${it.postId.hexToNoteHrp()}")
                },
                onHashtagClick = onHashtagClick,
                onWalletUnavailable = onWalletUnavailable,
                defaultZapAmount = state.defaultZapAmount,
                zapOptions = state.zapOptions,
                syncStats = state.syncStats,
                paddingValues = paddingValues,
                bottomBarHeightPx = with(LocalDensity.current) {
                    bottomBarHeight.roundToPx().toFloat()
                },
                bottomBarOffsetHeightPx = bottomBarOffsetHeightPx,
                onScrolledToTop = {
                    eventPublisher(FeedContract.UiEvent.FeedScrolledToTop)
                },
                onMuteClick = {
                    eventPublisher(FeedContract.UiEvent.MuteAction(it))
                },
                onMediaClick = onMediaClick,
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
                        .clip(CircleShape)
                        .background(color = AppTheme.colorScheme.primary, shape = CircleShape),
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                    containerColor = Color.Unspecified,
                    content = {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                            tint = Color.White,
                        )
                    },
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@Composable
private fun ErrorHandler(error: FeedError?, snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    LaunchedEffect(error ?: true) {
        val errorMessage = when (error) {
            is FeedError.InvalidZapRequest -> context.getString(
                R.string.post_action_invalid_zap_request,
            )
            is FeedError.MissingLightningAddress -> context.getString(
                R.string.post_action_missing_lightning_address,
            )
            is FeedError.FailedToPublishZapEvent -> context.getString(
                R.string.post_action_zap_failed,
            )
            is FeedError.FailedToPublishLikeEvent -> context.getString(
                R.string.post_action_like_failed,
            )
            is FeedError.FailedToPublishRepostEvent -> context.getString(
                R.string.post_action_repost_failed,
            )
            is FeedError.MissingRelaysConfiguration -> context.getString(
                R.string.app_missing_relays_config,
            )
            else -> null
        }

        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short,
            )
        }
    }
}

@Preview
@Composable
fun FeedScreenPreview() {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        FeedScreen(
            state = FeedContract.UiState(posts = flow { }),
            eventPublisher = {},
            onFeedsClick = {},
            onNewPostClick = {},
            onPostClick = {},
            onPostReplyClick = {},
            onProfileClick = {},
            onHashtagClick = {},
            onMediaClick = { _, _ -> },
            onWalletUnavailable = {},
            onPrimaryDestinationChanged = {},
            onDrawerDestinationClick = {},
        )
    }
}
