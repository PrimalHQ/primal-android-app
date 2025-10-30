package net.primal.android.settings.connected.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.domain.account.model.AppConnection

@Composable
fun ConnectedAppDetailsScreen(viewModel: ConnectedAppDetailsViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()
    ConnectedAppDetailsScreen(
        state = uiState.value,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectedAppDetailsScreen(state: ConnectedAppDetailsContract.UiState, onClose: () -> Unit) {
    PrimalScaffold(
        topBar = {
            PrimalTopAppBar(
                title = state.connection?.name ?: stringResource(id = R.string.settings_connected_app_details_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
    ) { paddingValues ->
        if (state.loading) {
            PrimalLoadingSpinner()
        } else if (state.connection != null) {
            ConnectedAppDetailsContent(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                connection = state.connection,
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(id = R.string.settings_connected_app_details_not_found),
                )
            }
        }
    }
}

@Composable
fun ConnectedAppDetailsContent(modifier: Modifier = Modifier, connection: AppConnection) {
    Column(modifier = modifier) {
        Text(text = stringResource(id = R.string.settings_connected_app_details_for_app, connection.name ?: ""))
        Text(text = stringResource(id = R.string.settings_connected_app_details_permissions_and_sessions))
    }
}
