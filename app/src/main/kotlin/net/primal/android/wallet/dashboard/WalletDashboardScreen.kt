package net.primal.android.wallet.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.icons.primaliconpack.WalletPurchaseSats
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.utils.isGoogleBuild
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalDrawerScaffold
import net.primal.android.theme.AppTheme
import net.primal.android.user.domain.WalletPreference
import net.primal.android.wallet.dashboard.WalletDashboardContract.UiEvent
import net.primal.android.wallet.dashboard.WalletDashboardContract.UiState.DashboardError
import net.primal.android.wallet.dashboard.ui.WalletAction
import net.primal.android.wallet.dashboard.ui.WalletCallToActionBox
import net.primal.android.wallet.dashboard.ui.WalletDashboard
import net.primal.android.wallet.dashboard.ui.WalletDashboardLite
import net.primal.android.wallet.domain.WalletKycLevel
import net.primal.android.wallet.store.inapp.InAppPurchaseBuyBottomSheet
import net.primal.android.wallet.transactions.TransactionsLazyColumn

@Composable
fun WalletDashboardScreen(
    viewModel: WalletDashboardViewModel,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    onWalletActivateClick: () -> Unit,
    onProfileClick: (String) -> Unit,
    onSendClick: () -> Unit,
    onScanClick: () -> Unit,
    onReceiveClick: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    WalletDashboardScreen(
        state = uiState.value,
        onPrimaryDestinationChanged = onPrimaryDestinationChanged,
        onDrawerDestinationClick = onDrawerDestinationClick,
        onWalletActivateClick = onWalletActivateClick,
        onProfileClick = onProfileClick,
        onSendClick = onSendClick,
        onScanClick = onScanClick,
        onReceiveClick = onReceiveClick,
        eventPublisher = { viewModel.setEvents(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WalletDashboardScreen(
    state: WalletDashboardContract.UiState,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    onWalletActivateClick: () -> Unit,
    onProfileClick: (String) -> Unit,
    onSendClick: () -> Unit,
    onScanClick: () -> Unit,
    onReceiveClick: () -> Unit,
    eventPublisher: (UiEvent) -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)

    val pagingItems = state.transactions.collectAsLazyPagingItems()
    val listState = pagingItems.rememberLazyListStatePagingWorkaround()

    var inAppPurchaseVisible by remember { mutableStateOf(false) }
    if (inAppPurchaseVisible) {
        InAppPurchaseBuyBottomSheet(
            onDismiss = { inAppPurchaseVisible = false },
        )
    }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = {
            when (it) {
                is DashboardError.InAppPurchaseNoticeError ->
                    it.message
                        ?: context.getString(R.string.app_generic_error)

                is DashboardError.InAppPurchaseConfirmationFailed ->
                    context.getString(R.string.wallet_in_app_purchase_error_confirmation_failed)
            }
        },
        onErrorDismiss = { eventPublisher(UiEvent.DismissError) },
    )

    LaunchedEffect(state.walletBalance) {
        pagingItems.refresh()
    }

    val dashboardExpanded by remember(listState) {
        derivedStateOf {
            listState.firstVisibleItemScrollOffset == 0 && listState.firstVisibleItemIndex <= 1
        }
    }

    var topBarHeight by remember { mutableIntStateOf(0) }

    PrimalDrawerScaffold(
        drawerState = drawerState,
        activeDestination = PrimalTopLevelDestination.Wallet,
        onActiveDestinationClick = { uiScope.launch { listState.animateScrollToItem(0) } },
        onPrimaryDestinationChanged = onPrimaryDestinationChanged,
        onDrawerDestinationClick = onDrawerDestinationClick,
        focusModeEnabled = false,
        topBar = { scrollBehaviour ->
            PrimalTopAppBar(
                modifier = Modifier.onSizeChanged { topBarHeight = it.height },
                title = stringResource(id = R.string.wallet_title),
                avatarCdnImage = state.activeAccountAvatarCdnImage,
                navigationIcon = PrimalIcons.AvatarDefault,
                onNavigationIconClick = {
                    uiScope.launch { drawerState.open() }
                },
                actions = {
                    if (isGoogleBuild()) {
                        AppBarIcon(
                            icon = PrimalIcons.WalletPurchaseSats,
                            onClick = {
                                inAppPurchaseVisible = true
                            },
                        )
                    }
                },
                scrollBehavior = scrollBehaviour,
                showDivider = !LocalPrimalTheme.current.isDarkTheme,
                footer = {
                    if (state.walletPreference != WalletPreference.NostrWalletConnect) {
                        AnimatedContent(
                            targetState = dashboardExpanded,
                            label = "DashboardAnimation",
                        ) { expanded ->
                            when (expanded) {
                                true -> WalletDashboard(
                                    modifier = Modifier
                                        .wrapContentSize(align = Alignment.Center)
                                        .padding(horizontal = 32.dp)
                                        .padding(vertical = 32.dp)
                                        .then(
                                            if (state.primalWallet?.kycLevel == WalletKycLevel.None) {
                                                Modifier.graphicsLayer { alpha = 0.42f }
                                            } else {
                                                Modifier
                                            },
                                        ),
                                    walletBalance = state.walletBalance,
                                    enabled = state.primalWallet?.kycLevel != WalletKycLevel.None,
                                    actions = listOf(WalletAction.Send, WalletAction.Scan, WalletAction.Receive),
                                    onWalletAction = { action ->
                                        when (action) {
                                            WalletAction.Send -> onSendClick()
                                            WalletAction.Scan -> onScanClick()
                                            WalletAction.Receive -> onReceiveClick()
                                        }
                                    },
                                )

                                false -> WalletDashboardLite(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .background(color = AppTheme.colorScheme.surface)
                                        .padding(horizontal = 10.dp, vertical = 16.dp),
                                    walletBalance = state.walletBalance,
                                    actions = listOf(WalletAction.Send, WalletAction.Scan, WalletAction.Receive),
                                    onWalletAction = { action ->
                                        when (action) {
                                            WalletAction.Send -> onSendClick()
                                            WalletAction.Scan -> onScanClick()
                                            WalletAction.Receive -> onReceiveClick()
                                        }
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
        content = { paddingValues ->
            when {
                state.walletPreference == WalletPreference.NostrWalletConnect -> {
                    WalletCallToActionBox(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 64.dp)
                            .padding(bottom = 32.dp)
                            .navigationBarsPadding(),
                        message = stringResource(id = R.string.wallet_dashboard_enable_wallet_notice_hint),
                        actionLabel = stringResource(id = R.string.wallet_dashboard_enable_wallet_button),
                        onActionClick = { eventPublisher(UiEvent.EnablePrimalWallet) },
                    )
                }

                state.primalWallet != null && state.primalWallet.kycLevel == WalletKycLevel.None -> {
                    WalletCallToActionBox(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 64.dp)
                            .padding(bottom = 32.dp)
                            .navigationBarsPadding(),
                        message = stringResource(id = R.string.wallet_dashboard_activate_notice_hint),
                        actionLabel = stringResource(id = R.string.wallet_dashboard_activate_button),
                        onActionClick = onWalletActivateClick,
                    )
                }

                else -> {
                    if (pagingItems.loadState.refresh is LoadState.NotLoading && pagingItems.isEmpty()) {
                        WalletCallToActionBox(
                            modifier = Modifier
                                .fillMaxSize()
                                .animateContentSize()
                                .padding(paddingValues)
                                .padding(bottom = 32.dp)
                                .navigationBarsPadding(),
                            message = stringResource(id = R.string.wallet_dashboard_no_transactions_hint),
                            actionLabel = if (isGoogleBuild()) {
                                stringResource(id = R.string.wallet_dashboard_buy_sats_button)
                            } else {
                                null
                            },
                            onActionClick = {
                                inAppPurchaseVisible = true
                            },
                        )
                    } else {
                        TransactionsLazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = AppTheme.colorScheme.surfaceVariant)
                                .padding(top = with(LocalDensity.current) { topBarHeight.toDp() }),
                            pagingItems = pagingItems,
                            listState = listState,
                            onProfileClick = onProfileClick,
                            header = {
                                if (state.lowBalance && pagingItems.itemCount > 0 && isGoogleBuild()) {
                                    WalletCallToActionBox(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .animateContentSize()
                                            .padding(bottom = 32.dp),
                                        message = stringResource(id = R.string.wallet_dashboard_low_sats_hint),
                                        actionLabel = stringResource(id = R.string.wallet_dashboard_buy_sats_button),
                                        onActionClick = {
                                            inAppPurchaseVisible = true
                                        },
                                    )
                                }
                            },
                        )
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}
