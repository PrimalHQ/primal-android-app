package net.primal.android.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.primal.android.core.compose.connectionindicator.ConnectionIndicatorOverlay
import net.primal.android.core.compose.connectionindicator.ConnectionIndicatorViewModel
import net.primal.android.core.compose.session.RemoteSessionIndicatorOverlay
import net.primal.android.core.compose.session.RemoteSessionIndicatorViewModel
import net.primal.android.nostrconnect.approvals.PermissionsBottomSheet
import net.primal.android.nostrconnect.approvals.PermissionsViewModel
import net.primal.android.wallet.upgrade.sheet.UpgradeWalletBottomSheet
import net.primal.android.wallet.upgrade.sheet.UpgradeWalletSheetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppOverlays(
    onRemoteSessionClick: () -> Unit,
    onUpgradeWalletClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val connectionIndicatorViewModel = hiltViewModel<ConnectionIndicatorViewModel>()
    val remoteSessionIndicatorViewModel = hiltViewModel<RemoteSessionIndicatorViewModel>()
    val permissionsViewModel = hiltViewModel<PermissionsViewModel>()
    val upgradeWalletSheetViewModel = hiltViewModel<UpgradeWalletSheetViewModel>()
    val connectionState by connectionIndicatorViewModel.state.collectAsState()

    UpgradeWalletBottomSheet(
        viewModel = upgradeWalletSheetViewModel,
        onUpgradeClick = onUpgradeWalletClick,
    ) {
        PermissionsBottomSheet(viewModel = permissionsViewModel) {
            ConnectionIndicatorOverlay(viewModel = connectionIndicatorViewModel) {
                RemoteSessionIndicatorOverlay(
                    viewModel = remoteSessionIndicatorViewModel,
                    isNetworkUnavailable = !connectionState.hasConnection,
                    onClick = onRemoteSessionClick,
                    content = content,
                )
            }
        }
    }
}
