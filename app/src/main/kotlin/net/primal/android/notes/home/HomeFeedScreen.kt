package net.primal.android.notes.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.launch
import net.primal.android.LocalContentDisplaySettings
import net.primal.android.R
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.icons.primaliconpack.Search
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalDrawerScaffold
import net.primal.android.feeds.FeedsBottomSheet
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.feeds.ui.model.FeedUi
import net.primal.android.notes.feed.NoteFeedList
import net.primal.android.notes.feed.note.events.NoteCallbacks
import net.primal.android.notes.home.HomeFeedContract.UiEvent
import net.primal.android.theme.AppTheme

@Composable
fun HomeFeedScreen(
    viewModel: HomeFeedViewModel,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
    onDrawerQrCodeClick: () -> Unit,
    onSearchClick: () -> Unit,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    onNewPostClick: (content: TextFieldValue?) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

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
        onDrawerQrCodeClick = onDrawerQrCodeClick,
        onSearchClick = onSearchClick,
        noteCallbacks = noteCallbacks,
        onGoToWallet = onGoToWallet,
        onNewPostClick = onNewPostClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeFeedScreen(
    state: HomeFeedContract.UiState,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
    onDrawerQrCodeClick: () -> Unit,
    onSearchClick: () -> Unit,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    onNewPostClick: (content: TextFieldValue?) -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

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

    /** uiScope.launch { feedListState.animateScrollToItem(index = 0) } **/
    PrimalDrawerScaffold(
        drawerState = drawerState,
        activeDestination = PrimalTopLevelDestination.Home,
        onActiveDestinationClick = {
            /** uiScope.launch { feedListState.animateScrollToItem(index = 0) } **/
        },
        onPrimaryDestinationChanged = onTopLevelDestinationChanged,
        onDrawerDestinationClick = onDrawerScreenClick,
        onDrawerQrCodeClick = onDrawerQrCodeClick,
        badges = state.badges,
        focusModeEnabled = LocalContentDisplaySettings.current.focusModeEnabled,
        topBar = { scrollBehavior ->
            NoteFeedTopAppBar(
                title = activeFeed?.name ?: "",
                activeFeed = activeFeed,
                avatarCdnImage = state.activeAccountAvatarCdnImage,
                onAvatarClick = { uiScope.launch { drawerState.open() } },
                onSearchClick = onSearchClick,
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
                    pageNestedScrollConnection = remember(pagerState) {
                        PagerDefaults.pageNestedScrollConnection(pagerState, Orientation.Horizontal)
                    },
                ) { index ->
                    val spec = state.feeds[index].spec
                    NoteFeedList(
                        feedSpec = spec,
                        visible = activeFeed?.spec == spec,
                        noteCallbacks = noteCallbacks,
                        contentPadding = paddingValues,
                        onGoToWallet = onGoToWallet,
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNewPostClick(null) },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(color = AppTheme.colorScheme.primary, shape = CircleShape),
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                containerColor = Color.Unspecified,
                content = {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(id = R.string.accessibility_new_post),
                        tint = Color.White,
                    )
                },
            )
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
    scrollBehavior: TopAppBarScrollBehavior? = null,
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
