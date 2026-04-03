package net.primal.android.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.primal.android.core.activity.LocalContentDisplaySettings
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.fab.NewPostFloatingActionButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AdvancedSearch
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalDrawerScaffold
import net.primal.android.drawer.multiaccount.events.AccountSwitcherCallbacks
import net.primal.android.explore.search.ui.SearchScope
import net.primal.android.feeds.list.ui.model.FeedUi
import net.primal.android.main.explore.ExploreHomeContent
import net.primal.android.main.explore.ExploreHomeViewModel
import net.primal.android.main.explore.ExploreTopAppBar
import net.primal.android.main.explore.ui.EXPLORE_HOME_TAB_COUNT
import net.primal.android.main.home.HomeFeedContent
import net.primal.android.main.home.HomeFeedContract
import net.primal.android.main.home.HomeFeedViewModel
import net.primal.android.main.home.NoteFeedTopAppBar
import net.primal.android.main.notifications.NotificationsContent
import net.primal.android.main.notifications.NotificationsTopAppBar
import net.primal.android.main.notifications.NotificationsViewModel
import net.primal.android.main.reads.ArticleFeedTopAppBar
import net.primal.android.main.reads.ReadsContent
import net.primal.android.main.reads.ReadsViewModel
import net.primal.android.main.wallet.WalletDashboardContent
import net.primal.android.main.wallet.WalletDashboardTopAppBar
import net.primal.android.navigation.accountSwitcherCallbacksHandler
import net.primal.android.navigation.navigateToAdvancedSearch
import net.primal.android.navigation.navigateToFollowPack
import net.primal.android.navigation.navigateToNoteEditor
import net.primal.android.navigation.navigateToProfileQrCodeViewer
import net.primal.android.navigation.navigateToSearch
import net.primal.android.navigation.navigateToWallet
import net.primal.android.navigation.noteCallbacksHandler
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.stream.player.LocalStreamState
import net.primal.domain.wallet.CurrencyMode

