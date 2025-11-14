package net.primal.android.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import net.primal.android.core.compose.connectionindicator.ConnectionIndicatorOverlay
import net.primal.android.core.compose.connectionindicator.ConnectionIndicatorViewModel
import net.primal.android.core.compose.session.RemoteSessionIndicatorOverlay
import net.primal.android.core.compose.session.RemoteSessionIndicatorViewModel
import net.primal.android.nostrconnect.permissions.PermissionsBottomSheet
import net.primal.android.nostrconnect.permissions.PermissionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppOverlays(onRemoteSessionClick: () -> Unit, content: @Composable () -> Unit) {
    val connectionIndicatorViewModel = hiltViewModel<ConnectionIndicatorViewModel>()
    val remoteSessionIndicatorViewModel = hiltViewModel<RemoteSessionIndicatorViewModel>()
    val permissionsViewModel = hiltViewModel<PermissionsViewModel>()
    val connectionState by connectionIndicatorViewModel.state.collectAsState()

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
