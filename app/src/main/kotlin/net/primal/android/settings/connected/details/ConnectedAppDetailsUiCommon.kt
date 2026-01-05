package net.primal.android.settings.connected.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import net.primal.android.R
import net.primal.android.core.compose.AppIconThumbnail
import net.primal.android.core.compose.ConfirmActionAlertDialog
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.getListItemShape
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.HighSecurity
import net.primal.android.core.compose.icons.primaliconpack.LowSecurity
import net.primal.android.core.compose.icons.primaliconpack.MediumSecurity
import net.primal.android.core.compose.nostrconnect.PermissionsListItem
import net.primal.android.core.utils.PrimalDateFormats
import net.primal.android.core.utils.rememberPrimalFormattedDateTime
import net.primal.android.settings.connected.model.SessionUi
import net.primal.android.signer.provider.rememberAppDisplayInfo
import net.primal.android.theme.AppTheme
import net.primal.domain.account.model.TrustLevel

val DangerPrimaryColor = Color(0xFFFF2121)
val DangerSecondaryColor = Color(0xFFFA3C3C)

@Composable
fun ConnectedAppSummarySection(
    iconUrl: String?,
    appName: String?,
    lastSession: Long?,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        AppIconThumbnail(
            modifier = Modifier.padding(bottom = 12.dp),
            appIconUrl = iconUrl,
            appName = appName ?: stringResource(id = R.string.settings_connected_apps_unknown),
            avatarSize = 48.dp,
        )

        Text(
            text = appName ?: stringResource(id = R.string.settings_connected_apps_unknown),
            style = AppTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
            fontWeight = FontWeight.Bold,
        )
        if (lastSession != null) {
            val formattedLastSession = rememberPrimalFormattedDateTime(
                timestamp = lastSession,
                format = PrimalDateFormats.DATETIME_MM_DD_YYYY_HH_MM_A,
            )
            Text(
                text = stringResource(
                    id = R.string.settings_connected_app_details_last_session,
                    formattedLastSession,
                ),
                style = AppTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            )
        }
    }
}

@Composable
fun ConnectedAppPermissionsSection(
    trustLevel: TrustLevel,
    onTrustLevelChange: (TrustLevel) -> Unit,
    onPermissionDetailsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(id = R.string.settings_connected_app_permissions_subtitle).uppercase(),
            style = AppTheme.typography.titleMedium.copy(lineHeight = 20.sp),
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colorScheme.onPrimary,
        )

        Column(
            modifier = Modifier.clip(AppTheme.shapes.medium),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PermissionsListItem(
                icon = PrimalIcons.HighSecurity,
                title = stringResource(id = R.string.signer_connect_full_trust_title),
                subtitle = stringResource(id = R.string.settings_connected_app_permissions_full_trust_subtitle),
                isSelected = trustLevel == TrustLevel.Full,
                onClick = { onTrustLevelChange(TrustLevel.Full) },
            )
            PermissionsListItem(
                icon = PrimalIcons.MediumSecurity,
                title = stringResource(id = R.string.signer_connect_medium_trust_title),
                subtitle = stringResource(id = R.string.settings_connected_app_permissions_medium_trust_subtitle),
                isSelected = trustLevel == TrustLevel.Medium,
                onClick = { onTrustLevelChange(TrustLevel.Medium) },
            )
            PermissionsListItem(
                icon = PrimalIcons.LowSecurity,
                title = stringResource(id = R.string.signer_connect_low_trust_title),
                subtitle = stringResource(id = R.string.settings_connected_app_permissions_low_trust_subtitle),
                isSelected = trustLevel == TrustLevel.Low,
                onClick = { onTrustLevelChange(TrustLevel.Low) },
            )
        }

        if (trustLevel == TrustLevel.Medium) {
            Text(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clickable { onPermissionDetailsClick() },
                text = stringResource(id = R.string.settings_connected_app_permission_details_link),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colorScheme.secondary,
            )
        }
    }
}

