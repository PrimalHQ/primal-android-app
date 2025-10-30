package net.primal.android.settings.connected

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.AppIconThumbnail
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.settings.connected.model.AppConnectionUi
import net.primal.android.stream.player.LocalStreamState
import net.primal.android.stream.player.StreamState
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.domain.links.CdnImage

@Composable
fun ConnectedAppsScreen(
    viewModel: ConnectedAppsViewModel,
    onClose: () -> Unit,
    onConnectedAppClick: (String) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()
    ConnectedAppsScreen(
        state = uiState.value,
        onClose = onClose,
        onConnectedAppClick = onConnectedAppClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectedAppsScreen(
    state: ConnectedAppsContract.UiState,
    onClose: () -> Unit,
    onConnectedAppClick: (String) -> Unit,
) {
    PrimalScaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_connected_apps_title),
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
                    .padding(paddingValues),
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                            .clip(RoundedCornerShape(size = 12.dp)),
                    ) {
                        state.connections.forEachIndexed { index, connection ->
                            ConnectedAppListItem(
                                connection = connection,
                                onClick = { onConnectedAppClick(connection.connectionId) },
                            )
                            if (index < state.connections.size - 1) {
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
private fun ConnectedAppListItem(connection: AppConnectionUi, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
        ),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (connection.isActive) {
                            AppTheme.extraColorScheme.successBright
                        } else {
                            AppTheme.extraColorScheme.onSurfaceVariantAlt4
                        },
                        shape = CircleShape,
                    ),
            )
        },
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                AppIconThumbnail(
                    avatarCdnImage = connection.appImage,
                    appName = connection.appName,
                    avatarSize = 22.dp,
                )
                Text(
                    modifier = Modifier.padding(top = 2.dp),
                    text = connection.appName,
                    style = AppTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                )
            }
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UniversalAvatarThumbnail(
                    avatarCdnImage = connection.userAvatarCdnImage,
                    avatarSize = 22.dp,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                )
            }
        },
    )
}

@Preview
@Composable
fun PreviewConnectedAppsScreen() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        CompositionLocalProvider(LocalStreamState provides StreamState()) {
            ConnectedAppsScreen(
                state = ConnectedAppsContract.UiState(
                    loading = false,
                    connections = listOf(
                        AppConnectionUi(
                            connectionId = "1",
                            appName = "Primal web app",
                            appImage = CdnImage("https://primal.net/assets/favicon-51789dff.ico"),
                            userAvatarCdnImage = CdnImage("https://i.imgur.com/Z8dpmvc.png"),
                            isActive = true,
                        ),
                        AppConnectionUi(
                            connectionId = "2",
                            appName = "Nostr 1",
                            appImage = null,
                            userAvatarCdnImage = CdnImage("https://i.imgur.com/Z8dpmvc.png"),
                            isActive = true,
                        ),
                        AppConnectionUi(
                            connectionId = "3",
                            appName = "Primal",
                            appImage = null,
                            userAvatarCdnImage = CdnImage("https://i.imgur.com/Z8dpmvc.png"),
                            isActive = false,
                        ),
                        AppConnectionUi(
                            connectionId = "4",
                            appName = "",
                            appImage = null,
                            userAvatarCdnImage = CdnImage("https://i.imgur.com/Z8dpmvc.png"),
                            isActive = false,
                        ),
                    ),
                ),
                onClose = {},
                onConnectedAppClick = {},
            )
        }
    }
}
