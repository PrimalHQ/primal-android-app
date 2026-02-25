package net.primal.android.wallet.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.FormatStyle
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.activity.LocalPrimalTheme
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.InvisibleAppBarIcon
import net.primal.android.core.compose.NavigationBarFullHeightDp
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.icons.primaliconpack.WalletPurchaseSats
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.compose.pulltorefresh.PrimalPullToRefreshBox
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.core.utils.formatToDefaultDateFormat
import net.primal.android.core.utils.isGoogleBuild
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalDrawerScaffold
import net.primal.android.drawer.multiaccount.events.AccountSwitcherCallbacks
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.dashboard.WalletDashboardContract.UiEvent
import net.primal.android.wallet.dashboard.WalletDashboardContract.UiState.DashboardError
import net.primal.android.wallet.dashboard.ui.WalletAction
import net.primal.android.wallet.dashboard.ui.WalletCallToActionAnnotatedBox
import net.primal.android.wallet.dashboard.ui.WalletCallToActionBox
import net.primal.android.wallet.dashboard.ui.WalletDashboard
import net.primal.android.wallet.dashboard.ui.WalletDashboardLite
import net.primal.android.wallet.dashboard.ui.WalletPickerBottomSheet
import net.primal.android.wallet.store.inapp.InAppPurchaseBuyBottomSheet
import net.primal.android.wallet.transactions.list.TransactionsLazyColumn
import net.primal.domain.utils.isConfigured
import net.primal.domain.wallet.CurrencyMode
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.capabilities

private val DATE_OF_WALLET_EXPIRATION = LocalDate.of(2026, 4, 30)
    .atTime(12, 0)
    .toInstant(ZoneOffset.UTC)

