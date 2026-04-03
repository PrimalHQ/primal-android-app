package net.primal.android.main.wallet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.FormatStyle
import net.primal.android.R
import net.primal.android.core.activity.LocalPrimalTheme
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.InvisibleAppBarIcon
import net.primal.android.core.compose.NavigationBarFullHeightDp
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.icons.primaliconpack.WalletPurchaseSats
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.compose.pulltorefresh.PrimalPullToRefreshBox
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.core.utils.formatToDefaultDateFormat
import net.primal.android.core.utils.isGoogleBuild
import net.primal.android.main.wallet.WalletDashboardContract.UiEvent
import net.primal.android.main.wallet.WalletDashboardContract.WalletDashboardState
import net.primal.android.navigation.navigateToProfile
import net.primal.android.navigation.navigateToTransactionDetails
import net.primal.android.navigation.navigateToWalletBackup
import net.primal.android.navigation.navigateToWalletPicker
import net.primal.android.navigation.navigateToWalletReceive
import net.primal.android.navigation.navigateToWalletRestore
import net.primal.android.navigation.navigateToWalletSendPayment
import net.primal.android.navigation.navigateToWalletUpgrade
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.dashboard.ui.WalletAction
import net.primal.android.wallet.dashboard.ui.WalletCallToActionAnnotatedBox
import net.primal.android.wallet.dashboard.ui.WalletCallToActionBox
import net.primal.android.wallet.dashboard.ui.WalletDashboard
import net.primal.android.wallet.dashboard.ui.WalletDashboardLite
import net.primal.android.wallet.dashboard.ui.WalletSetupCallToAction
import net.primal.android.wallet.transactions.list.TransactionListItemDataUi
import net.primal.android.wallet.transactions.list.TransactionsLazyColumn
import net.primal.android.wallet.transactions.send.prepare.tabs.SendPaymentTab
import net.primal.domain.utils.isConfigured
import net.primal.domain.wallet.CurrencyMode
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.capabilities

private val DATE_OF_WALLET_EXPIRATION = LocalDate.of(2026, 4, 30)
    .atTime(12, 0)
    .toInstant(ZoneOffset.UTC)

@Composable
internal fun WalletDashboardContent(
    currencyMode: CurrencyMode,
    topBarHeight: Int,
    topBarFooterHeight: Int,
    onScrolledToTopChanged: (Boolean) -> Unit,
    paddingValues: PaddingValues,
    navController: NavController,
) {
    val walletViewModel = hiltViewModel<WalletDashboardViewModel>()
    val walletState by walletViewModel.state.collectAsState()

    DisposableLifecycleObserverEffect(walletViewModel) {
        when (it) {
            Lifecycle.Event.ON_START -> {
                if (walletState.wallet?.capabilities?.supportsBalanceSubscription != true) {
                    walletViewModel.setEvents(UiEvent.RequestWalletBalanceUpdate)
                }
                walletViewModel.setEvents(UiEvent.RequestLatestTransactionsSync)
                walletViewModel.setEvents(UiEvent.EnrichTransactions)
            }

            else -> Unit
        }
    }

    @Suppress("SimplifyBooleanWithConstants", "KotlinConstantConditions")
    val canBuySats = remember(walletState.wallet) {
        isGoogleBuild() && walletState.wallet is Wallet.Primal && false
    }

    val pagingItems = walletState.transactions.collectAsLazyPagingItems()
    val listState = pagingItems.rememberLazyListStatePagingWorkaround()

    val isScrolledToTop by remember(listState) {
        derivedStateOf { listState.firstVisibleItemScrollOffset == 0 }
    }

    LaunchedEffect(isScrolledToTop) {
        onScrolledToTopChanged(isScrolledToTop)
    }

    val dashboardLiteHeightDp = 80.dp
    val density = LocalDensity.current

    var shouldAddFooter by remember { mutableStateOf(false) }
    LaunchedEffect(pagingItems.itemCount, listState) {
        if (listState.canScrollForward) {
            shouldAddFooter = true
        }
        if (listState.firstVisibleItemScrollOffset == 0) {
            listState.animateScrollToItem(0)
        }
    }

    WalletDashboardContent(
        state = walletState,
        pagingItems = pagingItems,
        listState = listState,
        canBuySats = canBuySats,
        currencyMode = currencyMode,
        topBarHeight = topBarHeight,
        topBarFooterHeight = topBarFooterHeight,
        dashboardLiteHeightDp = dashboardLiteHeightDp,
        shouldAddFooter = shouldAddFooter,
        density = density,
        paddingValues = paddingValues,
        eventPublisher = walletViewModel::setEvents,
        onProfileClick = { profileId -> navController.navigateToProfile(profileId) },
        onTransactionClick = { txId -> navController.navigateToTransactionDetails(txId) },
        onUpgradeWalletClick = { navController.navigateToWalletUpgrade() },
        onWalletBackupClick = { walletId -> navController.navigateToWalletBackup(walletId) },
        onRestoreWalletClick = { navController.navigateToWalletRestore() },
        onBuySatsClick = {},
    )
}

