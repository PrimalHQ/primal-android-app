package net.primal.android.explore.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.articles.feed.ArticleFeedList
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.InvisibleAppBarIcon
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.UserFeedAdd
import net.primal.android.core.compose.icons.primaliconpack.UserFeedRemove
import net.primal.android.core.errors.UiError
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.explore.feed.ExploreFeedContract.UiEvent.AddToUserFeeds
import net.primal.android.explore.feed.ExploreFeedContract.UiEvent.RemoveFromUserFeeds
import net.primal.android.explore.feed.ExploreFeedContract.UiState.ExploreFeedError
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.feeds.domain.extractTopicFromFeedSpec
import net.primal.android.feeds.domain.isNotesBookmarkFeedSpec
import net.primal.android.feeds.domain.isSearchFeedSpec
import net.primal.android.notes.feed.MediaFeedGrid
import net.primal.android.notes.feed.NoteFeedList
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.theme.AppTheme

@Composable
fun ExploreFeedScreen(
    viewModel: ExploreFeedViewModel,
    onClose: () -> Unit,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    ExploreFeedScreen(
        state = uiState.value,
        onClose = onClose,
        noteCallbacks = noteCallbacks,
        onGoToWallet = onGoToWallet,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreFeedScreen(
    state: ExploreFeedContract.UiState,
    onClose: () -> Unit,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    eventPublisher: (ExploreFeedContract.UiEvent) -> Unit,
) {
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()

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
            Box(modifier = Modifier.padding(paddingValues)) {
                when (state.feedSpecKind) {
                    FeedSpecKind.Reads -> ExploreArticleFeed(
                        feedSpec = state.feedSpec,
                        onArticleClick = { noteCallbacks.onArticleClick?.invoke(it) },
                    )

                    FeedSpecKind.Notes -> ExploreNoteFeed(
                        feedSpec = state.feedSpec,
                        renderType = state.renderType,
                        noteCallbacks = noteCallbacks,
                        onGoToWallet = onGoToWallet,
                        onUiError = { uiError ->
                            uiScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = uiError.resolveUiErrorMessage(context),
                                    duration = SnackbarDuration.Short,
                                )
                            }
                        },
                    )

                    null -> UnknownFeedSpecKind(feedSpec = state.feedSpec)
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@Composable
private fun ExploreNoteFeed(
    feedSpec: String,
    renderType: ExploreFeedContract.RenderType,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    onUiError: ((UiError) -> Unit)? = null,
) {
    when (renderType) {
        ExploreFeedContract.RenderType.List -> {
            NoteFeedList(
                feedSpec = feedSpec,
                noteCallbacks = noteCallbacks,
                onGoToWallet = onGoToWallet,
                noContentText = when {
                    feedSpec.isNotesBookmarkFeedSpec() -> stringResource(
                        id = R.string.bookmarks_no_content,
                    )
                    else -> stringResource(id = R.string.feed_no_content)
                },
                onUiError = onUiError,
            )
        }

        ExploreFeedContract.RenderType.Grid -> {
            MediaFeedGrid(
                feedSpec = feedSpec,
                onNoteClick = { noteCallbacks.onNoteClick?.invoke(it) },
            )
        }
    }
}

@Composable
private fun ExploreArticleFeed(
    feedSpec: String,
    onArticleClick: (naddr: String) -> Unit,
    onUiError: ((UiError) -> Unit)? = null,
) {
    ArticleFeedList(
        feedSpec = feedSpec,
        onArticleClick = onArticleClick,
        onUiError = onUiError,
    )
}

@Composable
fun UnknownFeedSpecKind(feedSpec: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Unknown feed spec kind.\n$feedSpec",
            style = AppTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun ExploreFeedContract.UiState.extractTitle(): String {
    val topic = feedSpec.extractTopicFromFeedSpec()
    return when {
        topic != null -> topic
        feedSpec.isSearchFeedSpec() -> stringResource(R.string.explore_feed_search_results)
        feedSpec.isNotesBookmarkFeedSpec() -> stringResource(id = R.string.bookmarks_title)
        else -> stringResource(id = R.string.explore_feed_fallback_title)
    }
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
