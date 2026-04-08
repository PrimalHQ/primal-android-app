package net.primal.android.main.reads

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.articles.feed.ArticleFeedList
import net.primal.android.core.compose.FeedsErrorColumn
import net.primal.android.core.compose.HeightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.PrimalTopLevelAppBar
import net.primal.android.core.errors.UiError
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.feeds.list.ui.model.FeedUi
import net.primal.android.navigation.navigateToArticleDetails
import net.primal.android.navigation.navigateToPremiumBuying
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.domain.links.CdnImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadsContent(
    onActiveFeedChanged: (FeedUi?) -> Unit,
    shouldAnimateScrollToTop: MutableState<Boolean>,
    scrollToFeed: MutableState<FeedUi?>,
    snackbarHostState: SnackbarHostState,
    paddingValues: PaddingValues,
    navController: NavController,
) {
    val readsViewModel = hiltViewModel<ReadsViewModel>()
    val readsState by readsViewModel.state.collectAsState()

    ReadsContent(
        state = readsState,
        eventPublisher = readsViewModel::setEvent,
        onActiveFeedChanged = onActiveFeedChanged,
        shouldAnimateScrollToTop = shouldAnimateScrollToTop,
        scrollToFeed = scrollToFeed,
        snackbarHostState = snackbarHostState,
        paddingValues = paddingValues,
        onArticleClick = { naddr -> navController.navigateToArticleDetails(naddr) },
        onGetPremiumClick = { navController.navigateToPremiumBuying() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadsContent(
    state: ReadsScreenContract.UiState,
    eventPublisher: (ReadsScreenContract.UiEvent) -> Unit,
    onActiveFeedChanged: (FeedUi?) -> Unit,
    shouldAnimateScrollToTop: MutableState<Boolean>,
    scrollToFeed: MutableState<FeedUi?> = remember { mutableStateOf(null) },
    snackbarHostState: SnackbarHostState,
    paddingValues: PaddingValues,
    onArticleClick: (String) -> Unit,
    onGetPremiumClick: () -> Unit,
) {
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { state.feeds.size })

    var activeFeed by remember { mutableStateOf<FeedUi?>(null) }

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
            ArticleFeedList(
                feedSpec = state.feeds[index].spec,
                contentPadding = paddingValues,
                shouldAnimateScrollToTop = shouldAnimateScrollToTop.value,
                onArticleClick = onArticleClick,
                onGetPremiumClick = onGetPremiumClick,
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
            onRefresh = { eventPublisher(ReadsScreenContract.UiEvent.RefreshReadsFeeds) },
            onRestoreDefaultFeeds = { eventPublisher(ReadsScreenContract.UiEvent.RestoreDefaultFeeds) },
        )
    }
}

@ExperimentalMaterial3Api
@Composable
internal fun ArticleFeedTopAppBar(
    title: String,
    avatarCdnImage: CdnImage?,
    onAvatarClick: () -> Unit,
    onFeedPickerRequest: () -> Unit,
    activeFeed: FeedUi?,
    avatarLegendaryCustomization: LegendaryCustomization? = null,
    avatarBlossoms: List<String> = emptyList(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    titleOverride: String? = null,
    subtitleOverride: String? = null,
    chevronExpanded: Boolean = false,
) {
    PrimalTopLevelAppBar(
        title = title,
        subtitle = activeFeed?.description?.ifBlank { null },
        titleOverride = titleOverride,
        subtitleOverride = subtitleOverride,
        showTitleChevron = true,
        chevronExpanded = chevronExpanded,
        onTitleClick = {
            if (activeFeed != null) {
                onFeedPickerRequest()
            }
        },
        avatarCdnImage = avatarCdnImage,
        avatarBlossoms = avatarBlossoms,
        avatarLegendaryCustomization = avatarLegendaryCustomization,
        onAvatarClick = onAvatarClick,
        scrollBehavior = scrollBehavior,
    )
}
