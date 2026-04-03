package net.primal.android.main.home

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.FeedsErrorColumn
import net.primal.android.core.compose.HeightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.icons.primaliconpack.Search
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.feeds.list.FeedsBottomSheet
import net.primal.android.feeds.list.ui.model.FeedUi
import net.primal.android.main.home.HomeFeedContract.UiEvent
import net.primal.android.notes.feed.list.NoteFeedList
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.domain.feeds.FeedSpecKind
import net.primal.domain.links.CdnImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeFeedContent(
    state: HomeFeedContract.UiState,
    noteCallbacks: NoteCallbacks,
    eventPublisher: (UiEvent) -> Unit,
    onActiveFeedChanged: (FeedUi?) -> Unit,
    topAppBarCollapsedFraction: Float,
    shouldAnimateScrollToTop: MutableState<Boolean>,
    scrollToFeed: MutableState<FeedUi?> = remember { mutableStateOf(null) },
    snackbarHostState: SnackbarHostState,
    paddingValues: PaddingValues,
    onGoToWallet: () -> Unit,
) {
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { state.feeds.size })

    var activeFeed by remember { mutableStateOf<FeedUi?>(null) }

    val pollingStates by remember(activeFeed, state.feeds) {
        derivedStateOf {
            state.feeds.associateWith { feed ->
                activeFeed?.spec == feed.spec
            }
        }
    }

    LaunchedEffect(pagerState, state.feeds) {
        snapshotFlow { pagerState.currentPage }
            .collect { index ->
                if (state.feeds.isNotEmpty()) {
                    val feed = state.feeds[index]
                    activeFeed = feed
                    onActiveFeedChanged(feed)
                }
            }
    }

    LaunchedEffect(scrollToFeed.value) {
        val feed = scrollToFeed.value ?: return@LaunchedEffect
        val pageIndex = state.feeds.indexOf(feed)
        if (pageIndex >= 0) {
            pagerState.scrollToPage(page = pageIndex)
        }
        scrollToFeed.value = null
    }

    if (state.feeds.isNotEmpty()) {
        HorizontalPager(
            state = pagerState,
            key = { index -> state.feeds.getOrNull(index)?.spec ?: Unit },
            pageNestedScrollConnection = PagerDefaults.pageNestedScrollConnection(
                state = pagerState,
                orientation = Orientation.Horizontal,
            ),
        ) { index ->
            val feedUi = state.feeds[index]
            NoteFeedList(
                feedSpec = feedUi.spec,
                pollingEnabled = pollingStates[feedUi] ?: false,
                noteCallbacks = noteCallbacks,
                showTopZaps = true,
                bigPillStreams = state.streams,
                showStreamsInNewPill = true,
                newNotesNoticeAlpha = (1 - topAppBarCollapsedFraction) * 1.0f,
                onGoToWallet = onGoToWallet,
                contentPadding = paddingValues,
                shouldAnimateScrollToTop = shouldAnimateScrollToTop.value,
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
    } else if (state.loading) {
        HeightAdjustableLoadingLazyListPlaceholder(
            height = 128.dp,
            contentPaddingValues = paddingValues,
            itemPadding = PaddingValues(horizontal = 16.dp),
        )
    } else {
        FeedsErrorColumn(
            modifier = Modifier.fillMaxSize(),
            text = stringResource(id = R.string.feeds_error_loading_user_feeds),
            onRefresh = { eventPublisher(UiEvent.RefreshNoteFeeds) },
            onRestoreDefaultFeeds = { eventPublisher(UiEvent.RestoreDefaultNoteFeeds) },
        )
    }
}

@ExperimentalMaterial3Api
@Composable
internal fun NoteFeedTopAppBar(
    title: String,
    avatarCdnImage: CdnImage?,
    onAvatarClick: () -> Unit,
    onSearchClick: () -> Unit,
    activeFeed: FeedUi?,
    onFeedChanged: (FeedUi) -> Unit,
    avatarLegendaryCustomization: LegendaryCustomization? = null,
    avatarBlossoms: List<String> = emptyList(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onGoToWallet: (() -> Unit)? = null,
) {
    var feedPickerVisible by rememberSaveable { mutableStateOf(false) }

    if (feedPickerVisible && activeFeed != null) {
        FeedsBottomSheet(
            activeFeed = activeFeed,
            feedSpecKind = FeedSpecKind.Notes,
            onFeedClick = { feed ->
                feedPickerVisible = false
                onFeedChanged(feed)
            },
            onDismissRequest = { feedPickerVisible = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onGoToWallet = onGoToWallet,
        )
    }

    PrimalTopAppBar(
        title = title,
        titleMaxLines = 1,
        titleOverflow = TextOverflow.Ellipsis,
        titleTrailingIcon = Icons.Default.ExpandMore,
        onTitleClick = {
            if (activeFeed != null) {
                feedPickerVisible = true
            }
        },
        avatarCdnImage = avatarCdnImage,
        avatarBlossoms = avatarBlossoms,
        legendaryCustomization = avatarLegendaryCustomization,
        navigationIcon = PrimalIcons.AvatarDefault,
        onNavigationIconClick = onAvatarClick,
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