fun LazyListScope.connectedAppRecentSessionsSection(
    recentSessions: List<SessionUi>,
    appIconUrl: String?,
    appName: String?,
    appPackageName: String? = null,
    onSessionClick: (String) -> Unit,
) {
    if (recentSessions.isNotEmpty()) {
        item(key = "RecentSessionsTitle", contentType = "Title") {
            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = stringResource(id = R.string.settings_connected_app_details_recent_sessions).uppercase(),
                style = AppTheme.typography.titleMedium.copy(lineHeight = 20.sp),
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colorScheme.onPrimary,
            )
        }

        itemsIndexed(
            items = recentSessions,
            key = { _, session -> session.sessionId },
            contentType = { _, _ -> "SessionItem" },
        ) { index, session ->
            val shape = getListItemShape(index = index, listSize = recentSessions.size)
            val isLast = index == recentSessions.lastIndex

            Column(modifier = Modifier.clip(shape)) {
                RecentSessionItem(
                    session = session,
                    iconUrl = appIconUrl,
                    appName = appName,
                    packageName = appPackageName,
                    onClick = { onSessionClick(session.sessionId) },
                )
                if (!isLast) {
                    PrimalDivider()
                }
            }
        }
    }
}

@Composable
private fun RecentSessionItem(
    session: SessionUi,
    iconUrl: String?,
    appName: String?,
    packageName: String? = null,
    onClick: () -> Unit,
) {
    val formattedDate = rememberPrimalFormattedDateTime(
        timestamp = session.startedAt,
        format = PrimalDateFormats.DATETIME_MM_DD_YYYY_HH_MM_A,
    )

    ListItem(
        modifier = Modifier.clickable { onClick() },
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
        ),
        leadingContent = {
            if (packageName != null) {
                val appDisplayInfo = rememberAppDisplayInfo(packageName = packageName)
                if (appDisplayInfo?.icon != null) {
                    Image(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(AppTheme.shapes.small),
                        bitmap = appDisplayInfo.icon.toBitmap().asImageBitmap(),
                        contentDescription = appName,
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    AppIconThumbnail(
                        appName = appName ?: stringResource(id = R.string.settings_connected_apps_unknown),
                        appIconUrl = null,
                        avatarSize = 24.dp,
                    )
                }
            } else {
                AppIconThumbnail(
                    appName = appName ?: stringResource(id = R.string.settings_connected_apps_unknown),
                    appIconUrl = iconUrl,
                    avatarSize = 24.dp,
                )
            }
        },
        headlineContent = {
            Text(
                text = formattedDate,
                style = AppTheme.typography.bodyMedium,
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
        },
    )
}

@Composable
fun TrustLevel.toUserFriendlyText() =
    when (this) {
        TrustLevel.Full -> stringResource(id = R.string.settings_connected_app_details_full_trust)
        TrustLevel.Medium -> stringResource(id = R.string.settings_connected_app_details_medium_trust)
        TrustLevel.Low -> stringResource(id = R.string.settings_connected_app_details_low_trust)
    }

@Composable
fun ConnectedAppDeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ConfirmActionAlertDialog(
        dialogTitle = stringResource(id = R.string.settings_connected_app_details_delete_connection_dialog_title),
        dialogText = stringResource(id = R.string.settings_connected_app_details_delete_connection_dialog_text),
        confirmText = stringResource(id = R.string.settings_connected_app_details_delete_connection_confirm),
        onConfirmation = onConfirm,
        dismissText = stringResource(id = R.string.settings_connected_app_details_delete_connection_dismiss),
        onDismissRequest = onDismiss,
    )
}

@Composable
fun ConnectedAppUpdateTrustLevelDialog(
    trustLevel: TrustLevel,
    onConfirm: (TrustLevel) -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmActionAlertDialog(
        dialogTitle = stringResource(id = R.string.settings_connected_app_details_update_trust_level_dialog_title),
        dialogText = stringResource(
            id = R.string.settings_connected_app_details_update_trust_level_dialog_text,
            trustLevel.toUserFriendlyText(),
        ),
        confirmText = stringResource(id = R.string.settings_connected_app_details_update_trust_level_confirm),
        dismissText = stringResource(id = R.string.settings_connected_app_details_update_trust_level_dismiss),
        onConfirmation = { onConfirm(trustLevel) },
        onDismissRequest = onDismiss,
    )
}
