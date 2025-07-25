package net.primal.android.articles.reads

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.primal.android.LocalContentDisplaySettings
import net.primal.android.R
import net.primal.android.articles.feed.ArticleFeedList
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.FeedsErrorColumn
import net.primal.android.core.compose.HeightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.icons.primaliconpack.Search
import net.primal.android.core.errors.UiError
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalDrawerScaffold
import net.primal.android.drawer.multiaccount.events.AccountSwitcherCallbacks
import net.primal.android.feeds.list.FeedsBottomSheet
import net.primal.android.feeds.list.ui.model.FeedUi
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.domain.feeds.FeedSpecKind
import net.primal.domain.links.CdnImage

@Composable
fun ReadsScreen(
    viewModel: ReadsViewModel,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
    accountSwitcherCallbacks: AccountSwitcherCallbacks,
    callbacks: ReadsScreenContract.ScreenCallbacks,
) {
    val uiState = viewModel.state.collectAsState()

    ReadsScreen(
        state = uiState.value,
        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
        onDrawerScreenClick = onDrawerScreenClick,
        eventPublisher = viewModel::setEvent,
        accountSwitcherCallbacks = accountSwitcherCallbacks,
        callbacks = callbacks,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadsScreen(
    state: ReadsScreenContract.UiState,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
    eventPublisher: (ReadsScreenContract.UiEvent) -> Unit,
    accountSwitcherCallbacks: AccountSwitcherCallbacks,
    callbacks: ReadsScreenContract.ScreenCallbacks,
) {
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    var shouldAnimateScrollToTop by remember { mutableStateOf(false) }
    var activeFeed by remember { mutableStateOf<FeedUi?>(null) }
    val pagerState = rememberPagerState(pageCount = { state.feeds.size })

    LaunchedEffect(pagerState, state.feeds) {
        snapshotFlow { pagerState.currentPage }
            .collect { index ->
                if (state.feeds.isNotEmpty()) {
                    activeFeed = state.feeds[index]
                }
            }
    }

    PrimalDrawerScaffold(
        drawerState = drawerState,
        drawerOpenGestureEnabled = false,
        activeDestination = PrimalTopLevelDestination.Reads,
        onActiveDestinationClick = {
            shouldAnimateScrollToTop = true
            uiScope.launch {
                delay(500.milliseconds)
                shouldAnimateScrollToTop = false
            }
        },
        accountSwitcherCallbacks = accountSwitcherCallbacks,
        onPrimaryDestinationChanged = onTopLevelDestinationChanged,
        onDrawerDestinationClick = onDrawerScreenClick,
        onDrawerQrCodeClick = callbacks.onDrawerQrCodeClick,
        badges = state.badges,
        focusModeEnabled = LocalContentDisplaySettings.current.focusModeEnabled,
        topAppBar = { scrollBehavior ->
            ArticleFeedTopAppBar(
                title = activeFeed?.title ?: "",
                activeFeed = activeFeed,
                avatarCdnImage = state.activeAccountAvatarCdnImage,
                avatarLegendaryCustomization = state.activeAccountLegendaryCustomization,
                avatarBlossoms = state.activeAccountBlossoms,
                onAvatarClick = { uiScope.launch { drawerState.open() } },
                onSearchClick = callbacks.onSearchClick,
                onFeedChanged = { feed ->
                    val pageIndex = state.feeds.indexOf(feed)
                    uiScope.launch { pagerState.scrollToPage(page = pageIndex) }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        content = { paddingValues ->
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
                        shouldAnimateScrollToTop = shouldAnimateScrollToTop,
                        onArticleClick = callbacks.onArticleClick,
                        onGetPremiumClick = callbacks.onGetPremiumClick,
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
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@ExperimentalMaterial3Api
@Composable
private fun ArticleFeedTopAppBar(
    title: String,
    avatarCdnImage: CdnImage?,
    onAvatarClick: () -> Unit,
    onSearchClick: () -> Unit,
    activeFeed: FeedUi?,
    onFeedChanged: (FeedUi) -> Unit,
    avatarLegendaryCustomization: LegendaryCustomization? = null,
    avatarBlossoms: List<String> = emptyList(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    var feedPickerVisible by rememberSaveable { mutableStateOf(false) }

    if (feedPickerVisible && activeFeed != null) {
        FeedsBottomSheet(
            activeFeed = activeFeed,
            feedSpecKind = FeedSpecKind.Reads,
            onFeedClick = { feed ->
                feedPickerVisible = false
                onFeedChanged(feed)
            },
            onDismissRequest = { feedPickerVisible = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        )
    }

    PrimalTopAppBar(
        title = title,
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