@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
private fun WalletDashboardContent(
    state: WalletDashboardContract.UiState,
    pagingItems: LazyPagingItems<TransactionListItemDataUi>,
    listState: LazyListState,
    canBuySats: Boolean,
    currencyMode: CurrencyMode,
    topBarHeight: Int,
    topBarFooterHeight: Int,
    dashboardLiteHeightDp: Dp,
    shouldAddFooter: Boolean,
    density: Density,
    paddingValues: PaddingValues,
    eventPublisher: (UiEvent) -> Unit,
    onProfileClick: (String) -> Unit,
    onTransactionClick: (String) -> Unit,
    onUpgradeWalletClick: () -> Unit,
    onWalletBackupClick: (String) -> Unit,
    onRestoreWalletClick: () -> Unit,
    onBuySatsClick: () -> Unit,
) {
    PrimalPullToRefreshBox(
        isRefreshing = state.refreshing,
        onRefresh = {
            eventPublisher(UiEvent.RequestLatestTransactionsSync)
            eventPublisher(UiEvent.RequestWalletBalanceUpdate)
        },
        enabled = state.wallet != null,
        indicatorPaddingValues = paddingValues,
    ) {
        when (state.dashboardState) {
            WalletDashboardState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            WalletDashboardState.NoWalletNpubLogin -> {
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

            WalletDashboardState.WalletDetected -> {
                WalletSetupCallToAction(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 32.dp)
                        .padding(bottom = 32.dp)
                        .navigationBarsPadding(),
                    title = stringResource(id = R.string.wallet_dashboard_detected_title),
                    description = stringResource(id = R.string.wallet_dashboard_detected_description),
                    onRestoreWalletClick = onRestoreWalletClick,
                    onCreateWalletClick = { eventPublisher(UiEvent.CreateWallet) },
                )
            }

            WalletDashboardState.WalletDiscontinued -> {
                WalletSetupCallToAction(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 32.dp)
                        .padding(bottom = 32.dp)
                        .navigationBarsPadding(),
                    title = stringResource(id = R.string.wallet_dashboard_discontinued_title),
                    description = stringResource(id = R.string.wallet_dashboard_discontinued_description),
                    onRestoreWalletClick = onRestoreWalletClick,
                    onCreateWalletClick = { eventPublisher(UiEvent.CreateWallet) },
                )
            }

            WalletDashboardState.ActiveWallet -> {
                val isTransactionListSettled = pagingItems.loadState.refresh is LoadState.NotLoading &&
                    pagingItems.loadState.append is LoadState.NotLoading &&
                    !state.refreshing

                if (isTransactionListSettled && pagingItems.isEmpty()) {
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

                        if (state.wallet?.balanceInBtc == 0.0) {
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
                                onActionClick = onBuySatsClick,
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
                                        .padding(top = 16.dp, bottom = 12.dp),
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
                                    onActionClick = { state.wallet?.let { onWalletBackupClick(it.walletId) } },
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
                                    onActionClick = onBuySatsClick,
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

            WalletDashboardState.NoWallet -> {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WalletDashboardTopAppBar(
    isScrolledToTop: Boolean,
    currencyMode: CurrencyMode,
    scrollBehavior: TopAppBarScrollBehavior?,
    onTopBarHeightChanged: (Int) -> Unit,
    onTopBarFooterHeightChanged: (Int) -> Unit,
    onCurrencyModeToggle: (CurrencyMode) -> Unit,
    onAvatarClick: () -> Unit,
    navController: NavController,
) {
    val walletViewModel = hiltViewModel<WalletDashboardViewModel>()
    val walletState by walletViewModel.state.collectAsState()
    val canShowWalletPicker = walletState.walletPickerEnabled && walletState.wallet != null

    val dashboardExpanded by rememberSaveable(isScrolledToTop) {
        mutableStateOf(isScrolledToTop)
    }

    @Suppress("SimplifyBooleanWithConstants", "KotlinConstantConditions")
    val canBuySats = remember(walletState.wallet) {
        isGoogleBuild() && walletState.wallet is Wallet.Primal && false
    }

    WalletDashboardTopAppBar(
        state = walletState,
        canShowWalletPicker = canShowWalletPicker,
        canBuySats = canBuySats,
        dashboardExpanded = dashboardExpanded,
        dashboardLiteHeightDp = 80.dp,
        currencyMode = currencyMode,
        scrollBehavior = scrollBehavior,
        onTopBarHeightChanged = onTopBarHeightChanged,
        onTopBarFooterHeightChanged = onTopBarFooterHeightChanged,
        onCurrencyModeToggle = onCurrencyModeToggle,
        onAvatarClick = onAvatarClick,
        onWalletPickerClick = { navController.navigateToWalletPicker() },
        onSendClick = { navController.navigateToWalletSendPayment(tab = SendPaymentTab.Nostr) },
        onScanClick = { navController.navigateToWalletSendPayment(tab = SendPaymentTab.Scan) },
        onReceiveClick = { navController.navigateToWalletReceive() },
        onBuySatsClick = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletDashboardTopAppBar(
    state: WalletDashboardContract.UiState,
    canShowWalletPicker: Boolean,
    canBuySats: Boolean,
    dashboardExpanded: Boolean,
    dashboardLiteHeightDp: Dp,
    currencyMode: CurrencyMode,
    scrollBehavior: TopAppBarScrollBehavior?,
    onTopBarHeightChanged: (Int) -> Unit,
    onTopBarFooterHeightChanged: (Int) -> Unit,
    onCurrencyModeToggle: (CurrencyMode) -> Unit,
    onAvatarClick: () -> Unit,
    onWalletPickerClick: () -> Unit,
    onSendClick: () -> Unit,
    onScanClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onBuySatsClick: () -> Unit,
) {
    PrimalTopAppBar(
        modifier = Modifier.onSizeChanged { onTopBarHeightChanged(it.height) },
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
        onTitleClick = if (canShowWalletPicker) {
            { onWalletPickerClick() }
        } else {
            null
        },
        navigationIcon = PrimalIcons.AvatarDefault,
        onNavigationIconClick = onAvatarClick,
        actions = {
            if (canBuySats) {
                AppBarIcon(
                    icon = PrimalIcons.WalletPurchaseSats,
                    onClick = onBuySatsClick,
                )
            } else {
                InvisibleAppBarIcon()
            }
        },
        scrollBehavior = scrollBehavior,
        showDivider = !LocalPrimalTheme.current.isDarkTheme,
        footer = {
            if (state.dashboardState == WalletDashboardState.ActiveWallet) {
                AnimatedContent(
                    modifier = Modifier.onSizeChanged { onTopBarFooterHeightChanged(it.height) },
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
                            enabled = state.wallet.isConfigured(),
                            actions = listOf(WalletAction.Send, WalletAction.Scan, WalletAction.Receive),
                            onWalletAction = { action ->
                                when (action) {
                                    WalletAction.Send -> onSendClick()
                                    WalletAction.Scan -> onScanClick()
                                    WalletAction.Receive -> onReceiveClick()
                                }
                            },
                            currencyMode = currencyMode,
                            onSwitchCurrencyMode = onCurrencyModeToggle,
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
                            enabled = state.wallet.isConfigured(),
                            onSwitchCurrencyMode = onCurrencyModeToggle,
                            exchangeBtcUsdRate = state.exchangeBtcUsdRate,
                        )
                    }
                }
            }
        },
    )
}
