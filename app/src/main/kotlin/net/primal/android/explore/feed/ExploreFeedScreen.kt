package net.primal.android.explore.feed

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.feed.list.FeedNoteList
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.UserFeedAdd
import net.primal.android.core.compose.icons.primaliconpack.UserFeedRemove
import net.primal.android.crypto.hexToNoteHrp
import net.primal.android.explore.feed.ExploreFeedContract.UiEvent.AddToUserFeeds
import net.primal.android.explore.feed.ExploreFeedContract.UiEvent.RemoveFromUserFeeds
import net.primal.android.explore.feed.ExploreFeedContract.UiState.ExploreFeedError

@Composable
fun ExploreFeedScreen(
    viewModel: ExploreFeedViewModel,
    onClose: () -> Unit,
    onPostClick: (String) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onPostQuoteClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
    onWalletUnavailable: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    ExploreFeedScreen(
        state = uiState.value,
        onClose = onClose,
        onPostClick = onPostClick,
        onPostReplyClick = onPostReplyClick,
        onProfileClick = onProfileClick,
        onPostQuoteClick = onPostQuoteClick,
        onHashtagClick = onHashtagClick,
        onMediaClick = onMediaClick,
        onWalletUnavailable = onWalletUnavailable,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreFeedScreen(
    state: ExploreFeedContract.UiState,
    onClose: () -> Unit,
    onPostClick: (String) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onPostQuoteClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
    onWalletUnavailable: () -> Unit,
    eventPublisher: (ExploreFeedContract.UiEvent) -> Unit,
) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)

    val feedPagingItems = state.posts.collectAsLazyPagingItems()
    val feedListState = feedPagingItems.rememberLazyListStatePagingWorkaround()

    val uiScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val addedToUserFeedsMessage = stringResource(id = R.string.app_added_to_user_feeds)
    val removedFromUserFeedsMessage = stringResource(id = R.string.app_removed_from_user_feeds)

    ErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            PrimalTopAppBar(
                title = state.title,
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
                actions = {
                    AppBarIcon(
                        icon = if (state.existsInUserFeeds) {
                            PrimalIcons.UserFeedRemove
                        } else {
                            PrimalIcons.UserFeedAdd
                        },
                        onClick = {
                            if (state.existsInUserFeeds) {
                                eventPublisher(RemoveFromUserFeeds)
                                uiScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = removedFromUserFeedsMessage,
                                        duration = SnackbarDuration.Short,
                                    )
                                }
                            } else {
                                eventPublisher(AddToUserFeeds)
                                uiScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = addedToUserFeedsMessage,
                                        duration = SnackbarDuration.Short,
                                    )
                                }
                            }
                        },
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
        content = { paddingValues ->
            FeedNoteList(
                feedListState = feedListState,
                pagingItems = feedPagingItems,
                walletConnected = state.walletConnected,
                paddingValues = paddingValues,
                onPostClick = onPostClick,
                onProfileClick = onProfileClick,
                onPostReplyClick = onPostReplyClick,
                onZapClick = { post, zapAmount, zapDescription ->
                    eventPublisher(
                        ExploreFeedContract.UiEvent.ZapAction(
                            postId = post.postId,
                            postAuthorId = post.authorId,
                            zapAmount = zapAmount,
                            zapDescription = zapDescription,
                        ),
                    )
                },
                onPostLikeClick = {
                    eventPublisher(
                        ExploreFeedContract.UiEvent.PostLikeAction(
                            postId = it.postId,
                            postAuthorId = it.authorId,
                        ),
                    )
                },
                onRepostClick = {
                    eventPublisher(
                        ExploreFeedContract.UiEvent.RepostAction(
                            postId = it.postId,
                            postAuthorId = it.authorId,
                            postNostrEvent = it.rawNostrEventJson,
                        ),
                    )
                },
                onPostQuoteClick = {
                    onPostQuoteClick("\n\nnostr:${it.postId.hexToNoteHrp()}")
                },
                onHashtagClick = onHashtagClick,
                onWalletUnavailable = onWalletUnavailable,
                defaultZapAmount = state.defaultZapAmount,
                zapOptions = state.zapOptions,
                onMuteClick = {
                    eventPublisher(ExploreFeedContract.UiEvent.MuteAction(profileId = it))
                },
                onMediaClick = onMediaClick,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@Composable
private fun ErrorHandler(error: ExploreFeedError?, snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    LaunchedEffect(error ?: true) {
        val errorMessage = when (error) {
            is ExploreFeedError.InvalidZapRequest -> context.getString(
                R.string.post_action_invalid_zap_request,
            )
            is ExploreFeedError.MissingLightningAddress -> context.getString(
                R.string.post_action_missing_lightning_address,
            )
            is ExploreFeedError.FailedToPublishZapEvent -> context.getString(
                R.string.post_action_zap_failed,
            )
            is ExploreFeedError.FailedToPublishLikeEvent -> context.getString(
                R.string.post_action_like_failed,
            )
            is ExploreFeedError.FailedToPublishRepostEvent -> context.getString(
                R.string.post_action_repost_failed,
            )
            is ExploreFeedError.MissingRelaysConfiguration -> context.getString(
                R.string.app_missing_relays_config,
            )
            is ExploreFeedError.FailedToAddToFeed -> context.getString(
                R.string.app_error_adding_to_feed,
            )
            is ExploreFeedError.FailedToRemoveFeed -> context.getString(
                R.string.app_error_removing_feed,
            )
            is ExploreFeedError.FailedToMuteUser -> context.getString(
                R.string.app_error_muting_user,
            )
            null -> return@LaunchedEffect
        }

        snackbarHostState.showSnackbar(
            message = errorMessage,
            duration = SnackbarDuration.Short,
        )
    }
}
