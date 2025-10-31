package net.primal.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import net.primal.android.core.compose.connectionindicator.ConnectionIndicatorOverlay
import net.primal.android.core.compose.connectionindicator.ConnectionIndicatorViewModel
import net.primal.android.core.compose.session.RemoteSessionIndicatorOverlay
import net.primal.android.core.compose.session.RemoteSessionIndicatorViewModel

@Composable
fun AppOverlays(content: @Composable () -> Unit) {
    val connectionIndicatorViewModel = hiltViewModel<ConnectionIndicatorViewModel>()
    val remoteSessionIndicatorViewModel = hiltViewModel<RemoteSessionIndicatorViewModel>()
    val connectionState by connectionIndicatorViewModel.state.collectAsState()

    ConnectionIndicatorOverlay(viewModel = connectionIndicatorViewModel) {
        RemoteSessionIndicatorOverlay(
            viewModel = remoteSessionIndicatorViewModel,
            isNetworkUnavailable = !connectionState.hasConnection,
            content = content,
        )
    }
}
