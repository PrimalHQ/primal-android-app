package net.primal.android.notes.feed.list

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import io.github.aakira.napier.Napier
import net.primal.android.BuildConfig
import net.primal.android.R
import net.primal.android.core.compose.ListLoadingError
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PremiumFeedPaywall
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.heightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.compose.isNotEmpty
import net.primal.android.core.compose.zaps.FeedNoteTopZapsSection
import net.primal.android.core.errors.UiError
import net.primal.android.nostr.mappers.asFeedPostUi
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.StreamPillUi
import net.primal.android.notes.feed.note.FeedNoteCard
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.stream.player.LocalStreamState
import net.primal.domain.nostr.ReactionType

internal const val FEED_NESTED_NOTES_CUT_OFF_LIMIT = 2
internal const val STREAM_PILLS_ROW_KEY = "streamPillsRow"

@Composable
fun NoteFeedLazyColumn(
    modifier: Modifier = Modifier,
    pagingItems: LazyPagingItems<FeedPostUi>,
    streamPills: List<StreamPillUi>,
    listState: LazyListState,
    showPaywall: Boolean,
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
    val streamState = LocalStreamState.current
    var firstVisibleVideoPlayingIndex by rememberFirstVisibleVideoPlayingItemIndex(
        listState = listState,
        hasVideo = { index ->
            if (index < 0 || index >= pagingItems.itemCount) {
                false
            } else {
                hasAutoPlayVideo(pagingItems.peek(index))
            }
        },
    )
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

        item(key = STREAM_PILLS_ROW_KEY) {
            StreamPillsRow(
                streamPills = streamPills,
                onClick = { streamState.start(it) },
                onProfileClick = { noteCallbacks.onProfileClick?.invoke(it) },
            )
        }

        if (BuildConfig.FEATURE_PRIMAL_CRASH_REPORTER) {
            when (val prependMediatorLoadState = pagingItems.loadState.mediator?.prepend) {
                is LoadState.Error -> {
                    item(contentType = "PrependError") {
                        val error = prependMediatorLoadState.error
                        Napier.w(throwable = error) { "Error loading prev page" }
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
                        nestingCutOffLimit = FEED_NESTED_NOTES_CUT_OFF_LIMIT,
                        showReplyTo = showReplyTo,
                        couldAutoPlay = index == firstVisibleVideoPlayingIndex,
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
                                        {
                                            noteCallbacks.onEventReactionsClick
                                                .invoke(item.postId, ReactionType.ZAPS, null)
                                        }
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
                    Napier.w(throwable = error) { "Error loading feed" }
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
                    Napier.w(throwable = error) { "Error loading next page" }
                    ListLoadingError(
                        text = stringResource(R.string.app_error_loading_next_page) + "\n${error.message}",
                    )
                }
            }

            else -> Unit
        }

        if (pagingItems.isNotEmpty() && showPaywall) {
            item(contentType = "Paywall") {
                PremiumFeedPaywall(
                    onClick = {
                        noteCallbacks.onGetPrimalPremiumClick?.invoke()
                    },
                )
            }
        }

        if (pagingItems.isNotEmpty()) {
            item(contentType = "Footer") {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

private fun hasAutoPlayVideo(post: FeedPostUi?): Boolean =
    post != null && hasAutoPlayVideoRecursive(post, mutableSetOf())

private fun hasAutoPlayVideoRecursive(post: FeedPostUi, visitedIds: MutableSet<String>): Boolean {
    var hasVideo = false

    if (visitedIds.add(post.postId)) {
        val attachmentCount = post.uris.count { uri ->
            uri.mimeType?.let { it.startsWith("video") || it.startsWith("image") } == true
        }
        if (attachmentCount <= 1) {
            hasVideo = post.uris.any { it.mimeType?.startsWith("video") == true }

            if (!hasVideo) {
                hasVideo = post.nostrUris.any { nostrUri ->
                    nostrUri.referencedNote
                        ?.asFeedPostUi()
                        ?.let { hasAutoPlayVideoRecursive(it, visitedIds) }
                        ?: false
                }
            }
        }
    }

    return hasVideo
}
