package net.primal.android.explore.feed.note

import androidx.compose.foundation.layout.padding
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
import net.primal.android.core.compose.InvisibleAppBarIcon
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.UserFeedAdd
import net.primal.android.core.compose.icons.primaliconpack.UserFeedRemove
import net.primal.android.explore.feed.note.ExploreNoteFeedContract.UiEvent.AddToUserFeeds
import net.primal.android.explore.feed.note.ExploreNoteFeedContract.UiEvent.RemoveFromUserFeeds
import net.primal.android.explore.feed.note.ExploreNoteFeedContract.UiState.ExploreFeedError
import net.primal.android.feeds.domain.isNotesBookmarkFeedSpec
import net.primal.android.notes.feed.MediaFeedGrid
import net.primal.android.notes.feed.NoteFeedList
import net.primal.android.notes.feed.note.showNoteErrorSnackbar
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks

@Composable
fun ExploreNoteFeedScreen(
    viewModel: ExploreNoteFeedViewModel,
    onClose: () -> Unit,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    ExploreNoteFeedScreen(
        state = uiState.value,
        onClose = onClose,
        noteCallbacks = noteCallbacks,
        onGoToWallet = onGoToWallet,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreNoteFeedScreen(
    state: ExploreNoteFeedContract.UiState,
    onClose: () -> Unit,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    eventPublisher: (ExploreNoteFeedContract.UiEvent) -> Unit,
) {
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    val feedPagingItems = state.notes.collectAsLazyPagingItems()
    val feedListState = feedPagingItems.rememberLazyListStatePagingWorkaround()

    val feedTitle = state.extractTitle()
    val snackbarHostState = remember { SnackbarHostState() }

    ErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
    )

    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            PrimalTopAppBar(
                title = feedTitle,
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                actions = {
                    if (state.canBeAddedInUserFeeds) {
                        val addedToUserFeedsMessage = stringResource(id = R.string.app_added_to_user_feeds)
                        val removedFromUserFeedsMessage = stringResource(id = R.string.app_removed_from_user_feeds)
                        AddRemoveUserFeedAppBarIcon(
                            existsInUserFeeds = state.existsInUserFeeds,
                            onRemoveFromUserFeedsClick = {
                                eventPublisher(RemoveFromUserFeeds)
                                uiScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = removedFromUserFeedsMessage,
                                        duration = SnackbarDuration.Short,
                                    )
                                }
                            },
                            onAddToUserFeedsClick = {
                                eventPublisher(AddToUserFeeds(title = feedTitle))
                                uiScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = addedToUserFeedsMessage,
                                        duration = SnackbarDuration.Short,
                                    )
                                }
                            },
                        )
                    } else {
                        InvisibleAppBarIcon()
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        content = { paddingValues ->
            when (state.renderType) {
                ExploreNoteFeedContract.RenderType.List -> {
                    NoteFeedList(
                        feedListState = feedListState,
                        pagingItems = feedPagingItems,
                        paddingValues = paddingValues,
                        noteCallbacks = noteCallbacks,
                        onGoToWallet = onGoToWallet,
                        noContentText = when {
                            state.feedSpec.isNotesBookmarkFeedSpec() -> stringResource(
                                id = R.string.bookmarks_no_content,
                            )
                            else -> stringResource(id = R.string.feed_no_content)
                        },
                        onNoteError = { noteError ->
                            uiScope.launch {
                                showNoteErrorSnackbar(
                                    context = context,
                                    error = noteError,
                                    snackbarHostState = snackbarHostState,
                                )
                            }
                        },
                    )
                }
                ExploreNoteFeedContract.RenderType.Grid -> {
                    MediaFeedGrid(
                        modifier = Modifier.padding(paddingValues),
                        feedSpec = state.feedSpec,
                        onNoteClick = { noteCallbacks.onNoteClick?.invoke(it) },
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@Composable
private fun ExploreNoteFeedContract.UiState.extractTitle() =
    when {
        // TODO Extract search title once api is implemented
//        feedSpec.isSearchFeed() -> feedSpec.removeSearchPrefix()
        feedSpec.isNotesBookmarkFeedSpec() -> stringResource(id = R.string.bookmarks_title)
        else -> stringResource(id = R.string.explore_fallback_title)
    }

@Composable
private fun AddRemoveUserFeedAppBarIcon(
    existsInUserFeeds: Boolean,
    onAddToUserFeedsClick: () -> Unit,
    onRemoveFromUserFeedsClick: () -> Unit,
) {
    AppBarIcon(
        icon = if (existsInUserFeeds) {
            PrimalIcons.UserFeedRemove
        } else {
            PrimalIcons.UserFeedAdd
        },
        appBarIconContentDescription = if (existsInUserFeeds) {
            stringResource(id = R.string.accessibility_remove_feed)
        } else {
            stringResource(id = R.string.accessibility_add_feed)
        },
        onClick = {
            if (existsInUserFeeds) {
                onRemoveFromUserFeedsClick()
            } else {
                onAddToUserFeedsClick()
            }
        },
    )
}

@Composable
@Deprecated("Replace with SnackbarErrorHandler")
private fun ErrorHandler(error: ExploreFeedError?, snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    LaunchedEffect(error ?: true) {
        val errorMessage = when (error) {
            is ExploreFeedError.FailedToAddToFeed -> context.getString(
                R.string.app_error_adding_to_feed,
            )

            is ExploreFeedError.FailedToRemoveFeed -> context.getString(
                R.string.app_error_removing_feed,
            )

            null -> return@LaunchedEffect
        }

        snackbarHostState.showSnackbar(
            message = errorMessage,
            duration = SnackbarDuration.Short,
        )
    }
}
