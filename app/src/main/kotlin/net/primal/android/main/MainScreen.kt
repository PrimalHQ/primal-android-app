package net.primal.android.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.activity.LocalContentDisplaySettings
import net.primal.android.core.compose.PrimalOverlay
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.feeds.list.FeedListOverlayContent
import net.primal.android.wallet.picker.WalletPickerOverlayContent
import net.primal.domain.feeds.FeedSpecKind
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.fab.NewPostFloatingActionButton
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalDrawer
import net.primal.android.drawer.PrimalMainScaffold
import net.primal.android.drawer.multiaccount.events.AccountSwitcherCallbacks
import net.primal.android.feeds.list.ui.model.FeedUi
import net.primal.android.main.explore.ExploreHomeContent
import net.primal.android.main.explore.ExploreTopAppBar
import net.primal.android.main.explore.ui.EXPLORE_HOME_TAB_COUNT
import net.primal.android.main.feeds.NoteFeedTopAppBar
import net.primal.android.main.feeds.NoteFeedsContent
import net.primal.android.main.feeds.NoteFeedsContract
import net.primal.android.main.feeds.NoteFeedsViewModel
import net.primal.android.main.notifications.NotificationsContent
import net.primal.android.main.notifications.NotificationsTopAppBar
import net.primal.android.main.reads.ArticleFeedTopAppBar
import net.primal.android.main.reads.ReadsContent
import net.primal.android.main.wallet.WalletDashboardContent
import net.primal.android.main.wallet.WalletDashboardContract
import net.primal.android.main.wallet.WalletDashboardTopAppBar
import net.primal.android.main.wallet.WalletDashboardViewModel
import net.primal.android.navigation.accountSwitcherCallbacksHandler
import net.primal.android.navigation.navigateToFollowPack
import net.primal.android.navigation.navigateToNoteEditor
import net.primal.android.navigation.navigateToProfileQrCodeViewer
import net.primal.android.navigation.noteCallbacksHandler
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.stream.player.LocalStreamState
import net.primal.domain.links.CdnImage
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

    // Tab state management
    var activeTab by rememberSaveable { mutableStateOf(PrimalTopLevelDestination.Feeds) }
    // Observe requestedTab from external navigation
    val requestedTab = navBackStackEntry.savedStateHandle
        .getStateFlow<String?>(REQUESTED_TAB_KEY, null)
        .collectAsState()

    LaunchedEffect(requestedTab.value) {
        val tabName = requestedTab.value ?: return@LaunchedEffect
        val destination = PrimalTopLevelDestination.entries.find { it.name == tabName }
        if (destination != null && destination != activeTab) {
            activeTab = destination
        }
        navBackStackEntry.savedStateHandle[REQUESTED_TAB_KEY] = null
    }

    // Shared callbacks
    val noteCallbacks = noteCallbacksHandler(navController)
    val accountSwitcherCallbacks = accountSwitcherCallbacksHandler(navController)

    val mainViewModel = hiltViewModel<MainViewModel>(navBackStackEntry)
    val mainState by mainViewModel.state.collectAsState()

    MainScreenSharedEffects(mainViewModel)

    val noteFeedsViewModel = hiltViewModel<NoteFeedsViewModel>(navBackStackEntry)
    val noteFeedsState by noteFeedsViewModel.state.collectAsState()

    MainScreenHomeEffects(noteFeedsViewModel)

    val homeTopAppBarState = rememberHomeTopAppBarState()
    val currentTopAppBarState = rememberPerTabTopAppBarState(activeTab, homeTopAppBarState)

    val sharedState = rememberMainScreenSharedState()

    SnackbarErrorHandler(
        error = noteFeedsState.uiError,
        snackbarHostState = sharedState.snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context = LocalContext.current) },
        onErrorDismiss = { noteFeedsViewModel.setEvent(NoteFeedsContract.UiEvent.DismissError) },
    )

    WalletErrorHandler(navBackStackEntry, sharedState.snackbarHostState)

    val onActiveDestinationClick: () -> Unit = {
        handleActiveDestinationClick(activeTab, sharedState, uiScope)
    }

    val onTabChanged: (PrimalTopLevelDestination) -> Unit = { destination ->
        if (destination != activeTab) {
            activeTab = destination
        }
    }

    val focusModeEnabled = when (activeTab) {
        PrimalTopLevelDestination.Wallet -> false
        else -> LocalContentDisplaySettings.current.focusModeEnabled
    }

    BackHandler(enabled = activeTab != PrimalTopLevelDestination.Feeds) {
        activeTab = PrimalTopLevelDestination.Feeds
    }

    MainScreenScaffold(
        activeTab = activeTab,
        mainState = mainState,
        homeState = noteFeedsState,
        homeEventPublisher = noteFeedsViewModel::setEvent,
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
    onAvatarClick: () -> Unit,
    onFeedPickerRequest: () -> Unit,
    onReadPickerRequest: () -> Unit,
    onWalletPickerRequest: () -> Unit,
    titleOverride: String? = null,
    subtitleOverride: String? = null,
    chevronExpanded: Boolean = false,
    avatarCdnImage: CdnImage?,
    avatarLegendaryCustomization: LegendaryCustomization?,
    avatarBlossoms: List<String>,
    homeActiveFeed: FeedUi?,
    readsActiveFeed: FeedUi?,
    explorePagerState: PagerState,
) {
    when (activeTab) {
        PrimalTopLevelDestination.Feeds -> {
            NoteFeedTopAppBar(
                title = homeActiveFeed?.title ?: "",
                activeFeed = homeActiveFeed,
                avatarCdnImage = avatarCdnImage,
                avatarLegendaryCustomization = avatarLegendaryCustomization,
                avatarBlossoms = avatarBlossoms,
                onAvatarClick = onAvatarClick,
                onFeedPickerRequest = onFeedPickerRequest,
                scrollBehavior = scrollBehavior,
                titleOverride = titleOverride,
                subtitleOverride = subtitleOverride,
                chevronExpanded = chevronExpanded,
            )
        }

        PrimalTopLevelDestination.Reads -> {
            ArticleFeedTopAppBar(
                title = readsActiveFeed?.title ?: "",
                activeFeed = readsActiveFeed,
                avatarCdnImage = avatarCdnImage,
                avatarLegendaryCustomization = avatarLegendaryCustomization,
                avatarBlossoms = avatarBlossoms,
                onAvatarClick = onAvatarClick,
                onFeedPickerRequest = onReadPickerRequest,
                scrollBehavior = scrollBehavior,
                titleOverride = titleOverride,
                subtitleOverride = subtitleOverride,
                chevronExpanded = chevronExpanded,
            )
        }

        PrimalTopLevelDestination.Explore -> {
            ExploreTopAppBar(
                pagerState = explorePagerState,
                avatarCdnImage = avatarCdnImage,
                avatarLegendaryCustomization = avatarLegendaryCustomization,
                avatarBlossoms = avatarBlossoms,
                onAvatarClick = onAvatarClick,
                scrollBehavior = scrollBehavior,
                titleOverride = titleOverride,
                subtitleOverride = subtitleOverride,
            )
        }

        PrimalTopLevelDestination.Alerts -> {
            NotificationsTopAppBar(
                avatarCdnImage = avatarCdnImage,
                avatarLegendaryCustomization = avatarLegendaryCustomization,
                avatarBlossoms = avatarBlossoms,
                scrollBehavior = scrollBehavior,
                onAvatarClick = onAvatarClick,
                titleOverride = titleOverride,
                subtitleOverride = subtitleOverride,
            )
        }

        PrimalTopLevelDestination.Wallet -> WalletDashboardTopAppBar(
            scrollBehavior = scrollBehavior,
            onAvatarClick = onAvatarClick,
            onWalletPickerRequest = onWalletPickerRequest,
            titleOverride = titleOverride,
            subtitleOverride = subtitleOverride,
            chevronExpanded = chevronExpanded,
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
    homeState: NoteFeedsContract.UiState,
    homeEventPublisher: (NoteFeedsContract.UiEvent) -> Unit,
    onHomeActiveFeedChanged: (FeedUi?) -> Unit,
    homeShouldAnimateScrollToTop: MutableState<Boolean>,
    homeScrollToFeed: MutableState<FeedUi?>,
    homeTopAppBarState: TopAppBarState,
    onReadsActiveFeedChanged: (FeedUi?) -> Unit,
    readsShouldAnimateScrollToTop: MutableState<Boolean>,
    readsScrollToFeed: MutableState<FeedUi?>,
    explorePagerState: PagerState,
    walletCurrencyMode: CurrencyMode,
    onWalletCurrencyModeToggle: (CurrencyMode) -> Unit,
    onWalletScrolledToTopChanged: (Boolean) -> Unit,
    walletShouldAnimateScrollToTop: MutableState<Boolean>,
    notificationsShouldAnimateScrollToTop: MutableState<Boolean>,
    navController: NavController,
    onGoToWallet: () -> Unit,
) {
    Box {
        Box(
            modifier = if (activeTab != PrimalTopLevelDestination.Feeds) {
                Modifier.graphicsLayer { alpha = 0f }
            } else {
                Modifier
            },
        ) {
            NoteFeedsContent(
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

        if (activeTab != PrimalTopLevelDestination.Feeds) {
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

                    PrimalTopLevelDestination.Alerts -> NotificationsContent(
                        paddingValues = paddingValues,
                        noteCallbacks = noteCallbacks,
                        onGoToWallet = onGoToWallet,
                        shouldAnimateScrollToTop = notificationsShouldAnimateScrollToTop,
                    )

                    PrimalTopLevelDestination.Wallet -> WalletDashboardContent(
                        currencyMode = walletCurrencyMode,
                        onCurrencyModeToggle = onWalletCurrencyModeToggle,
                        onScrolledToTopChanged = onWalletScrolledToTopChanged,
                        shouldAnimateScrollToTop = walletShouldAnimateScrollToTop,
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
    val walletIsScrolledToTop: MutableState<Boolean>,
    val walletShouldAnimateScrollToTop: MutableState<Boolean>,
    val notificationsShouldAnimateScrollToTop: MutableState<Boolean>,
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
        walletIsScrolledToTop = remember { mutableStateOf(true) },
        walletShouldAnimateScrollToTop = remember { mutableStateOf(false) },
        notificationsShouldAnimateScrollToTop = remember { mutableStateOf(false) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenScaffold(
    activeTab: PrimalTopLevelDestination,
    mainState: MainContract.UiState,
    homeState: NoteFeedsContract.UiState,
    homeEventPublisher: (NoteFeedsContract.UiEvent) -> Unit,
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

    var activeOverlay by rememberSaveable { mutableStateOf<ActiveOverlay?>(null) }

    val feedPickerVisible = activeOverlay == ActiveOverlay.FeedPicker
    val readPickerVisible = activeOverlay == ActiveOverlay.ReadPicker
    val walletPickerVisible = activeOverlay == ActiveOverlay.WalletPicker
    val accountDrawerVisible = activeOverlay == ActiveOverlay.AccountDrawer

    val streamState = LocalStreamState.current
    LaunchedEffect(accountDrawerVisible) {
        if (accountDrawerVisible) {
            streamState.acquireHide()
        } else {
            streamState.releaseHide()
        }
    }

    fun toggleOverlay(overlay: ActiveOverlay) {
        activeOverlay = if (activeOverlay == overlay) null else overlay
    }

    PrimalMainScaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        activeDestination = activeTab,
        onActiveDestinationClick = onActiveDestinationClick,
        onPrimaryDestinationChanged = onTabChanged,
        badges = mainState.badges,
        focusModeEnabled = focusModeEnabled,
        topAppBarState = currentTopAppBarState,
        topAppBar = { scrollBehavior ->
            val drawerTitle = if (accountDrawerVisible) stringResource(id = R.string.account_drawer_title) else null
            val drawerSubtitle = if (accountDrawerVisible) stringResource(id = R.string.account_drawer_subtitle) else null

            MainScreenTopAppBar(
                activeTab = activeTab,
                scrollBehavior = scrollBehavior,
                onAvatarClick = { toggleOverlay(ActiveOverlay.AccountDrawer) },
                onFeedPickerRequest = { toggleOverlay(ActiveOverlay.FeedPicker) },
                onReadPickerRequest = { toggleOverlay(ActiveOverlay.ReadPicker) },
                onWalletPickerRequest = { toggleOverlay(ActiveOverlay.WalletPicker) },
                titleOverride = drawerTitle,
                subtitleOverride = drawerSubtitle,
                chevronExpanded = feedPickerVisible || readPickerVisible || walletPickerVisible,
                avatarCdnImage = mainState.activeAccountAvatarCdnImage,
                avatarLegendaryCustomization = mainState.activeAccountLegendaryCustomization,
                avatarBlossoms = mainState.activeAccountBlossoms,
                homeActiveFeed = sharedState.homeActiveFeed.value,
                readsActiveFeed = sharedState.readsActiveFeed.value,
                explorePagerState = sharedState.explorePagerState,
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
                onWalletCurrencyModeToggle = { sharedState.walletCurrencyMode.value = it },
                onWalletScrolledToTopChanged = { sharedState.walletIsScrolledToTop.value = it },
                walletShouldAnimateScrollToTop = sharedState.walletShouldAnimateScrollToTop,
                notificationsShouldAnimateScrollToTop = sharedState.notificationsShouldAnimateScrollToTop,
                navController = navController,
                onGoToWallet = { onTabChanged(PrimalTopLevelDestination.Wallet) },
            )
        },
        overlay = {
            val dismissOverlay = { activeOverlay = null }

            PrimalOverlay(
                visible = accountDrawerVisible,
                onDismiss = dismissOverlay,
            ) {
                PrimalDrawer(
                    onDismiss = dismissOverlay,
                    onDrawerDestinationClick = onDrawerDestinationClick,
                    onQrCodeClick = { navController.navigateToProfileQrCodeViewer() },
                    accountSwitcherCallbacks = accountSwitcherCallbacks,
                )
            }

            val homeActiveFeed = sharedState.homeActiveFeed.value
            if (homeActiveFeed != null) {
                PrimalOverlay(
                    visible = feedPickerVisible,
                    onDismiss = dismissOverlay,
                ) {
                    FeedListOverlayContent(
                        activeFeed = homeActiveFeed,
                        feedSpecKind = FeedSpecKind.Notes,
                        onFeedClick = { feed ->
                            activeOverlay = null
                            sharedState.homeScrollToFeed.value = feed
                        },
                        onDismiss = dismissOverlay,
                        onGoToWallet = { onTabChanged(PrimalTopLevelDestination.Wallet) },
                    )
                }
            }

            val readsActiveFeed = sharedState.readsActiveFeed.value
            if (readsActiveFeed != null) {
                PrimalOverlay(
                    visible = readPickerVisible,
                    onDismiss = dismissOverlay,
                ) {
                    FeedListOverlayContent(
                        activeFeed = readsActiveFeed,
                        feedSpecKind = FeedSpecKind.Reads,
                        onFeedClick = { feed ->
                            activeOverlay = null
                            sharedState.readsScrollToFeed.value = feed
                        },
                        onDismiss = dismissOverlay,
                    )
                }
            }

            PrimalOverlay(
                visible = walletPickerVisible,
                onDismiss = dismissOverlay,
            ) {
                WalletPickerOverlayContent(
                    onDismiss = dismissOverlay,
                )
            }
        },
        floatingActionButton = {
            when (activeTab) {
                PrimalTopLevelDestination.Feeds,
                PrimalTopLevelDestination.Explore,
                PrimalTopLevelDestination.Alerts,
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
private fun MainScreenSharedEffects(mainViewModel: MainViewModel) {
    DisposableLifecycleObserverEffect(mainViewModel) {
        when (it) {
            Lifecycle.Event.ON_START -> {
                mainViewModel.setEvent(MainContract.UiEvent.RequestUserDataUpdate)
            }

            else -> Unit
        }
    }
}

@Composable
private fun MainScreenHomeEffects(noteFeedsViewModel: NoteFeedsViewModel) {
    val streamState = LocalStreamState.current
    LaunchedEffect(noteFeedsViewModel, noteFeedsViewModel.effects) {
        noteFeedsViewModel.effects.collect {
            when (it) {
                is NoteFeedsContract.SideEffect.StartStream -> streamState.start(naddr = it.naddr)
            }
        }
    }
}

@Composable
private fun WalletErrorHandler(navBackStackEntry: NavBackStackEntry, snackbarHostState: SnackbarHostState) {
    val walletViewModel = hiltViewModel<WalletDashboardViewModel>(navBackStackEntry)
    val walletState by walletViewModel.state.collectAsState()
    val context = LocalContext.current
    SnackbarErrorHandler(
        error = walletState.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = {
            when (it) {
                is WalletDashboardContract.UiState.DashboardError.InAppPurchaseNoticeError ->
                    it.message ?: context.getString(R.string.app_generic_error)

                is WalletDashboardContract.UiState.DashboardError.InAppPurchaseConfirmationFailed ->
                    context.getString(R.string.wallet_in_app_purchase_error_confirmation_failed)

                is WalletDashboardContract.UiState.DashboardError.WalletCreationFailed ->
                    context.getString(R.string.wallet_dashboard_create_wallet_error)
            }
        },
        onErrorDismiss = { walletViewModel.setEvents(WalletDashboardContract.UiEvent.DismissError) },
    )
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
        PrimalTopLevelDestination.Feeds -> homeTopAppBarState
        PrimalTopLevelDestination.Reads -> readsTopAppBarState
        PrimalTopLevelDestination.Explore -> exploreTopAppBarState
        PrimalTopLevelDestination.Alerts -> notificationsTopAppBarState
        PrimalTopLevelDestination.Wallet -> walletTopAppBarState
    }
}

private fun handleActiveDestinationClick(
    activeTab: PrimalTopLevelDestination,
    sharedState: MainScreenSharedState,
    scope: kotlinx.coroutines.CoroutineScope,
) {
    val target = when (activeTab) {
        PrimalTopLevelDestination.Feeds -> sharedState.homeShouldAnimateScrollToTop
        PrimalTopLevelDestination.Reads -> sharedState.readsShouldAnimateScrollToTop
        PrimalTopLevelDestination.Wallet -> sharedState.walletShouldAnimateScrollToTop
        PrimalTopLevelDestination.Alerts -> sharedState.notificationsShouldAnimateScrollToTop
        else -> null
    }
    target?.let {
        it.value = true
        scope.launch {
            delay(500.milliseconds)
            it.value = false
        }
    }
}

private enum class ActiveOverlay {
    AccountDrawer,
    FeedPicker,
    ReadPicker,
    WalletPicker,
}
