package net.primal.android.notes.home

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
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.activity.LocalContentDisplaySettings
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.FeedsErrorColumn
import net.primal.android.core.compose.HeightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.fab.NewPostFloatingActionButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.icons.primaliconpack.Search
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalDrawerScaffold
import net.primal.android.drawer.multiaccount.events.AccountSwitcherCallbacks
import net.primal.android.feeds.list.FeedsBottomSheet
import net.primal.android.feeds.list.ui.model.FeedUi
import net.primal.android.notes.feed.list.NoteFeedList
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.notes.home.HomeFeedContract.UiEvent
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.stream.player.LocalStreamState
import net.primal.domain.feeds.FeedSpecKind
import net.primal.domain.links.CdnImage

@Composable
fun HomeFeedScreen(
    viewModel: HomeFeedViewModel,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
    noteCallbacks: NoteCallbacks,
    accountSwitcherCallbacks: AccountSwitcherCallbacks,
    callbacks: HomeFeedContract.ScreenCallbacks,
) {
    val streamState = LocalStreamState.current
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(viewModel, viewModel.effects) {
        viewModel.effects.collect {
            when (it) {
                is HomeFeedContract.SideEffect.StartStream -> streamState.start(naddr = it.naddr)
            }
        }
    }

    DisposableLifecycleObserverEffect(viewModel) {
        when (it) {
            Lifecycle.Event.ON_START -> {
                viewModel.setEvent(UiEvent.RequestUserDataUpdate)
            }

            else -> Unit
        }
    }

    HomeFeedScreen(
        state = uiState.value,
        onTopLevelDestinationChanged = onTopLevelDestinationChanged,
        onDrawerScreenClick = onDrawerScreenClick,
        noteCallbacks = noteCallbacks,
        eventPublisher = viewModel::setEvent,
        accountSwitcherCallbacks = accountSwitcherCallbacks,
        callbacks = callbacks,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFeedScreen(
    state: HomeFeedContract.UiState,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
    noteCallbacks: NoteCallbacks,
    eventPublisher: (UiEvent) -> Unit,
    accountSwitcherCallbacks: AccountSwitcherCallbacks,
    callbacks: HomeFeedContract.ScreenCallbacks,
) {
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    SnackbarErrorHandler(
        error = state.uiError,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context = context) },
        onErrorDismiss = { eventPublisher(UiEvent.DismissError) },
    )

    var shouldAnimateScrollToTop by remember { mutableStateOf(false) }
    var activeFeed by remember { mutableStateOf<FeedUi?>(null) }
    val pagerState = rememberPagerState(pageCount = { state.feeds.size })

    val topAppBarState = remember {
        TopAppBarState(
            initialHeightOffsetLimit = -Float.MAX_VALUE,
            initialHeightOffset = 0f,
            initialContentOffset = 0f,
        )
    }

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
                    activeFeed = state.feeds[index]
                }
            }
    }

    PrimalDrawerScaffold(
        modifier = Modifier.semantics {
            testTagsAsResourceId = true
        },
        drawerState = drawerState,
        drawerOpenGestureEnabled = false,
        activeDestination = PrimalTopLevelDestination.Home,
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
        topAppBarState = topAppBarState,
        topAppBar = { scrollBehavior ->
            NoteFeedTopAppBar(
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
                onGoToWallet = callbacks.onGoToWallet,
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
                    val feedUi = state.feeds[index]
                    NoteFeedList(
                        feedSpec = feedUi.spec,
                        pollingEnabled = pollingStates[feedUi] ?: false,
                        noteCallbacks = noteCallbacks,
                        showTopZaps = true,
                        bigPillStreams = state.streams,
                        showStreamsInNewPill = true,
                        newNotesNoticeAlpha = (1 - topAppBarState.collapsedFraction) * 1.0f,
                        onGoToWallet = callbacks.onGoToWallet,
                        contentPadding = paddingValues,
                        shouldAnimateScrollToTop = shouldAnimateScrollToTop,
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
        },
        floatingActionButton = {
            NewPostFloatingActionButton(onNewPostClick = callbacks.onNewPostClick)
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@ExperimentalMaterial3Api
@Composable
private fun NoteFeedTopAppBar(
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
