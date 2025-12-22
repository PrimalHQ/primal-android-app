package net.primal.android.settings.connected.permissions.remote

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.primal.android.settings.connected.permissions.ConnectedAppPermissionsScreen
import net.primal.android.settings.connected.permissions.remote.RemoteAppPermissionsContract.UiEvent
import net.primal.android.settings.connected.ui.ConnectedAppHeader

@Composable
fun RemoteAppPermissionsScreen(viewModel: RemoteAppPermissionsViewModel, onClose: () -> Unit) {
    val state = viewModel.state.collectAsState()

    ConnectedAppPermissionsScreen(
        headerContent = {
            ConnectedAppHeader(
                modifier = Modifier.padding(vertical = 16.dp),
                appName = state.value.appName,
                appIconUrl = state.value.appIconUrl,
                startedAt = state.value.appLastSessionAt,
            )
        },
        permissions = state.value.permissions,
        loading = state.value.loading,
        error = state.value.error,
        onUpdatePermission = { ids, action -> viewModel.setEvent(UiEvent.UpdatePermission(ids, action)) },
        onResetPermissions = { viewModel.setEvent(UiEvent.ResetPermissions) },
        onRetry = { viewModel.setEvent(UiEvent.Retry) },
        onDismissError = { viewModel.setEvent(UiEvent.DismissError) },
        onClose = onClose,
    )
}
