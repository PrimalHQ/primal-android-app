package net.primal.android.wallet.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import java.math.BigDecimal
import java.text.NumberFormat
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalBottomBarHeightDp
import net.primal.android.drawer.PrimalDrawerScaffold
import net.primal.android.theme.AppTheme
import net.primal.android.user.domain.PrimalWallet
import net.primal.android.user.domain.WalletPreference
import net.primal.android.wallet.dashboard.WalletDashboardContract.UiEvent
import net.primal.android.wallet.domain.WalletKycLevel
import net.primal.android.wallet.transactions.TransactionsLazyColumn
import net.primal.android.wallet.utils.CurrencyConversionUtils.toSats

@Composable
fun WalletDashboardScreen(
    viewModel: WalletDashboardViewModel,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    onWalletActivateClick: () -> Unit,
    onProfileClick: (String) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    WalletDashboardScreen(
        state = uiState.value,
        onPrimaryDestinationChanged = onPrimaryDestinationChanged,
        onDrawerDestinationClick = onDrawerDestinationClick,
        onWalletActivateClick = onWalletActivateClick,
        onProfileClick = onProfileClick,
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
    eventPublisher: (UiEvent) -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)

    val bottomBarHeight = PrimalBottomBarHeightDp
    var bottomBarOffsetHeightPx by remember { mutableFloatStateOf(0f) }

    val pagingItems = state.transactions.collectAsLazyPagingItems()
    val listState = pagingItems.rememberLazyListStatePagingWorkaround()

    PrimalDrawerScaffold(
        drawerState = drawerState,
        activeDestination = PrimalTopLevelDestination.Wallet,
        onActiveDestinationClick = { uiScope.launch { listState.animateScrollToItem(0) } },
        onPrimaryDestinationChanged = onPrimaryDestinationChanged,
        onDrawerDestinationClick = onDrawerDestinationClick,
        bottomBarHeight = bottomBarHeight,
        onBottomBarOffsetChange = { bottomBarOffsetHeightPx = it },
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.wallet_title),
                avatarCdnImage = state.activeAccountAvatarCdnImage,
                navigationIcon = PrimalIcons.AvatarDefault,
                onNavigationIconClick = {
                    uiScope.launch { drawerState.open() }
                },
                scrollBehavior = it,
            )
        },
        content = { paddingValues ->
            if (state.primalWallet != null && state.primalWallet.kycLevel != WalletKycLevel.None) {
                TransactionsLazyColumn(
                    walletBalance = state.walletBalance,
                    primalWallet = state.primalWallet,
                    walletPreference = state.walletPreference,
                    eventPublisher = eventPublisher,
                    pagingItems = pagingItems,
                    listState = listState,
                    paddingValues = paddingValues,
                    onProfileClick = onProfileClick,
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
    )
}

@Composable
fun WalletDashboard(
    walletBalance: BigDecimal?,
    primalWallet: PrimalWallet,
    walletPreference: WalletPreference,
    eventPublisher: (UiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .wrapContentSize(align = Alignment.Center)
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WalletBalanceText(walletBalance)

        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = "Primal wallet is activated.\n" +
                "Primal wallet lightning address is ${primalWallet.lightningAddress}.\n" +
                "Primal KYC level is ${primalWallet.kycLevel}.\n" +
                "Your wallet preference is $walletPreference.",
            textAlign = TextAlign.Center,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            style = AppTheme.typography.bodyMedium,
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            PrimalLoadingButton(
                modifier = Modifier.padding(horizontal = 4.dp),
                text = "Prefer Primal",
                onClick = {
                    eventPublisher(UiEvent.UpdateWalletPreference(WalletPreference.PrimalWallet))
                },
            )

            PrimalLoadingButton(
                modifier = Modifier.padding(horizontal = 4.dp),
                text = "Prefer NWC",
                onClick = {
                    eventPublisher(UiEvent.UpdateWalletPreference(WalletPreference.NostrWalletConnect))
                },
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun WalletBalanceText(walletBalance: BigDecimal?) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = walletBalance?.toSats()?.let { numberFormat.format(it.toLong()) } ?: "⌛",
            textAlign = TextAlign.Center,
            style = AppTheme.typography.displayMedium,
        )

        if (walletBalance != null) {
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = " ${stringResource(id = R.string.wallet_sats_suffix)}",
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
        }
    }
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
