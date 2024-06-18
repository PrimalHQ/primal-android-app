package net.primal.android.discuss.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.LocalContentDisplaySettings
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.AvatarThumbnailsRow
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.feed.list.FeedNoteList
import net.primal.android.core.compose.feed.model.FeedPostsSyncStats
import net.primal.android.core.compose.feed.note.ConfirmFirstBookmarkAlertDialog
import net.primal.android.core.compose.feed.note.events.InvoicePayClickEvent
import net.primal.android.core.compose.feed.note.events.MediaClickEvent
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.icons.primaliconpack.FeedPicker
import net.primal.android.core.compose.isNotEmpty
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.crypto.hexToNoteHrp
import net.primal.android.discuss.feed.FeedContract.UiState.FeedError
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalDrawerScaffold
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    onFeedsClick: () -> Unit,
    onNewPostClick: (content: TextFieldValue?) -> Unit,
    onPostClick: (String) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (MediaClickEvent) -> Unit,
    onPayInvoiceClick: ((InvoicePayClickEvent) -> Unit)? = null,
    onGoToWallet: () -> Unit,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
    onDrawerQrCodeClick: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    DisposableLifecycleObserverEffect(viewModel) {
        when (it) {
            Lifecycle.Event.ON_START -> {
                viewModel.setEvent(FeedContract.UiEvent.RequestUserDataUpdate)
                viewModel.setEvent(FeedContract.UiEvent.StartPolling)
            }

            Lifecycle.Event.ON_STOP -> {
                viewModel.setEvent(FeedContract.UiEvent.StopPolling)
            }

            else -> Unit
        }
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
        onPayInvoiceClick = onPayInvoiceClick,
        onGoToWallet = onGoToWallet,
        onPrimaryDestinationChanged = onTopLevelDestinationChanged,
        onDrawerDestinationClick = onDrawerScreenClick,
        onDrawerQrCodeClick = onDrawerQrCodeClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    state: FeedContract.UiState,
    eventPublisher: (FeedContract.UiEvent) -> Unit,
    onFeedsClick: () -> Unit,
    onNewPostClick: (content: TextFieldValue?) -> Unit,
    onPostClick: (String) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (MediaClickEvent) -> Unit,
    onPayInvoiceClick: ((InvoicePayClickEvent) -> Unit)? = null,
    onGoToWallet: () -> Unit,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    onDrawerQrCodeClick: () -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)

    val pagingItems = state.posts.collectAsLazyPagingItems()
    val feedListState = pagingItems.rememberLazyListStatePagingWorkaround()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedListState, pagingItems) {
        withContext(Dispatchers.IO) {
            snapshotFlow { feedListState.firstVisibleItemIndex to pagingItems.itemCount }
                .distinctUntilChanged()
                .filter { (_, size) -> size > 0 }
                .collect { (index, _) ->
                    val firstVisibleNote = pagingItems.peek(index)
                    if (firstVisibleNote != null) {
                        eventPublisher(
                            FeedContract.UiEvent.UpdateCurrentTopVisibleNote(
                                noteId = firstVisibleNote.postId,
                                repostId = firstVisibleNote.repostId,
                            ),
                        )
                    }
                }
        }
    }

    if (state.confirmBookmarkingNoteId != null) {
        ConfirmFirstBookmarkAlertDialog(
            onBookmarkConfirmed = {
                eventPublisher(
                    FeedContract.UiEvent.BookmarkAction(
                        noteId = state.confirmBookmarkingNoteId,
                        forceUpdate = true,
                    ),
                )
            },
            onClose = {
                eventPublisher(FeedContract.UiEvent.DismissBookmarkConfirmation)
            },
        )
    }

    ErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
    )

    PrimalDrawerScaffold(
        drawerState = drawerState,
        activeDestination = PrimalTopLevelDestination.Home,
        onActiveDestinationClick = { uiScope.launch { feedListState.animateScrollToItem(0) } },
        onPrimaryDestinationChanged = onPrimaryDestinationChanged,
        onDrawerDestinationClick = onDrawerDestinationClick,
        onDrawerQrCodeClick = onDrawerQrCodeClick,
        badges = state.badges,
        focusModeEnabled = LocalContentDisplaySettings.current.focusModeEnabled && pagingItems.isNotEmpty(),
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
                        appBarIconContentDescription = stringResource(id = R.string.accessibility_feed_picker),
                    )
                },
                scrollBehavior = it,
            )
        },
        content = { paddingValues ->
            FeedNoteList(
                pagingItems = pagingItems,
                feedListState = feedListState,
                zappingState = state.zappingState,
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
                onPostQuoteClick = { onNewPostClick(TextFieldValue(text = "\n\nnostr:${it.postId.hexToNoteHrp()}")) },
                onHashtagClick = onHashtagClick,
                onGoToWallet = onGoToWallet,
                paddingValues = paddingValues,
                onScrolledToTop = { eventPublisher(FeedContract.UiEvent.FeedScrolledToTop) },
                onMuteClick = { eventPublisher(FeedContract.UiEvent.MuteAction(it)) },
                onMediaClick = onMediaClick,
                onPayInvoiceClick = onPayInvoiceClick,
                onBookmarkClick = { eventPublisher(FeedContract.UiEvent.BookmarkAction(noteId = it)) },
                onReportContentClick = { type, profileId, noteId ->
                    eventPublisher(
                        FeedContract.UiEvent.ReportAbuse(
                            reportType = type,
                            profileId = profileId,
                            noteId = noteId,
                        ),
                    )
                },
            )
        },
        floatingNewDataHost = {
            if (state.syncStats.latestNoteIds.isNotEmpty() && pagingItems.isNotEmpty()) {
                var doneDelaying by remember { mutableStateOf(false) }
                LaunchedEffect(true) {
                    delay(0.21.seconds)
                    doneDelaying = true
                }
                if (doneDelaying) {
                    NewPostsButton(
                        syncStats = state.syncStats,
                        onClick = {
                            eventPublisher(FeedContract.UiEvent.ShowLatestNotes)
                        },
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNewPostClick(null) },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(color = AppTheme.colorScheme.primary, shape = CircleShape),
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                containerColor = Color.Unspecified,
                content = {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(id = R.string.accessibility_new_post),
                        tint = Color.White,
                    )
                },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@Composable
private fun NewPostsButton(syncStats: FeedPostsSyncStats, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(40.dp)
            .background(
                color = AppTheme.colorScheme.primary,
                shape = AppTheme.shapes.extraLarge,
            )
            .padding(horizontal = 2.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarThumbnailsRow(
            modifier = Modifier.padding(start = 6.dp),
            avatarCdnImages = syncStats.latestAvatarCdnImages,
            onClick = { onClick() },
        )

        Text(
            modifier = Modifier
                .padding(start = 12.dp, end = 16.dp)
                .wrapContentHeight(),
            text = stringResource(id = R.string.feed_new_posts_notice_general),
            style = AppTheme.typography.bodySmall,
            color = Color.White,
        )
    }
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
            onMediaClick = {},
            onGoToWallet = {},
            onPrimaryDestinationChanged = {},
            onDrawerDestinationClick = {},
            onDrawerQrCodeClick = {},
        )
    }
}
