package net.primal.android.settings.connected.session

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.AppIconThumbnail
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
import net.primal.android.settings.connected.session.SessionDetailsContract.UiEvent
import net.primal.android.theme.AppTheme
import net.primal.domain.links.CdnImage

@Composable
fun SessionDetailsScreen(
    viewModel: SessionDetailsViewModel,
    onClose: () -> Unit,
    onEventClick: (connectionId: String, sessionId: String, eventId: String) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(viewModel, onEventClick) {
        viewModel.effect.collect {
            when (it) {
                is SessionDetailsContract.SideEffect.NavigateToEventDetails -> {
                    onEventClick(it.connectionId, it.sessionId, it.eventId)
                }
            }
        }
    }

    SessionDetailsScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailsScreen(
    state: SessionDetailsContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (UiEvent) -> Unit,
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
                    HeaderSection(
                        modifier = Modifier.padding(vertical = 16.dp),
                        appName = state.appName,
                        appIconUrl = state.appIconUrl,
                        startedAt = state.sessionStartedAt,
                    )
                }

                itemsIndexed(
                    items = state.sessionEvents,
                    key = { _, event -> event.id },
                ) { index, event ->
                    val shape = getListItemShape(index = index, listSize = state.sessionEvents.size)
                    val isLast = index == state.sessionEvents.lastIndex

                    Column(modifier = Modifier.clip(shape)) {
                        SessionEventListItem(event = event, onClick = { eventPublisher(UiEvent.EventClick(event.id)) })
                        if (!isLast) {
                            PrimalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    modifier: Modifier = Modifier,
    appName: String?,
    appIconUrl: String?,
    startedAt: Long?,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            AppIconThumbnail(
                modifier = Modifier.padding(bottom = 6.dp),
                avatarCdnImage = appIconUrl?.let { CdnImage(it) },
                appName = appName,
                avatarSize = 48.dp,
            )
            Text(
                text = appName ?: stringResource(id = R.string.settings_connected_apps_unknown),
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colorScheme.onPrimary,
            )
            if (startedAt != null) {
                val formattedStartedAt = rememberPrimalFormattedDateTime(
                    timestamp = startedAt,
                    format = PrimalDateFormats.DATETIME_MM_DD_YYYY_HH_MM_A,
                )
                Text(
                    modifier = Modifier.padding(top = 2.dp),
                    text = stringResource(id = R.string.settings_session_details_started_on, formattedStartedAt),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                )
            }
        }
    }
}

@Composable
private fun SessionEventListItem(event: SessionEventUi, onClick: () -> Unit) {
    val formattedTimestamp = rememberPrimalFormattedDateTime(
        timestamp = event.timestamp,
        format = PrimalDateFormats.DATETIME_MM_DD_YYYY_HH_MM_SS_A,
    )
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(text = event.title, style = AppTheme.typography.bodyLarge) },
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
