package net.primal.android.settings.connected.session.remote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.getListItemShape
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.settings.connected.session.SessionEventListItem
import net.primal.android.settings.connected.ui.ConnectedAppHeader
import net.primal.android.theme.AppTheme

@Composable
fun RemoteSessionDetailsScreen(
    viewModel: RemoteSessionDetailsViewModel,
    onClose: () -> Unit,
    onEventClick: (eventId: String) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()
    RemoteSessionDetailsScreen(
        state = uiState.value,
        onClose = onClose,
        onEventClick = onEventClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteSessionDetailsScreen(
    state: RemoteSessionDetailsContract.UiState,
    onClose: () -> Unit,
    onEventClick: (eventId: String) -> Unit,
) {
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_session_details_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
    ) { paddingValues ->
        if (state.loading) {
            PrimalLoadingSpinner()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colorScheme.surfaceVariant)
                    .padding(paddingValues)
                    .padding(horizontal = 12.dp),
            ) {
                item(key = "Header") {
                    ConnectedAppHeader(
                        modifier = Modifier.padding(vertical = 16.dp),
                        appName = state.appName,
                        appIconUrl = state.appIconUrl,
                        startedAt = state.sessionStartedAt,
                    )
                }
                if (state.sessionEvents.isEmpty()) {
                    item(key = "NoContent") {
                        ListNoContent(
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .padding(vertical = 32.dp),
                            noContentText = stringResource(id = R.string.settings_session_no_events),
                            refreshButtonVisible = false,
                        )
                    }
                } else {
                    itemsIndexed(
                        items = state.sessionEvents,
                        key = { _, event -> event.id },
                    ) { index, event ->
                        val shape = getListItemShape(index = index, listSize = state.sessionEvents.size)
                        val isLast = index == state.sessionEvents.lastIndex

                        Column(modifier = Modifier.clip(shape)) {
                            SessionEventListItem(
                                event = event,
                                namingMap = state.namingMap,
                                onClick = { onEventClick(event.id) },
                            )
                            if (!isLast) {
                                PrimalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}
