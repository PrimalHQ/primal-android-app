package net.primal.android.settings.connected.session

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Date
import java.util.Locale
import net.primal.android.R
import net.primal.android.core.compose.AppIconThumbnail
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.settings.connected.model.SessionEventUi
import net.primal.android.theme.AppTheme
import net.primal.domain.links.CdnImage

private const val SECONDS_TO_MILLIS = 1000L

@Composable
fun SessionDetailsScreen(viewModel: SessionDetailsViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()
    SessionDetailsScreen(
        state = uiState.value,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailsScreen(state: SessionDetailsContract.UiState, onClose: () -> Unit) {
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
                    val isFirst = index == 0
                    val isLast = index == state.sessionEvents.lastIndex

                    val shape = when {
                        isFirst && isLast -> RoundedCornerShape(size = 12.dp)
                        isFirst -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                        isLast -> RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                        else -> AppTheme.shapes.extraSmall
                    }

                    Column(modifier = Modifier.clip(shape)) {
                        SessionEventListItem(event = event, onClick = { })
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
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            AppIconThumbnail(
                avatarCdnImage = appIconUrl?.let { CdnImage(it) },
                appName = appName,
                avatarSize = 48.dp,
            )
            Text(
                text = appName ?: stringResource(id = R.string.settings_connected_apps_unknown),
                style = AppTheme.typography.bodyLarge.copy(
                    color = AppTheme.colorScheme.onPrimary,
                ),
            )
            if (startedAt != null) {
                val formattedStartedAt = rememberFormattedDateTime(
                    timestamp = startedAt,
                    format = "MMM dd, yyyy h:mm a",
                )
                Text(
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
    val formattedTimestamp = rememberFormattedDateTime(
        timestamp = event.timestamp,
        format = "MMM dd, yyyy h:mm:ss a",
    )
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(text = event.title, style = AppTheme.typography.labelMedium.copy(fontSize = 16.sp)) },
        supportingContent = {
            Text(
                modifier = Modifier.padding(top = 2.dp),
                text = formattedTimestamp,
                style = AppTheme.typography.titleSmall,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            )
        },
        trailingContent = {
            Icon(
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

@Composable
private fun rememberFormattedDateTime(timestamp: Long, format: String): String {
    return remember(timestamp, format) {
        val simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
        simpleDateFormat.format(Date(timestamp * SECONDS_TO_MILLIS))
    }
}
