package net.primal.android.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.AvatarThumbnailsRow
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.icons.primaliconpack.Search
import net.primal.android.core.compose.isNotEmpty
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalDrawerScaffold
import net.primal.android.feeds.OldFeedsModalBottomSheet
import net.primal.android.feeds.ui.model.FeedUi
import net.primal.android.notes.FeedContract.UiState.FeedError
import net.primal.android.notes.feed.NoteFeedList
import net.primal.android.notes.feed.model.FeedPostsSyncStats
import net.primal.android.notes.feed.note.ConfirmFirstBookmarkAlertDialog
import net.primal.android.notes.feed.note.events.NoteCallbacks
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

@Deprecated("Use NoteFeedScreen")
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    onFeedClick: (directive: String) -> Unit,
    onNewPostClick: (content: TextFieldValue?) -> Unit,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
    onDrawerQrCodeClick: () -> Unit,
    onSearchClick: () -> Unit,
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
        onFeedClick = onFeedClick,
        onNewPostClick = onNewPostClick,
        noteCallbacks = noteCallbacks,
        onGoToWallet = onGoToWallet,
        onPrimaryDestinationChanged = onTopLevelDestinationChanged,
        onDrawerDestinationClick = onDrawerScreenClick,
        onDrawerQrCodeClick = onDrawerQrCodeClick,
        onSearchClick = onSearchClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedScreen(
    state: FeedContract.UiState,
    onFeedClick: (directive: String) -> Unit,
    eventPublisher: (FeedContract.UiEvent) -> Unit,
    onNewPostClick: (content: TextFieldValue?) -> Unit,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    onDrawerQrCodeClick: () -> Unit,
    onSearchClick: () -> Unit,
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
        topBar = { scrollBehavior ->
            FeedTopAppBar(
                avatarCdnImage = state.activeAccountAvatarCdnImage,
                title = state.feedTitle,
                feeds = state.feeds,
                onNavigationClick = { uiScope.launch { drawerState.open() } },
                onFeedChanged = { feed -> onFeedClick(feed.spec) },
                onSearchClick = onSearchClick,
                scrollBehavior = scrollBehavior,
            )
        },
        content = { paddingValues ->
            NoteFeedList(
                pagingItems = pagingItems,
                feedListState = feedListState,
                zappingState = state.zappingState,
                noteCallbacks = noteCallbacks,
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
                onGoToWallet = onGoToWallet,
                paddingValues = paddingValues,
                onScrolledToTop = { eventPublisher(FeedContract.UiEvent.FeedScrolledToTop) },
                onMuteClick = { eventPublisher(FeedContract.UiEvent.MuteAction(it)) },
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

@ExperimentalMaterial3Api
@Composable
private fun FeedTopAppBar(
    avatarCdnImage: CdnImage?,
    title: String,
    feeds: List<FeedUi>,
    onNavigationClick: () -> Unit,
    onFeedChanged: (FeedUi) -> Unit,
    onSearchClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    var feedPickerVisible by remember { mutableStateOf(false) }

    if (feedPickerVisible) {
        OldFeedsModalBottomSheet(
            title = stringResource(id = R.string.home_feeds_title),
            feeds = feeds,
            activeFeed = null,
            onDismissRequest = { feedPickerVisible = false },
            onFeedClick = { feed ->
                feedPickerVisible = false
                onFeedChanged(feed)
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        )
    }

    PrimalTopAppBar(
        title = title,
        titleTrailingIcon = Icons.Default.ExpandMore,
        avatarCdnImage = avatarCdnImage,
        navigationIcon = PrimalIcons.AvatarDefault,
        onNavigationIconClick = onNavigationClick,
        onTitleClick = { feedPickerVisible = true },
        actions = {
            AppBarIcon(
                icon = PrimalIcons.Search,
                onClick = onSearchClick,
                appBarIconContentDescription = stringResource(id = R.string.accessibility_search),
            )
        },
        scrollBehavior = scrollBehavior,
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
@Deprecated("Replace with SnackbarErrorHandler")
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
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        FeedScreen(
            state = FeedContract.UiState(posts = flow { }),
            onFeedClick = {},
            eventPublisher = {},
            onNewPostClick = {},
            noteCallbacks = NoteCallbacks(),
            onGoToWallet = {},
            onPrimaryDestinationChanged = {},
            onDrawerDestinationClick = {},
            onDrawerQrCodeClick = {},
            onSearchClick = {},
        )
    }
}