@Composable
fun WalletDashboardScreen(
    viewModel: WalletDashboardViewModel,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    onDrawerQrCodeClick: () -> Unit,
    onWalletBackupClick: (String) -> Unit,
    onUpgradeWalletClick: () -> Unit,
    onProfileClick: (String) -> Unit,
    onTransactionClick: (String) -> Unit,
    onSendClick: () -> Unit,
    onScanClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onConfigureWalletsClick: () -> Unit,
    accountSwitcherCallbacks: AccountSwitcherCallbacks,
) {
    val uiState = viewModel.state.collectAsState()

    DisposableLifecycleObserverEffect(viewModel) {
        when (it) {
            Lifecycle.Event.ON_START -> {
                if (uiState.value.wallet?.capabilities?.supportsBalanceSubscription != true) {
                    viewModel.setEvents(UiEvent.RequestWalletBalanceUpdate)
                }
            }

            else -> Unit
        }
    }

    WalletDashboardScreen(
        state = uiState.value,
        onPrimaryDestinationChanged = onPrimaryDestinationChanged,
        onDrawerDestinationClick = onDrawerDestinationClick,
        onDrawerQrCodeClick = onDrawerQrCodeClick,
        onUpgradeWalletClick = onUpgradeWalletClick,
        onWalletBackupClick = onWalletBackupClick,
        onProfileClick = onProfileClick,
        onTransactionClick = onTransactionClick,
        onSendClick = onSendClick,
        onScanClick = onScanClick,
        onReceiveClick = onReceiveClick,
        onConfigureWalletsClick = onConfigureWalletsClick,
        eventPublisher = { viewModel.setEvents(it) },
        accountSwitcherCallbacks = accountSwitcherCallbacks,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WalletDashboardScreen(
    state: WalletDashboardContract.UiState,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    onDrawerQrCodeClick: () -> Unit,
    onWalletBackupClick: (String) -> Unit,
    onUpgradeWalletClick: () -> Unit,
    onProfileClick: (String) -> Unit,
    onTransactionClick: (String) -> Unit,
    onSendClick: () -> Unit,
    onScanClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onConfigureWalletsClick: () -> Unit,
    eventPublisher: (UiEvent) -> Unit,
    accountSwitcherCallbacks: AccountSwitcherCallbacks,
) {
    val uiScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)

    val pagingItems = state.transactions.collectAsLazyPagingItems()
    val listState = pagingItems.rememberLazyListStatePagingWorkaround()

    val canShowWalletPicker = state.userWallets.isNotEmpty() && state.userWallets.size > 1 && state.wallet != null
    var walletPickerVisible by remember { mutableStateOf(false) }
    if (walletPickerVisible && canShowWalletPicker) {
        WalletPickerBottomSheet(
            wallets = state.userWallets,
            activeWallet = state.wallet,
            onDismissRequest = { walletPickerVisible = false },
            onConfigureWalletsClick = onConfigureWalletsClick,
            onActiveWalletChanged = {
                eventPublisher(UiEvent.ChangeActiveWallet(it))
            },
        )
    }

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

                is DashboardError.WalletCreationFailed ->
                    context.getString(R.string.wallet_dashboard_create_wallet_error)
            }
        },
        onErrorDismiss = { eventPublisher(UiEvent.DismissError) },
    )

    @Suppress("SimplifyBooleanWithConstants", "KotlinConstantConditions")
    val canBuySats = remember(state.wallet) { isGoogleBuild() && state.wallet is Wallet.Primal && false }

    val isScrolledToTop by remember(listState) { derivedStateOf { listState.firstVisibleItemScrollOffset == 0 } }
    val dashboardExpanded by rememberSaveable(isScrolledToTop) { mutableStateOf(isScrolledToTop) }
    val dashboardLiteHeightDp = 80.dp
    var topBarHeight by remember { mutableIntStateOf(0) }
    var topBarFooterHeight by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    var currencyMode by rememberSaveable { mutableStateOf(CurrencyMode.SATS) }

    var shouldAddFooter by remember { mutableStateOf(false) }
    LaunchedEffect(pagingItems.itemCount, listState) {
        if (listState.canScrollForward) {
            shouldAddFooter = true
        }

        if (listState.firstVisibleItemScrollOffset == 0) {
            listState.animateScrollToItem(0)
        }
    }

    PrimalDrawerScaffold(
        drawerState = drawerState,
        activeDestination = PrimalTopLevelDestination.Wallet,
        onActiveDestinationClick = { uiScope.launch { listState.animateScrollToItem(0) } },
        onPrimaryDestinationChanged = onPrimaryDestinationChanged,
        onDrawerDestinationClick = onDrawerDestinationClick,
        onDrawerQrCodeClick = onDrawerQrCodeClick,
        badges = state.badges,
        focusModeEnabled = false,
        accountSwitcherCallbacks = accountSwitcherCallbacks,
        topAppBar = { scrollBehaviour ->
            PrimalTopAppBar(
                modifier = Modifier.onSizeChanged { topBarHeight = it.height },
                title = when (state.wallet) {
                    is Wallet.NWC -> stringResource(id = R.string.wallet_nwc_title)
                    is Wallet.Primal -> stringResource(id = R.string.wallet_primal_title)
                    is Wallet.Spark -> stringResource(id = R.string.wallet_spark_title)
                    null -> stringResource(id = R.string.wallet_title)
                },
                avatarCdnImage = state.activeAccountAvatarCdnImage,
                legendaryCustomization = state.activeAccountLegendaryCustomization,
                avatarBlossoms = state.activeAccountBlossoms,
                titleTrailingIcon = if (canShowWalletPicker) {
                    Icons.Default.ExpandMore
                } else {
                    null
                },
                onTitleClick = { walletPickerVisible = true },
                navigationIcon = PrimalIcons.AvatarDefault,
                onNavigationIconClick = {
                    uiScope.launch { drawerState.open() }
                },
                actions = {
                    if (canBuySats) {
                        AppBarIcon(
                            icon = PrimalIcons.WalletPurchaseSats,
                            onClick = {
                                inAppPurchaseVisible = true
                            },
                        )
                    } else {
                        InvisibleAppBarIcon()
                    }
                },
                scrollBehavior = scrollBehaviour,
                showDivider = !LocalPrimalTheme.current.isDarkTheme,
                footer = {
                    AnimatedContent(
                        modifier = Modifier.onSizeChanged { topBarFooterHeight = it.height },
                        targetState = dashboardExpanded,
                        label = "DashboardAnimation",
                    ) { expanded ->
                        when (expanded) {
                            true -> WalletDashboard(
                                modifier = Modifier
                                    .wrapContentSize(align = Alignment.Center)
                                    .padding(horizontal = 32.dp)
                                    .padding(top = 16.dp, bottom = 24.dp)
                                    .animateContentSize(),
                                walletBalance = state.wallet?.balanceInBtc?.toBigDecimal(),
                                enabled = state.wallet.isConfigured() && !state.isNpubLogin,
                                actions = listOf(WalletAction.Send, WalletAction.Scan, WalletAction.Receive),
                                onWalletAction = { action ->
                                    when (action) {
                                        WalletAction.Send -> onSendClick()
                                        WalletAction.Scan -> onScanClick()
                                        WalletAction.Receive -> onReceiveClick()
                                    }
                                },
                                currencyMode = currencyMode,
                                onSwitchCurrencyMode = { currencyMode = it },
                                exchangeBtcUsdRate = state.exchangeBtcUsdRate,
                            )

                            false -> WalletDashboardLite(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(dashboardLiteHeightDp)
                                    .background(color = AppTheme.colorScheme.surface)
                                    .padding(horizontal = 10.dp, vertical = 16.dp)
                                    .animateContentSize(),
                                walletBalance = state.wallet?.balanceInBtc?.toBigDecimal(),
                                actions = listOf(WalletAction.Send, WalletAction.Scan, WalletAction.Receive),
                                onWalletAction = { action ->
                                    when (action) {
                                        WalletAction.Send -> onSendClick()
                                        WalletAction.Scan -> onScanClick()
                                        WalletAction.Receive -> onReceiveClick()
                                    }
                                },
                                currencyMode = currencyMode,
                                enabled = state.wallet.isConfigured() && !state.isNpubLogin,
                                onSwitchCurrencyMode = { currencyMode = it },
                                exchangeBtcUsdRate = state.exchangeBtcUsdRate,
                            )
                        }
                    }
                },
            )
        },
        content = { paddingValues ->
            PrimalPullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = {
                    eventPublisher(UiEvent.RequestLatestTransactionsSync)
                    eventPublisher(UiEvent.RequestWalletBalanceUpdate)
                },
                enabled = state.wallet != null,
                indicatorPaddingValues = paddingValues,
            ) {
                when {
                    state.isNpubLogin -> {
                        WalletCallToActionBox(
                            modifier = Modifier
                                .fillMaxSize()
                                .animateContentSize()
                                .padding(paddingValues)
                                .padding(horizontal = 32.dp)
                                .padding(bottom = 32.dp)
                                .navigationBarsPadding(),
                            message = stringResource(id = R.string.app_npub_login_error),
                        )
                    }

                    state.wallet != null -> {
                        if (
                            pagingItems.loadState.refresh is LoadState.NotLoading &&
                            pagingItems.isEmpty() &&
                            !state.refreshing
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                                    .navigationBarsPadding(),
                            ) {
                                if (state.wallet is Wallet.Primal) {
                                    DashboardUpgradeNotice(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .animateContentSize()
                                            .padding(horizontal = 32.dp)
                                            .padding(vertical = 16.dp),
                                        onUpgradeWalletClick = onUpgradeWalletClick,
                                    )
                                }

                                if (state.wallet.balanceInBtc == 0.0) {
                                    WalletCallToActionBox(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .animateContentSize()
                                            .padding(horizontal = 32.dp)
                                            .padding(bottom = 32.dp),
                                        message = stringResource(id = R.string.wallet_dashboard_no_sats_hint),
                                        actionLabel = if (canBuySats) {
                                            stringResource(id = R.string.wallet_dashboard_buy_sats_button)
                                        } else {
                                            null
                                        },
                                        onActionClick = {
                                            inAppPurchaseVisible = true
                                        },
                                    )
                                } else {
                                    WalletCallToActionBox(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .animateContentSize()
                                            .padding(horizontal = 32.dp)
                                            .padding(bottom = 32.dp),
                                        message = stringResource(id = R.string.wallet_dashboard_no_transactions_hint),
                                    )
                                }
                            }
                        } else {
                            TransactionsLazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = AppTheme.colorScheme.surfaceVariant)
                                    .padding(top = with(LocalDensity.current) { topBarHeight.toDp() }),
                                pagingItems = pagingItems,
                                isRefreshing = state.refreshing,
                                currencyMode = currencyMode,
                                exchangeBtcUsdRate = state.exchangeBtcUsdRate,
                                listState = listState,
                                onProfileClick = onProfileClick,
                                onTransactionClick = onTransactionClick,
                                header = {
                                    if (state.wallet is Wallet.Primal) {
                                        DashboardUpgradeNotice(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .animateContentSize()
                                                .padding(horizontal = 32.dp)
                                                .padding(bottom = 12.dp),
                                            onUpgradeWalletClick = onUpgradeWalletClick,
                                        )
                                    } else if (!state.isWalletBackedUp) {
                                        val titleText = stringResource(
                                            id = R.string.wallet_dashboard_backup_notice_title,
                                        )
                                        val descriptionText = stringResource(
                                            id = R.string.wallet_dashboard_backup_notice_description,
                                        )

                                        val annotatedMessage = buildAnnotatedString {
                                            withStyle(
                                                SpanStyle(
                                                    fontWeight = FontWeight.Bold,
                                                    textDecoration = TextDecoration.Underline,
                                                ),
                                            ) {
                                                append(titleText.uppercase())
                                            }
                                            append(" ")
                                            append(descriptionText)
                                        }

                                        WalletCallToActionAnnotatedBox(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp)
                                                .padding(bottom = 16.dp)
                                                .animateContentSize(),
                                            message = annotatedMessage,
                                            actionLabel = stringResource(id = R.string.wallet_dashboard_backup_button),
                                            onActionClick = { onWalletBackupClick(state.wallet.walletId) },
                                        )
                                    } else if (state.lowBalance && pagingItems.itemCount > 0 && canBuySats) {
                                        WalletCallToActionBox(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .animateContentSize()
                                                .padding(horizontal = 32.dp)
                                                .padding(bottom = 32.dp),
                                            message = stringResource(id = R.string.wallet_dashboard_low_sats_hint),
                                            actionLabel = stringResource(
                                                id = R.string.wallet_dashboard_buy_sats_button,
                                            ),
                                            onActionClick = {
                                                inAppPurchaseVisible = true
                                            },
                                        )
                                    }
                                },
                                footer = {
                                    if (shouldAddFooter) {
                                        val systemNavigationBarDp = 48.dp
                                        val spacerHeight = with(density) {
                                            (topBarHeight - topBarFooterHeight).toDp() + dashboardLiteHeightDp +
                                                NavigationBarFullHeightDp + systemNavigationBarDp
                                        }
                                        Spacer(modifier = Modifier.height(spacerHeight))
                                    }
                                },
                            )
                        }
                    }

                    else -> {
                        WalletCallToActionBox(
                            modifier = Modifier
                                .fillMaxSize()
                                .animateContentSize()
                                .padding(paddingValues)
                                .padding(horizontal = 32.dp)
                                .padding(bottom = 32.dp)
                                .navigationBarsPadding(),
                            message = stringResource(
                                id = if (state.hasPersistedSparkWallet) {
                                    R.string.wallet_dashboard_activate_wallet_hint
                                } else {
                                    R.string.wallet_dashboard_create_wallet_hint
                                },
                            ),
                            actionLabel = stringResource(
                                id = if (state.hasPersistedSparkWallet) {
                                    R.string.wallet_dashboard_activate_wallet_button
                                } else {
                                    R.string.wallet_dashboard_create_wallet_button
                                },
                            ),
                            onActionClick = { eventPublisher(UiEvent.CreateWallet) },
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

@Composable
private fun DashboardUpgradeNotice(onUpgradeWalletClick: () -> Unit, modifier: Modifier = Modifier) {
    WalletCallToActionAnnotatedBox(
        modifier = modifier,
        message = buildAnnotatedString {
            withStyle(
                AppTheme.typography.bodyMedium.toSpanStyle()
                    .copy(color = AppTheme.extraColorScheme.onSurfaceVariantAlt2),
            ) {
                withStyle(
                    SpanStyle(
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Bold,
                    ),
                ) {
                    append(
                        stringResource(id = R.string.wallet_dashboard_upgrade_header),
                    )
                }
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    appendLine(":")
                }
                append(
                    stringResource(id = R.string.wallet_dashboard_upgrade_expires_on),
                )
                append(" ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(
                        DATE_OF_WALLET_EXPIRATION
                            .formatToDefaultDateFormat(FormatStyle.LONG),
                    )
                }
                appendLine(".")
                append(
                    stringResource(
                        id = R.string.wallet_dashboard_upgrade_please_upgrade,
                    ),
                )
            }
        },
        actionLabel = stringResource(id = R.string.wallet_dashboard_upgrade_button),
        onActionClick = onUpgradeWalletClick,
    )
}
