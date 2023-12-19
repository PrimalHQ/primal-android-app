package net.primal.android.wallet.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.icons.primaliconpack.WalletPurchaseSats
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalBottomBarHeightDp
import net.primal.android.drawer.PrimalDrawerScaffold
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.dashboard.WalletDashboardContract.UiEvent
import net.primal.android.wallet.dashboard.WalletDashboardContract.UiState.DashboardError
import net.primal.android.wallet.dashboard.ui.WalletAction
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

    val bottomBarHeight = PrimalBottomBarHeightDp
    var bottomBarOffsetHeightPx by remember { mutableFloatStateOf(0f) }

    val pagingItems = state.transactions.collectAsLazyPagingItems()
    val listState = pagingItems.rememberLazyListStatePagingWorkaround()

    var inAppPurchaseVisible by remember { mutableStateOf(false) }
    if (inAppPurchaseVisible) {
        InAppPurchaseBuyBottomSheet(
            onDismiss = { inAppPurchaseVisible = false },
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    ErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        onErrorDismiss = { eventPublisher(UiEvent.DismissError) },
    )

    LaunchedEffect(state.walletBalance) {
        pagingItems.refresh()
    }

    var dashboardLiteVisible by remember { mutableStateOf(false) }
    LaunchedEffect(listState) {
        withContext(Dispatchers.IO) {
            snapshotFlow { listState.isScrollInProgress }
                .distinctUntilChanged()
                .collect {
                    dashboardLiteVisible = listState.firstVisibleItemIndex >= 2
                }
        }
    }

    PrimalDrawerScaffold(
        drawerState = drawerState,
        activeDestination = PrimalTopLevelDestination.Wallet,
        onActiveDestinationClick = { uiScope.launch { listState.animateScrollToItem(0) } },
        onPrimaryDestinationChanged = onPrimaryDestinationChanged,
        onDrawerDestinationClick = onDrawerDestinationClick,
        bottomBarHeight = bottomBarHeight,
        onBottomBarOffsetChange = { bottomBarOffsetHeightPx = it },
        focusModeEnabled = false,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.wallet_title),
                avatarCdnImage = state.activeAccountAvatarCdnImage,
                navigationIcon = PrimalIcons.AvatarDefault,
                onNavigationIconClick = {
                    uiScope.launch { drawerState.open() }
                },
                actions = {
                    AppBarIcon(
                        icon = PrimalIcons.WalletPurchaseSats,
                        onClick = {
                            inAppPurchaseVisible = true
                        },
                    )
                },
                scrollBehavior = it,
                footer = {
                    Column {
                        WalletDashboardLite(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (dashboardLiteVisible) 80.dp else 0.dp)
                                .background(color = AppTheme.colorScheme.surface)
                                .padding(horizontal = 8.dp, vertical = 16.dp),
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
                },
            )
        },
        content = { paddingValues ->
            if (state.primalWallet != null && state.primalWallet.kycLevel != WalletKycLevel.None) {
                TransactionsLazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = AppTheme.colorScheme.surfaceVariant)
                        .systemBarsPadding(),
                    walletBalance = state.walletBalance,
                    primalWallet = state.primalWallet,
                    walletPreference = state.walletPreference,
                    eventPublisher = eventPublisher,
                    pagingItems = pagingItems,
                    listState = listState,
                    paddingValues = paddingValues,
                    onProfileClick = onProfileClick,
                    onWalletAction = { action ->
                        when (action) {
                            WalletAction.Send -> onSendClick()
                            WalletAction.Scan -> onScanClick()
                            WalletAction.Receive -> onReceiveClick()
                        }
                    },
                )
            } else {
                ActivateWalletNotice(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    onActivateClick = onWalletActivateClick,
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@Composable
private fun ActivateWalletNotice(modifier: Modifier, onActivateClick: () -> Unit) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.wrapContentSize(align = Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier.padding(vertical = 16.dp),
                text = stringResource(id = R.string.wallet_dashboard_activate_notice_hint),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                style = AppTheme.typography.bodyMedium,
            )

            PrimalFilledButton(
                modifier = Modifier.fillMaxWidth(fraction = 0.8f),
                onClick = onActivateClick,
            ) {
                Text(text = stringResource(id = R.string.wallet_dashboard_activate_button))
            }
        }
    }
}

@Composable
private fun ErrorHandler(
    error: DashboardError?,
    snackbarHostState: SnackbarHostState,
    onErrorDismiss: (() -> Unit)? = null,
    onActionPerformed: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    LaunchedEffect(error ?: true) {
        val errorMessage = when (error) {
            is DashboardError.InAppPurchaseNoticeError -> error.message ?: context.getString(R.string.app_generic_error)

            is DashboardError.InAppPurchaseConfirmationFailed ->
                context.getString(R.string.wallet_in_app_purchase_error_confirmation_failed)

            null -> return@LaunchedEffect
        }

        val result = snackbarHostState.showSnackbar(
            message = errorMessage,
            duration = SnackbarDuration.Indefinite,
            withDismissAction = true,
        )

        when (result) {
            SnackbarResult.Dismissed -> onErrorDismiss?.invoke()
            SnackbarResult.ActionPerformed -> onActionPerformed?.invoke()
        }
    }
}
