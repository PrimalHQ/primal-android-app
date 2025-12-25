package net.primal.android.settings.connected.event.remote

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.settings.connected.event.EventDetailsContent

@Composable
fun RemoteEventDetailsScreen(viewModel: RemoteEventDetailsViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()
    RemoteEventDetailsScreen(
        state = uiState.value,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteEventDetailsScreen(state: RemoteEventDetailsContract.UiState, onClose: () -> Unit) {
    PrimalScaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_event_details_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            EventDetailsContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                loading = state.loading,
                eventNotSupported = state.eventNotSupported,
                sessionEvent = state.sessionEvent,
                requestTypeId = state.requestTypeId,
                namingMap = state.namingMap,
                parsedSignedEvent = state.parsedSignedEvent,
                parsedUnsignedEvent = state.parsedUnsignedEvent,
                rawJson = state.rawJson,
            )
        },
    )
}
