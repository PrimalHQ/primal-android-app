package net.primal.android.notes.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import net.primal.android.R
import net.primal.android.core.compose.GridLoadingPlaceholder
import net.primal.android.core.compose.ListLoading
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.isEmpty
import net.primal.android.notes.feed.model.FeedPostUi
import timber.log.Timber

@Composable
fun MediaFeedGrid(
    feedSpec: String,
    onNoteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    noContentVerticalArrangement: Arrangement.Vertical = Arrangement.Center,
    noContentPaddingValues: PaddingValues = PaddingValues(all = 0.dp),
) {
    val viewModel = hiltViewModel<MediaFeedViewModel, MediaFeedViewModel.Factory>(
        key = feedSpec,
        creationCallback = { factory -> factory.create(feedSpec = feedSpec) },
    )

    val uiState = viewModel.state.collectAsState()

    MediaFeedGrid(
        state = uiState.value,
        onNoteClick = onNoteClick,
        gridState = gridState,
        modifier = modifier,
        contentPadding = contentPadding,
        noContentVerticalArrangement = noContentVerticalArrangement,
        noContentPaddingValues = noContentPaddingValues,
    )
}

@Composable
private fun MediaFeedGrid(
    modifier: Modifier = Modifier,
    state: MediaFeedContract.UiState,
    onNoteClick: (String) -> Unit,
    gridState: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    noContentVerticalArrangement: Arrangement.Vertical = Arrangement.Center,
    noContentPaddingValues: PaddingValues = PaddingValues(all = 0.dp),
) {
    val pagingItems = state.notes.collectAsLazyPagingItems()

    if (pagingItems.isEmpty()) {
        EmptyItemsContent(
            pagingItems = pagingItems,
            noContentVerticalArrangement = noContentVerticalArrangement,
            noContentPaddingValues = noContentPaddingValues,
        )
    } else {
        BoxWithConstraints {
            val itemWidth = maxWidth / 3
            LazyVerticalGrid(
                modifier = modifier,
                columns = GridCells.Fixed(count = 3),
                state = gridState,
                contentPadding = contentPadding,
            ) {
                items(
                    count = pagingItems.itemCount,
                    key = pagingItems.itemKey(key = { "${it.postId}${it.repostId}" }),
                    contentType = pagingItems.itemContentType(),
                ) { index ->
                    val item = pagingItems[index]

                    when {
                        item != null ->
                            MediaGridItem(
                                modifier = Modifier
                                    .clickable {
                                        onNoteClick(item.postId)
                                    }
                                    .padding(1.dp)
                                    .size(size = itemWidth),
                                item = item,
                                maxWidthPx = itemWidth.value.toInt(),
                            )
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyItemsContent(
    pagingItems: LazyPagingItems<FeedPostUi>,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    noContentVerticalArrangement: Arrangement.Vertical = Arrangement.Center,
    noContentPaddingValues: PaddingValues = PaddingValues(all = 0.dp),
) {
    when (val refreshLoadState = pagingItems.loadState.refresh) {
        LoadState.Loading -> {
            GridLoadingPlaceholder(
                modifier = Modifier.fillMaxSize(),
                contentPaddingValues = paddingValues,
            )
        }

        is LoadState.NotLoading -> {
            ListNoContent(
                modifier = Modifier.fillMaxSize(),
                noContentText = stringResource(id = R.string.feed_no_content),
                onRefresh = { pagingItems.refresh() },
                verticalArrangement = noContentVerticalArrangement,
                contentPadding = noContentPaddingValues,
            )
        }

        is LoadState.Error -> {
            val error = refreshLoadState.error
            Timber.w(error)
            ListNoContent(
                modifier = Modifier.fillMaxSize(),
                noContentText = stringResource(id = R.string.feed_error_loading),
                onRefresh = { pagingItems.refresh() },
                verticalArrangement = noContentVerticalArrangement,
                contentPadding = noContentPaddingValues,
            )
        }
    }
}
