package net.primal.android.notes.feed.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import net.primal.android.BuildConfig
import net.primal.android.R
import net.primal.android.core.compose.ListLoadingError
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.heightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.compose.isNotEmpty
import net.primal.android.core.compose.zaps.FeedNoteTopZapsSection
import net.primal.android.core.errors.UiError
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.note.FeedNoteCard
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import timber.log.Timber

@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@Composable
fun NoteFeedLazyColumn(
    modifier: Modifier = Modifier,
    pagingItems: LazyPagingItems<FeedPostUi>,
    listState: LazyListState,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    showTopZaps: Boolean = false,
    shouldShowLoadingState: Boolean = true,
    shouldShowNoContentState: Boolean = true,
    showReplyTo: Boolean = true,
    noContentVerticalArrangement: Arrangement.Vertical = Arrangement.Center,
    noContentPaddingValues: PaddingValues = PaddingValues(all = 0.dp),
    noContentText: String = stringResource(id = R.string.feed_no_content),
    contentPadding: PaddingValues = PaddingValues(all = 0.dp),
    header: @Composable (LazyItemScope.() -> Unit)? = null,
    stickyHeader: @Composable (LazyItemScope.() -> Unit)? = null,
    onUiError: ((UiError) -> Unit)? = null,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        state = listState,
    ) {
        if (stickyHeader != null) {
            stickyHeader {
                stickyHeader()
            }
        }

        if (header != null) {
            item {
                header()
            }
        }

        if (BuildConfig.FEATURE_PRIMAL_CRASH_REPORTER) {
            when (val prependMediatorLoadState = pagingItems.loadState.mediator?.prepend) {
                is LoadState.Error -> {
                    item(contentType = "PrependError") {
                        val error = prependMediatorLoadState.error
                        Timber.w(error)
                        ListLoadingError(
                            text = stringResource(R.string.app_error_loading_prev_page) + "\n${error.message}",
                        )
                    }
                }

                else -> Unit
            }
        }

        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey(key = { "${it.postId}${it.repostId}" }),
            contentType = pagingItems.itemContentType(),
        ) { index ->
            val item = pagingItems[index]

            when {
                item != null -> Column {
                    FeedNoteCard(
                        data = item,
                        shape = RectangleShape,
                        cardPadding = PaddingValues(all = 0.dp),
                        fullWidthContent = true,
                        enableTweetsMode = true,
                        showReplyTo = showReplyTo,
                        noteCallbacks = noteCallbacks,
                        onGoToWallet = onGoToWallet,
                        onUiError = onUiError,
                        contentFooter = {
                            if (showTopZaps && item.eventZaps.isNotEmpty()) {
                                FeedNoteTopZapsSection(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                        .padding(top = 4.dp, end = 2.dp),
                                    zaps = item.eventZaps,
                                    onClick = if (noteCallbacks.onEventReactionsClick != null) {
                                        { noteCallbacks.onEventReactionsClick.invoke(item.postId) }
                                    } else {
                                        null
                                    },
                                )
                            }
                        },
                    )

                    PrimalDivider()
                }

                else -> {}
            }
        }

        if (pagingItems.isEmpty()) {
            when (val refreshLoadState = pagingItems.loadState.refresh) {
                LoadState.Loading -> {
                    if (shouldShowLoadingState) {
                        heightAdjustableLoadingLazyListPlaceholder()
                    }
                }

                is LoadState.NotLoading -> {
                    if (shouldShowNoContentState && pagingItems.loadState.isIdle) {
                        item(contentType = "NoContent") {
                            ListNoContent(
                                modifier = Modifier.fillParentMaxSize(),
                                noContentText = noContentText,
                                onRefresh = { pagingItems.refresh() },
                                verticalArrangement = noContentVerticalArrangement,
                                contentPadding = noContentPaddingValues,
                            )
                        }
                    }
                }

                is LoadState.Error -> {
                    val error = refreshLoadState.error
                    Timber.w(error)
                    item(contentType = "RefreshError") {
                        ListNoContent(
                            modifier = Modifier.fillParentMaxSize(),
                            noContentText = stringResource(id = R.string.feed_error_loading),
                            onRefresh = { pagingItems.refresh() },
                            verticalArrangement = noContentVerticalArrangement,
                            contentPadding = noContentPaddingValues,
                        )
                    }
                }
            }
        }

        when (val appendMediatorLoadState = pagingItems.loadState.mediator?.append) {
            LoadState.Loading -> {
                heightAdjustableLoadingLazyListPlaceholder(
                    contentType = { "LoadingPrepend" },
                    repeat = 1,
                )
            }

            is LoadState.Error -> if (BuildConfig.FEATURE_PRIMAL_CRASH_REPORTER) {
                item(contentType = "AppendError") {
                    val error = appendMediatorLoadState.error
                    Timber.w(error)
                    ListLoadingError(
                        text = stringResource(R.string.app_error_loading_next_page) + "\n${error.message}",
                    )
                }
            }

            else -> Unit
        }

        if (pagingItems.isNotEmpty()) {
            item(contentType = "Footer") {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}
