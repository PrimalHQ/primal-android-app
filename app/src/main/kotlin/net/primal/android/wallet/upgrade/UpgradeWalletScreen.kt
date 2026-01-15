package net.primal.android.wallet.upgrade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.ui.FlowStatusColumn
import net.primal.android.wallet.upgrade.ui.UpgradeWalletFailed
import net.primal.android.wallet.upgrade.ui.UpgradeWalletSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeWalletScreen(viewModel: UpgradeWalletViewModel, onClose: () -> Unit) {
    val state = viewModel.state.collectAsState()

    UpgradeWalletScreen(
        state = state.value,
        onClose = onClose,
    )
}

@ExperimentalMaterial3Api
@Composable
fun UpgradeWalletScreen(state: UpgradeWalletContract.UiState, onClose: () -> Unit) {
    PrimalScaffold(
        topBar = {
            if (state.status != UpgradeWalletStatus.Success) {
                PrimalTopAppBar(
                    title = when (state.status) {
                        UpgradeWalletStatus.Upgrading -> stringResource(id = R.string.wallet_upgrade_upgrading_title)
                        UpgradeWalletStatus.Failed -> stringResource(id = R.string.wallet_upgrade_failed_title)
                        else -> ""
                    },
                    navigationIcon = PrimalIcons.ArrowBack,
                    navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                    showDivider = false,
                    onNavigationIconClick = onClose,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AppTheme.colorScheme.surfaceVariant,
                        scrolledContainerColor = AppTheme.colorScheme.surfaceVariant,
                    ),
                )
            }
        },
        content = { paddingValues ->
            when (state.status) {
                UpgradeWalletStatus.Upgrading -> {
                    FlowStatusColumn(
                        modifier = Modifier
                            .padding(paddingValues)
                            .padding(bottom = 80.dp)
                            .background(color = AppTheme.colorScheme.surfaceVariant)
                            .fillMaxSize(),
                        icon = null,
                        headlineText = null,
                        supportText = stringResource(id = R.string.wallet_upgrade_upgrading_support),
                    )
                }

                UpgradeWalletStatus.Success -> {
                    UpgradeWalletSuccess(
                        modifier = Modifier.fillMaxSize(),
                        onDoneClick = onClose,
                    )
                }

                UpgradeWalletStatus.Failed -> {
                    UpgradeWalletFailed(
                        modifier = Modifier
                            .padding(paddingValues)
                            .background(color = AppTheme.colorScheme.surfaceVariant)
                            .fillMaxSize(),
                        errorMessage = state.error?.message
                            ?: stringResource(id = R.string.app_generic_error),
                        onCloseClick = onClose,
                    )
                }
            }
        },
    )
}
