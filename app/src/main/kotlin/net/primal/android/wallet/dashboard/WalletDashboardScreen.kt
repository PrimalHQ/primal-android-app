package net.primal.android.wallet.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalBottomBarHeightDp
import net.primal.android.drawer.PrimalDrawerScaffold
import net.primal.android.theme.AppTheme

@Composable
fun WalletDashboardScreen(
    viewModel: WalletDashboardViewModel,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    onWalletActivateClick: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    WalletDashboardScreen(
        state = uiState.value,
        onPrimaryDestinationChanged = onPrimaryDestinationChanged,
        onDrawerDestinationClick = onDrawerDestinationClick,
        onWalletActivateClick = onWalletActivateClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletDashboardScreen(
    state: WalletDashboardContract.UiState,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    onWalletActivateClick: () -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)

    val bottomBarHeight = PrimalBottomBarHeightDp
    var bottomBarOffsetHeightPx by remember { mutableFloatStateOf(0f) }

    PrimalDrawerScaffold(
        drawerState = drawerState,
        activeDestination = PrimalTopLevelDestination.Wallet,
        onActiveDestinationClick = { uiScope.launch { /* TODO Scroll to top */ } },
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
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                ActivateWalletNotice(
                    modifier = Modifier.wrapContentSize(align = Alignment.Center),
                    onActivateClick = onWalletActivateClick,
                )
            }
        },
    )
}

@Composable
private fun ActivateWalletNotice(modifier: Modifier, onActivateClick: () -> Unit) {
    Column(
        modifier = modifier,
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
