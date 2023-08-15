package net.primal.android.explore.feed

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.feed.FeedPostList
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.UserFeedAdd
import net.primal.android.core.compose.icons.primaliconpack.UserFeedRemove
import net.primal.android.crypto.hexToNoteHrp
import net.primal.android.explore.feed.ExploreFeedContract.UiEvent.AddToUserFeeds
import net.primal.android.explore.feed.ExploreFeedContract.UiEvent.RemoveFromUserFeeds

@Composable
fun ExploreFeedScreen(
    viewModel: ExploreFeedViewModel,
    onClose: () -> Unit,
    onPostClick: (String) -> Unit,
    onPostQuoteClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onWalletUnavailable: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    ExploreFeedScreen(
        state = uiState.value,
        onClose = onClose,
        onPostClick = onPostClick,
        onProfileClick = onProfileClick,
        onPostQuoteClick = onPostQuoteClick,
        onHashtagClick = onHashtagClick,
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
    onPostQuoteClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onWalletUnavailable: () -> Unit,
    eventPublisher: (ExploreFeedContract.UiEvent) -> Unit,
) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
    val listState = rememberLazyListState()

    val uiScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val addedToUserFeedsMessage = stringResource(id = R.string.explore_feed_added_to_user_feeds)
    val removedFromUserFeedsMessage = stringResource(id = R.string.explore_feed_removed_from_user_feeds)

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
            FeedPostList(
                posts = state.posts,
                walletConnected = state.walletConnected,
                paddingValues = paddingValues,
                feedListState = listState,
                onPostClick = onPostClick,
                onProfileClick = onProfileClick,
                onPostReplyClick = {
                    onPostClick(it)
                },
                onZapClick = { post, zapAmount, zapDescription ->
                    eventPublisher(
                        ExploreFeedContract.UiEvent.ZapAction(
                            postId = post.postId,
                            postAuthorId = post.authorId,
                            zapAmount = zapAmount,
                            zapDescription = zapDescription,
                        )
                    )
                },
                onPostLikeClick = {
                    eventPublisher(
                        ExploreFeedContract.UiEvent.PostLikeAction(
                            postId = it.postId,
                            postAuthorId = it.authorId,
                        )
                    )
                },
                onRepostClick = {
                    eventPublisher(
                        ExploreFeedContract.UiEvent.RepostAction(
                            postId = it.postId,
                            postAuthorId = it.authorId,
                            postNostrEvent = it.rawNostrEventJson,
                        )
                    )
                },
                onPostQuoteClick = {
                    onPostQuoteClick("\n\nnostr:${it.postId.hexToNoteHrp()}")
                },
                onHashtagClick = onHashtagClick,
                onWalletUnavailable = onWalletUnavailable,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    )
}
