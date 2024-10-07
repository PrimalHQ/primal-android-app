package net.primal.android.explore.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.articles.feed.ArticleFeedList
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.errors.UiError
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.explore.feed.ExploreFeedContract.UiEvent.AddToUserFeeds
import net.primal.android.explore.feed.ExploreFeedContract.UiEvent.RemoveFromUserFeeds
import net.primal.android.explore.feed.ExploreFeedContract.UiState.ExploreFeedError
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.feeds.domain.extractTopicFromFeedSpec
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

    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { error ->
            when (error) {
                is ExploreFeedError.FailedToAddToFeed -> context.getString(
                    R.string.app_error_adding_to_feed,
                )

                is ExploreFeedError.FailedToRemoveFeed -> context.getString(
                    R.string.app_error_removing_feed,
                )
            }
        },
    )

    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            val addedToUserFeedsMessage = stringResource(id = R.string.app_added_to_user_feeds)
            val removedFromUserFeedsMessage = stringResource(id = R.string.app_removed_from_user_feeds)
            ExploreFeedTopAppBar(
                title = feedTitle,
                existsInUserFeeds = state.existsInUserFeeds,
                onClose = onClose,
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
                    eventPublisher(AddToUserFeeds(title = "", description = ""))
                    uiScope.launch {
                        snackbarHostState.showSnackbar(
                            message = addedToUserFeedsMessage,
                            duration = SnackbarDuration.Short,
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        content = { paddingValues ->
            when (state.feedSpecKind) {
                FeedSpecKind.Reads -> ExploreArticleFeed(
                    feedSpec = state.feedSpec,
                    onArticleClick = { noteCallbacks.onArticleClick?.invoke(it) },
                    contentPadding = paddingValues,
                )

                FeedSpecKind.Notes -> ExploreNoteFeed(
                    feedSpec = state.feedSpec,
                    renderType = state.renderType,
                    noteCallbacks = noteCallbacks,
                    contentPadding = paddingValues,
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
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@ExperimentalMaterial3Api
@Composable
private fun ExploreFeedTopAppBar(
    title: String,
    existsInUserFeeds: Boolean,
    onClose: () -> Unit,
    onAddToUserFeedsClick: () -> Unit,
    onRemoveFromUserFeedsClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    PrimalTopAppBar(
        title = title,
        navigationIcon = PrimalIcons.ArrowBack,
        onNavigationIconClick = onClose,
        navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
        actions = {
            AddRemoveUserFeedButton(
                existsInUserFeeds = existsInUserFeeds,
                onRemoveFromUserFeedsClick = onRemoveFromUserFeedsClick,
                onAddToUserFeedsClick = onAddToUserFeedsClick,
            )
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun ExploreNoteFeed(
    feedSpec: String,
    renderType: ExploreFeedContract.RenderType,
    contentPadding: PaddingValues,
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
                contentPadding = contentPadding,
                onUiError = onUiError,
            )
        }

        ExploreFeedContract.RenderType.Grid -> {
            MediaFeedGrid(
                feedSpec = feedSpec,
                contentPadding = contentPadding,
                onNoteClick = { noteCallbacks.onNoteClick?.invoke(it) },
            )
        }
    }
}

@Composable
private fun ExploreArticleFeed(
    feedSpec: String,
    contentPadding: PaddingValues,
    onArticleClick: (naddr: String) -> Unit,
    onUiError: ((UiError) -> Unit)? = null,
) {
    ArticleFeedList(
        feedSpec = feedSpec,
        contentPadding = contentPadding,
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
        else -> stringResource(id = R.string.explore_feed_fallback_title)
    }
}

@Composable
private fun AddRemoveUserFeedButton(
    existsInUserFeeds: Boolean,
    onAddToUserFeedsClick: () -> Unit,
    onRemoveFromUserFeedsClick: () -> Unit,
) {
    PrimalLoadingButton(
        modifier = Modifier.padding(end = 6.dp),
        text = if (existsInUserFeeds) {
            stringResource(R.string.explore_feed_remove_feed)
        } else {
            stringResource(R.string.explore_feed_save_feed)
        },
        height = 36.dp,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        contentPadding = PaddingValues(horizontal = 18.dp),
        onClick = {
            if (existsInUserFeeds) {
                onRemoveFromUserFeedsClick()
            } else {
                onAddToUserFeedsClick()
            }
        },
    )
}