internal const val REQUESTED_TAB_KEY = "requestedTab"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    navBackStackEntry: NavBackStackEntry,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)

    // Tab state management
    var activeTab by rememberSaveable { mutableStateOf(PrimalTopLevelDestination.Home) }
    var tabBackStack by rememberSaveable {
        mutableStateOf(listOf(PrimalTopLevelDestination.Home))
    }

    // Observe requestedTab from external navigation
    val requestedTab = navBackStackEntry.savedStateHandle
        .getStateFlow<String?>(REQUESTED_TAB_KEY, null)
        .collectAsState()

    LaunchedEffect(requestedTab.value) {
        val tabName = requestedTab.value ?: return@LaunchedEffect
        val destination = PrimalTopLevelDestination.entries.find { it.name == tabName }
        if (destination != null && destination != activeTab) {
            activeTab = destination
            tabBackStack = tabBackStack.filter { it != destination } + destination
        }
        navBackStackEntry.savedStateHandle[REQUESTED_TAB_KEY] = null
    }

    // Shared callbacks
    val noteCallbacks = noteCallbacksHandler(navController)
    val accountSwitcherCallbacks = accountSwitcherCallbacksHandler(navController)

    // Home ViewModel (always composed, provides badges for all tabs)
    val homeFeedViewModel = hiltViewModel<HomeFeedViewModel>(navBackStackEntry)
    val homeState by homeFeedViewModel.state.collectAsState()

    MainScreenHomeEffects(homeFeedViewModel)

    val homeTopAppBarState = rememberHomeTopAppBarState()
    val currentTopAppBarState = rememberPerTabTopAppBarState(activeTab, homeTopAppBarState)

    val sharedState = rememberMainScreenSharedState()

    SnackbarErrorHandler(
        error = homeState.uiError,
        snackbarHostState = sharedState.snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context = LocalContext.current) },
        onErrorDismiss = { homeFeedViewModel.setEvent(HomeFeedContract.UiEvent.DismissError) },
    )

    val onActiveDestinationClick: () -> Unit = {
        handleActiveDestinationClick(
            activeTab,
            sharedState.homeShouldAnimateScrollToTop,
            sharedState.readsShouldAnimateScrollToTop,
            uiScope,
        )
    }

    val onTabChanged: (PrimalTopLevelDestination) -> Unit = { destination ->
        if (destination != activeTab) {
            activeTab = destination
            tabBackStack = tabBackStack.filter { it != destination } + destination
        }
    }

    val focusModeEnabled = when (activeTab) {
        PrimalTopLevelDestination.Wallet -> false
        else -> LocalContentDisplaySettings.current.focusModeEnabled
    }

    BackHandler(enabled = tabBackStack.size > 1) {
        val newStack = tabBackStack.dropLast(1)
        tabBackStack = newStack
        activeTab = newStack.last()
    }

    MainScreenScaffold(
        activeTab = activeTab,
        drawerState = drawerState,
        homeState = homeState,
        homeEventPublisher = homeFeedViewModel::setEvent,
        homeTopAppBarState = homeTopAppBarState,
        currentTopAppBarState = currentTopAppBarState,
        sharedState = sharedState,
        noteCallbacks = noteCallbacks,
        accountSwitcherCallbacks = accountSwitcherCallbacks,
        focusModeEnabled = focusModeEnabled,
        onActiveDestinationClick = onActiveDestinationClick,
        onTabChanged = onTabChanged,
        onDrawerDestinationClick = onDrawerDestinationClick,
        navController = navController,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenTopAppBar(
    activeTab: PrimalTopLevelDestination,
    scrollBehavior: TopAppBarScrollBehavior?,
    drawerState: DrawerState,
    homeState: HomeFeedContract.UiState,
    homeActiveFeed: FeedUi?,
    homeScrollToFeed: MutableState<FeedUi?>,
    readsActiveFeed: FeedUi?,
    readsScrollToFeed: MutableState<FeedUi?>,
    explorePagerState: PagerState,
    walletIsScrolledToTop: Boolean,
    walletCurrencyMode: CurrencyMode,
    onWalletTopBarHeightChanged: (Int) -> Unit,
    onWalletTopBarFooterHeightChanged: (Int) -> Unit,
    onWalletCurrencyModeToggle: (CurrencyMode) -> Unit,
    navController: NavController,
) {
    val uiScope = rememberCoroutineScope()

    when (activeTab) {
        PrimalTopLevelDestination.Home -> {
            NoteFeedTopAppBar(
                title = homeActiveFeed?.title ?: "",
                activeFeed = homeActiveFeed,
                avatarCdnImage = homeState.activeAccountAvatarCdnImage,
                avatarLegendaryCustomization = homeState.activeAccountLegendaryCustomization,
                avatarBlossoms = homeState.activeAccountBlossoms,
                onAvatarClick = { uiScope.launch { drawerState.open() } },
                onSearchClick = { navController.navigateToSearch(searchScope = SearchScope.Notes) },
                onFeedChanged = { feed -> homeScrollToFeed.value = feed },
                scrollBehavior = scrollBehavior,
                onGoToWallet = { navController.navigateToWallet() },
            )
        }

        PrimalTopLevelDestination.Reads -> {
            val readsViewModel = hiltViewModel<ReadsViewModel>()
            val readsState by readsViewModel.state.collectAsState()
            ArticleFeedTopAppBar(
                title = readsActiveFeed?.title ?: "",
                activeFeed = readsActiveFeed,
                avatarCdnImage = readsState.activeAccountAvatarCdnImage,
                avatarLegendaryCustomization = readsState.activeAccountLegendaryCustomization,
                avatarBlossoms = readsState.activeAccountBlossoms,
                onAvatarClick = { uiScope.launch { drawerState.open() } },
                onSearchClick = { navController.navigateToSearch(searchScope = SearchScope.Reads) },
                onFeedChanged = { feed -> readsScrollToFeed.value = feed },
                scrollBehavior = scrollBehavior,
            )
        }

        PrimalTopLevelDestination.Explore -> {
            val exploreViewModel = hiltViewModel<ExploreHomeViewModel>()
            val exploreState by exploreViewModel.state.collectAsState()
            ExploreTopAppBar(
                pagerState = explorePagerState,
                actionIcon = PrimalIcons.AdvancedSearch,
                avatarCdnImage = exploreState.activeAccountAvatarCdnImage,
                avatarLegendaryCustomization = exploreState.activeAccountLegendaryCustomization,
                avatarBlossoms = exploreState.activeAccountBlossoms,
                navigationIcon = PrimalIcons.AvatarDefault,
                onNavigationIconClick = { uiScope.launch { drawerState.open() } },
                onSearchClick = { navController.navigateToSearch(searchScope = SearchScope.Notes) },
                onActionIconClick = { navController.navigateToAdvancedSearch() },
                scrollBehavior = scrollBehavior,
            )
        }

        PrimalTopLevelDestination.Notifications -> {
            val notificationsViewModel = hiltViewModel<NotificationsViewModel>()
            val notificationsState by notificationsViewModel.state.collectAsState()
            NotificationsTopAppBar(
                state = notificationsState,
                scrollBehavior = scrollBehavior,
                onNavigationIconClick = { uiScope.launch { drawerState.open() } },
                onSearchClick = {
                    navController.navigateToSearch(searchScope = SearchScope.MyNotifications)
                },
            )
        }

        PrimalTopLevelDestination.Wallet -> WalletDashboardTopAppBar(
            isScrolledToTop = walletIsScrolledToTop,
            currencyMode = walletCurrencyMode,
            scrollBehavior = scrollBehavior,
            onTopBarHeightChanged = onWalletTopBarHeightChanged,
            onTopBarFooterHeightChanged = onWalletTopBarFooterHeightChanged,
            onCurrencyModeToggle = onWalletCurrencyModeToggle,
            onAvatarClick = { uiScope.launch { drawerState.open() } },
            navController = navController,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenContent(
    activeTab: PrimalTopLevelDestination,
    saveableStateHolder: androidx.compose.runtime.saveable.SaveableStateHolder,
    paddingValues: PaddingValues,
    snackbarHostState: SnackbarHostState,
    noteCallbacks: NoteCallbacks,
    homeState: HomeFeedContract.UiState,
    homeEventPublisher: (HomeFeedContract.UiEvent) -> Unit,
    onHomeActiveFeedChanged: (FeedUi?) -> Unit,
    homeShouldAnimateScrollToTop: MutableState<Boolean>,
    homeScrollToFeed: MutableState<FeedUi?>,
    homeTopAppBarState: TopAppBarState,
    onReadsActiveFeedChanged: (FeedUi?) -> Unit,
    readsShouldAnimateScrollToTop: MutableState<Boolean>,
    readsScrollToFeed: MutableState<FeedUi?>,
    explorePagerState: PagerState,
    walletCurrencyMode: CurrencyMode,
    walletTopBarHeight: Int,
    walletTopBarFooterHeight: Int,
    onWalletScrolledToTopChanged: (Boolean) -> Unit,
    navController: NavController,
    onGoToWallet: () -> Unit,
) {
    Box {
        Box(
            modifier = if (activeTab != PrimalTopLevelDestination.Home) {
                Modifier.graphicsLayer { alpha = 0f }
            } else {
                Modifier
            },
        ) {
            HomeFeedContent(
                state = homeState,
                noteCallbacks = noteCallbacks,
                eventPublisher = homeEventPublisher,
                onActiveFeedChanged = onHomeActiveFeedChanged,
                topAppBarCollapsedFraction = homeTopAppBarState.collapsedFraction,
                shouldAnimateScrollToTop = homeShouldAnimateScrollToTop,
                scrollToFeed = homeScrollToFeed,
                snackbarHostState = snackbarHostState,
                paddingValues = paddingValues,
                onGoToWallet = onGoToWallet,
            )
        }

        if (activeTab != PrimalTopLevelDestination.Home) {
            saveableStateHolder.SaveableStateProvider(activeTab.name) {
                when (activeTab) {
                    PrimalTopLevelDestination.Reads -> ReadsContent(
                        onActiveFeedChanged = onReadsActiveFeedChanged,
                        shouldAnimateScrollToTop = readsShouldAnimateScrollToTop,
                        scrollToFeed = readsScrollToFeed,
                        snackbarHostState = snackbarHostState,
                        paddingValues = paddingValues,
                        navController = navController,
                    )

                    PrimalTopLevelDestination.Explore -> ExploreHomeContent(
                        pagerState = explorePagerState,
                        paddingValues = paddingValues,
                        noteCallbacks = noteCallbacks,
                        snackbarHostState = snackbarHostState,
                        onFollowPackClick = { profileId, identifier ->
                            navController.navigateToFollowPack(profileId, identifier)
                        },
                        onGoToWallet = onGoToWallet,
                    )

                    PrimalTopLevelDestination.Notifications -> NotificationsContent(
                        paddingValues = paddingValues,
                        noteCallbacks = noteCallbacks,
                        onGoToWallet = onGoToWallet,
                        navController = navController,
                    )

                    PrimalTopLevelDestination.Wallet -> WalletDashboardContent(
                        currencyMode = walletCurrencyMode,
                        topBarHeight = walletTopBarHeight,
                        topBarFooterHeight = walletTopBarFooterHeight,
                        onScrolledToTopChanged = onWalletScrolledToTopChanged,
                        paddingValues = paddingValues,
                        navController = navController,
                    )

                    else -> {}
                }
            }
        }
    }
}

@Suppress("LongParameterList")
private class MainScreenSharedState(
    val snackbarHostState: SnackbarHostState,
    val homeActiveFeed: MutableState<FeedUi?>,
    val readsActiveFeed: MutableState<FeedUi?>,
    val explorePagerState: PagerState,
    val homeShouldAnimateScrollToTop: MutableState<Boolean>,
    val homeScrollToFeed: MutableState<FeedUi?>,
    val readsShouldAnimateScrollToTop: MutableState<Boolean>,
    val readsScrollToFeed: MutableState<FeedUi?>,
    val walletCurrencyMode: MutableState<CurrencyMode>,
    val walletTopBarHeight: MutableState<Int>,
    val walletTopBarFooterHeight: MutableState<Int>,
    val walletIsScrolledToTop: MutableState<Boolean>,
)

@Composable
private fun rememberMainScreenSharedState(): MainScreenSharedState {
    return MainScreenSharedState(
        snackbarHostState = remember { SnackbarHostState() },
        homeActiveFeed = remember { mutableStateOf(null) },
        readsActiveFeed = remember { mutableStateOf(null) },
        explorePagerState = rememberPagerState { EXPLORE_HOME_TAB_COUNT },
        homeShouldAnimateScrollToTop = remember { mutableStateOf(false) },
        homeScrollToFeed = remember { mutableStateOf(null) },
        readsShouldAnimateScrollToTop = remember { mutableStateOf(false) },
        readsScrollToFeed = remember { mutableStateOf(null) },
        walletCurrencyMode = rememberSaveable { mutableStateOf(CurrencyMode.SATS) },
        walletTopBarHeight = remember { mutableIntStateOf(0) },
        walletTopBarFooterHeight = remember { mutableIntStateOf(0) },
        walletIsScrolledToTop = remember { mutableStateOf(true) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenScaffold(
    activeTab: PrimalTopLevelDestination,
    drawerState: DrawerState,
    homeState: HomeFeedContract.UiState,
    homeEventPublisher: (HomeFeedContract.UiEvent) -> Unit,
    homeTopAppBarState: TopAppBarState,
    currentTopAppBarState: TopAppBarState,
    sharedState: MainScreenSharedState,
    noteCallbacks: NoteCallbacks,
    accountSwitcherCallbacks: AccountSwitcherCallbacks,
    focusModeEnabled: Boolean,
    onActiveDestinationClick: () -> Unit,
    onTabChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    navController: NavController,
) {
    val saveableStateHolder = rememberSaveableStateHolder()

    PrimalDrawerScaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        drawerState = drawerState,
        drawerOpenGestureEnabled = false,
        activeDestination = activeTab,
        onActiveDestinationClick = onActiveDestinationClick,
        accountSwitcherCallbacks = accountSwitcherCallbacks,
        onPrimaryDestinationChanged = onTabChanged,
        onDrawerDestinationClick = onDrawerDestinationClick,
        onDrawerQrCodeClick = { navController.navigateToProfileQrCodeViewer() },
        badges = homeState.badges,
        focusModeEnabled = focusModeEnabled,
        topAppBarState = currentTopAppBarState,
        topAppBar = { scrollBehavior ->
            MainScreenTopAppBar(
                activeTab = activeTab,
                scrollBehavior = scrollBehavior,
                drawerState = drawerState,
                homeState = homeState,
                homeActiveFeed = sharedState.homeActiveFeed.value,
                homeScrollToFeed = sharedState.homeScrollToFeed,
                readsActiveFeed = sharedState.readsActiveFeed.value,
                readsScrollToFeed = sharedState.readsScrollToFeed,
                explorePagerState = sharedState.explorePagerState,
                walletIsScrolledToTop = sharedState.walletIsScrolledToTop.value,
                walletCurrencyMode = sharedState.walletCurrencyMode.value,
                onWalletTopBarHeightChanged = { sharedState.walletTopBarHeight.value = it },
                onWalletTopBarFooterHeightChanged = { sharedState.walletTopBarFooterHeight.value = it },
                onWalletCurrencyModeToggle = { sharedState.walletCurrencyMode.value = it },
                navController = navController,
            )
        },
        content = { paddingValues ->
            MainScreenContent(
                activeTab = activeTab,
                saveableStateHolder = saveableStateHolder,
                paddingValues = paddingValues,
                snackbarHostState = sharedState.snackbarHostState,
                noteCallbacks = noteCallbacks,
                homeState = homeState,
                homeEventPublisher = homeEventPublisher,
                onHomeActiveFeedChanged = { sharedState.homeActiveFeed.value = it },
                homeShouldAnimateScrollToTop = sharedState.homeShouldAnimateScrollToTop,
                homeScrollToFeed = sharedState.homeScrollToFeed,
                homeTopAppBarState = homeTopAppBarState,
                onReadsActiveFeedChanged = { sharedState.readsActiveFeed.value = it },
                readsShouldAnimateScrollToTop = sharedState.readsShouldAnimateScrollToTop,
                readsScrollToFeed = sharedState.readsScrollToFeed,
                explorePagerState = sharedState.explorePagerState,
                walletCurrencyMode = sharedState.walletCurrencyMode.value,
                walletTopBarHeight = sharedState.walletTopBarHeight.value,
                walletTopBarFooterHeight = sharedState.walletTopBarFooterHeight.value,
                onWalletScrolledToTopChanged = { sharedState.walletIsScrolledToTop.value = it },
                navController = navController,
                onGoToWallet = { onTabChanged(PrimalTopLevelDestination.Wallet) },
            )
        },
        floatingActionButton = {
            when (activeTab) {
                PrimalTopLevelDestination.Home,
                PrimalTopLevelDestination.Explore,
                PrimalTopLevelDestination.Notifications,
                -> NewPostFloatingActionButton(
                    onNewPostClick = { navController.navigateToNoteEditor(null) },
                )

                else -> {}
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = sharedState.snackbarHostState)
        },
    )
}

@Composable
private fun MainScreenHomeEffects(homeFeedViewModel: HomeFeedViewModel) {
    val streamState = LocalStreamState.current
    LaunchedEffect(homeFeedViewModel, homeFeedViewModel.effects) {
        homeFeedViewModel.effects.collect {
            when (it) {
                is HomeFeedContract.SideEffect.StartStream -> streamState.start(naddr = it.naddr)
            }
        }
    }

    DisposableLifecycleObserverEffect(homeFeedViewModel) {
        when (it) {
            Lifecycle.Event.ON_START -> {
                homeFeedViewModel.setEvent(HomeFeedContract.UiEvent.RequestUserDataUpdate)
            }

            else -> Unit
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberHomeTopAppBarState(): TopAppBarState {
    return remember {
        TopAppBarState(
            initialHeightOffsetLimit = -Float.MAX_VALUE,
            initialHeightOffset = 0f,
            initialContentOffset = 0f,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberPerTabTopAppBarState(
    activeTab: PrimalTopLevelDestination,
    homeTopAppBarState: TopAppBarState,
): TopAppBarState {
    val readsTopAppBarState = rememberTopAppBarState()
    val exploreTopAppBarState = rememberTopAppBarState()
    val notificationsTopAppBarState = rememberTopAppBarState()
    val walletTopAppBarState = rememberTopAppBarState()

    return when (activeTab) {
        PrimalTopLevelDestination.Home -> homeTopAppBarState
        PrimalTopLevelDestination.Reads -> readsTopAppBarState
        PrimalTopLevelDestination.Explore -> exploreTopAppBarState
        PrimalTopLevelDestination.Notifications -> notificationsTopAppBarState
        PrimalTopLevelDestination.Wallet -> walletTopAppBarState
    }
}

private fun handleActiveDestinationClick(
    activeTab: PrimalTopLevelDestination,
    homeShouldAnimateScrollToTop: MutableState<Boolean>,
    readsShouldAnimateScrollToTop: MutableState<Boolean>,
    scope: kotlinx.coroutines.CoroutineScope,
) {
    when (activeTab) {
        PrimalTopLevelDestination.Home -> {
            homeShouldAnimateScrollToTop.value = true
            scope.launch {
                delay(500.milliseconds)
                homeShouldAnimateScrollToTop.value = false
            }
        }

        PrimalTopLevelDestination.Reads -> {
            readsShouldAnimateScrollToTop.value = true
            scope.launch {
                delay(500.milliseconds)
                readsShouldAnimateScrollToTop.value = false
            }
        }

        else -> {}
    }
}
