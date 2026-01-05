package net.primal.android.settings.connected.permissions.local

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import net.primal.android.R
import net.primal.android.core.compose.AppIconThumbnail
import net.primal.android.settings.connected.permissions.ConnectedAppPermissionsScreen
import net.primal.android.settings.connected.permissions.local.LocalAppPermissionsContract.UiEvent
import net.primal.android.signer.provider.rememberAppDisplayInfo
import net.primal.android.theme.AppTheme

@Composable
fun LocalAppPermissionsScreen(viewModel: LocalAppPermissionsViewModel, onClose: () -> Unit) {
    val state = viewModel.state.collectAsState()

    val appDisplayInfo =
        rememberAppDisplayInfo(packageName = state.value.packageName ?: "", fallbackAppName = state.value.appName)

    ConnectedAppPermissionsScreen(
        headerContent = {
            LocalAppHeader(
                appName = appDisplayInfo.name,
                iconContent = {
                    if (appDisplayInfo.icon != null) {
                        Image(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(AppTheme.shapes.small),
                            bitmap = appDisplayInfo.icon.toBitmap().asImageBitmap(),
                            contentDescription = appDisplayInfo.name,
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        AppIconThumbnail(
                            appIconUrl = appDisplayInfo.icon,
                            appName = appDisplayInfo.name,
                            avatarSize = 48.dp,
                        )
                    }
                },
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

@Composable
private fun LocalAppHeader(
    modifier: Modifier = Modifier,
    appName: String?,
    iconContent: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.padding(vertical = 16.dp),
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
            Box(modifier = Modifier.padding(bottom = 6.dp)) {
                iconContent()
            }

            Text(
                text = appName ?: stringResource(id = R.string.settings_connected_apps_unknown),
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colorScheme.onPrimary,
            )
        }
    }
}
