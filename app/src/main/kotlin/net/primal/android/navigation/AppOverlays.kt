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
import net.primal.android.wallet.notice.sheet.WalletNoticeBottomSheet
import net.primal.android.wallet.notice.sheet.WalletNoticeSheetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppOverlays(
    onRemoteSessionClick: () -> Unit,
    onUpgradeWalletClick: () -> Unit,
    onWalletFaqClick: () -> Unit,
    onRestoreWalletClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val connectionIndicatorViewModel = hiltViewModel<ConnectionIndicatorViewModel>()
    val remoteSessionIndicatorViewModel = hiltViewModel<RemoteSessionIndicatorViewModel>()
    val permissionsViewModel = hiltViewModel<PermissionsViewModel>()
    val walletNoticeSheetViewModel = hiltViewModel<WalletNoticeSheetViewModel>()
    val connectionState by connectionIndicatorViewModel.state.collectAsState()

    WalletNoticeBottomSheet(
        viewModel = walletNoticeSheetViewModel,
        onUpgradeClick = onUpgradeWalletClick,
        onFaqClick = onWalletFaqClick,
        onRestoreWalletClick = onRestoreWalletClick,
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
