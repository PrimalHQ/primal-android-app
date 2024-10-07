package net.primal.android.bookmarks.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.articles.feed.ArticleFeedList
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.errors.UiError
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.notes.feed.NoteFeedList
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.theme.AppTheme

@Composable
fun BookmarksScreen(
    viewModel: BookmarksViewModel,
    onClose: () -> Unit,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    BookmarksScreen(
        state = uiState.value,
        eventPublisher = viewModel::setEvent,
        onClose = onClose,
        noteCallbacks = noteCallbacks,
        onGoToWallet = onGoToWallet,
    )
}

@Composable
private fun BookmarksScreen(
    state: BookmarksContract.UiState,
    eventPublisher: (BookmarksContract.UiEvent) -> Unit,
    onClose: () -> Unit,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            BookmarksTopAppBar(
                feedSpecKind = state.feedSpecKind,
                onFeedSpecKindChanged = {
                    eventPublisher(BookmarksContract.UiEvent.ChangeFeedSpecKind(it))
                },
                onClose = onClose,
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            )
        },
    ) { paddingValues ->
        when (state.feedSpecKind) {
            FeedSpecKind.Reads -> {
                ArticleFeedList(
                    contentPadding = paddingValues,
                    feedSpec = state.feedSpec,
                    onArticleClick = { naddr -> noteCallbacks.onArticleClick?.invoke(naddr) },
                    pullToRefreshEnabled = false,
                    onUiError = { uiError: UiError ->
                        uiScope.launch {
                            snackbarHostState.showSnackbar(
                                message = uiError.resolveUiErrorMessage(context),
                                duration = SnackbarDuration.Short,
                            )
                        }
                    },
                )
            }

            FeedSpecKind.Notes -> {
                NoteFeedList(
                    contentPadding = paddingValues,
                    feedSpec = state.feedSpec,
                    noteCallbacks = noteCallbacks,
                    onGoToWallet = onGoToWallet,
                    pollingEnabled = false,
                    pullToRefreshEnabled = false,
                    onUiError = { uiError ->
                        uiScope.launch {
                            snackbarHostState.showSnackbar(
                                message = uiError.resolveUiErrorMessage(context),
                                duration = SnackbarDuration.Short,
                            )
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookmarksTopAppBar(
    feedSpecKind: FeedSpecKind,
    onFeedSpecKindChanged: (FeedSpecKind) -> Unit,
    onClose: () -> Unit,
) {
    var bookmarksPickerVisibility by remember { mutableStateOf(false) }

    if (bookmarksPickerVisibility) {
        BookmarksBottomSheetPicker(
            onDismissRequest = { bookmarksPickerVisibility = false },
            onFeedSpecKindChanged = onFeedSpecKindChanged,
        )
    }

    PrimalTopAppBar(
        title = when (feedSpecKind) {
            FeedSpecKind.Notes -> stringResource(id = R.string.bookmarks_title_notes)
            FeedSpecKind.Reads -> stringResource(id = R.string.bookmarks_title_reads)
        },
        titleTrailingIcon = Icons.Default.ExpandMore,
        onTitleClick = { bookmarksPickerVisibility = true },
        navigationIcon = PrimalIcons.ArrowBack,
        onNavigationIconClick = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookmarksBottomSheetPicker(
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    onFeedSpecKindChanged: (FeedSpecKind) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val handleCloseAndChange: (FeedSpecKind) -> Unit = { feedSpecKind ->
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismissRequest()
            }
            onFeedSpecKindChanged(feedSpecKind)
        }
    }

    ModalBottomSheet(
        tonalElevation = 0.dp,
        containerColor = AppTheme.colorScheme.background,
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
    ) {
        BookmarksBottomSheetListItem(
            text = stringResource(id = R.string.bookmarks_bottom_sheet_notes),
            onClick = { handleCloseAndChange(FeedSpecKind.Notes) },
        )
        BookmarksBottomSheetListItem(
            text = stringResource(id = R.string.bookmarks_bottom_sheet_reads),
            onClick = { handleCloseAndChange(FeedSpecKind.Reads) },
        )
    }
}

@Composable
private fun BookmarksBottomSheetListItem(text: String, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        headlineContent = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = text,
            )
        },
    )
}
