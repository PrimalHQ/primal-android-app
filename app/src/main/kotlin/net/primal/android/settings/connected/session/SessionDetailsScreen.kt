package net.primal.android.settings.connected.session

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
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
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.getListItemShape
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.utils.PrimalDateFormats
import net.primal.android.core.utils.rememberPrimalFormattedDateTime
import net.primal.android.settings.connected.model.SessionEventUi
import net.primal.android.settings.connected.ui.ConnectedAppHeader
import net.primal.android.theme.AppTheme

@Composable
fun SessionDetailsScreen(
    viewModel: SessionDetailsViewModel,
    onClose: () -> Unit,
    onEventClick: (eventId: String) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    SessionDetailsScreen(
        state = uiState.value,
        onClose = onClose,
        onEventClick = onEventClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailsScreen(
    state: SessionDetailsContract.UiState,
    onClose: () -> Unit,
    onEventClick: (eventId: String) -> Unit,
) {
    PrimalScaffold(
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

@Composable
private fun SessionEventListItem(
    event: SessionEventUi,
    namingMap: Map<String, String>,
    onClick: () -> Unit,
) {
    val formattedTimestamp = rememberPrimalFormattedDateTime(
        timestamp = event.timestamp,
        format = PrimalDateFormats.DATETIME_MM_DD_YYYY_HH_MM_SS_A,
    )
    val title = namingMap[event.requestTypeId] ?: event.requestTypeId
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(text = title, style = AppTheme.typography.bodyLarge) },
        supportingContent = {
            Text(
                modifier = Modifier.padding(top = 5.dp),
                text = formattedTimestamp,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            )
        },
        trailingContent = {
            Icon(
                modifier = Modifier.size(28.dp),
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
        ),
    )
}
