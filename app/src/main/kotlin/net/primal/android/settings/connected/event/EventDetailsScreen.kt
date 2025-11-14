package net.primal.android.settings.connected.event

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.nostr.NostrEventDetails
import net.primal.android.core.utils.copyText

@Composable
fun EventDetailsScreen(viewModel: EventDetailsViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()
    EventDetailsScreen(
        state = uiState.value,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(state: EventDetailsContract.UiState, onClose: () -> Unit) {
    val context = LocalContext.current
    PrimalScaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_event_details_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            when {
                state.loading -> PrimalLoadingSpinner()
                state.event != null -> {
                    NostrEventDetails(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        event = state.event,
                        rawJson = state.rawJson,
                        onCopy = { text, label ->
                            copyText(context = context, text = text, label = label)
                        },
                    )
                }
                state.eventNotSupported -> {
                    ListNoContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        noContentText = stringResource(id = R.string.settings_event_details_not_supported),
                        refreshButtonVisible = false,
                    )
                }
            }
        },
    )
}
