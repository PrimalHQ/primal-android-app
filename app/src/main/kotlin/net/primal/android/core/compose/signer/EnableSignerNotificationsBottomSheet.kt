package net.primal.android.core.compose.signer

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import net.primal.android.R
import net.primal.android.core.compose.AppIconThumbnail
import net.primal.android.core.compose.PrimalBottomSheetDragHandle
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.service.PRIMAL_SERVICE_NOTIFICATION_CHANNEL_ID
import net.primal.android.core.utils.getNotificationSettingsIntent
import net.primal.android.core.utils.hasNotificationPermission
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EnableSignerNotificationsBottomSheet(
    appName: String?,
    appIconUrl: String?,
    onDismissRequest: () -> Unit,
    onTogglePushNotifications: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val channelId = PRIMAL_SERVICE_NOTIFICATION_CHANNEL_ID

    var isEnabled by remember { mutableStateOf(context.hasNotificationPermission(channelId)) }

    val systemSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        val hasPermission = context.hasNotificationPermission(channelId)
        isEnabled = hasPermission
        if (hasPermission) onTogglePushNotifications(true)
    }

    val notificationsPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS) {
            val hasPermission = context.hasNotificationPermission(channelId)
            isEnabled = hasPermission
            if (hasPermission) onTogglePushNotifications(true)
        }
    } else {
        null
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val hasPermission = context.hasNotificationPermission(channelId)
                isEnabled = hasPermission
                if (hasPermission) onTogglePushNotifications(true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    fun launchSettings() {
        val areAppNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()

        if (areAppNotificationsEnabled) {
            systemSettingsLauncher.launch(context.getNotificationSettingsIntent(channelId))
        } else {
            systemSettingsLauncher.launch(context.getNotificationSettingsIntent())
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        dragHandle = { PrimalBottomSheetDragHandle() },
    ) {
        EnableSignerNotificationsContent(
            appName = appName,
            appIconUrl = appIconUrl,
            isEnabled = isEnabled,
            onDismissRequest = onDismissRequest,
            onCheckedChange = { newEnabled ->
                if (newEnabled) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                        notificationsPermission?.status?.isGranted == true
                    ) {
                        if (!context.hasNotificationPermission(channelId)) {
                            launchSettings()
                        } else {
                            isEnabled = true
                            onTogglePushNotifications(true)
                        }
                    } else {
                        if (notificationsPermission?.status?.shouldShowRationale == true) {
                            notificationsPermission.launchPermissionRequest()
                        } else {
                            launchSettings()
                        }
                    }
                } else {
                    launchSettings()
                }
            },
        )
    }
}

@Composable
private fun EnableSignerNotificationsContent(
    appName: String?,
    appIconUrl: String?,
    isEnabled: Boolean,
    onDismissRequest: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
) {
    val appNameDisplay = appName ?: stringResource(id = R.string.signer_notification_unknown_app)

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        SignerAppHeader(
            appName = appNameDisplay,
            appIconUrl = appIconUrl,
        )

        PrimalDivider()

        SignerNotificationsControls(
            isEnabled = isEnabled,
            onCheckedChange = onCheckedChange,
            onDismissRequest = onDismissRequest,
        )
    }
}

@Composable
private fun SignerAppHeader(appName: String, appIconUrl: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = AppTheme.extraColorScheme.surfaceVariantAlt2)
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppIconThumbnail(
            appIconUrl = appIconUrl,
            appName = appName,
            avatarSize = 56.dp,
        )

        Text(
            text = appName,
            style = AppTheme.typography.titleLarge.copy(
                lineHeight = 24.sp,
                color = AppTheme.colorScheme.onPrimary,
            ),
            fontWeight = FontWeight.Bold,
        )

        val titleText = buildAnnotatedString {
            append(stringResource(id = R.string.signer_notification_session_active_prefix))
            append(" ")
            append(appName)
        }

        Text(
            text = titleText,
            style = AppTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Normal,
                color = AppTheme.extraColorScheme.successBright,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SignerNotificationsControls(
    isEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = AppTheme.extraColorScheme.surfaceVariantAlt1)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(id = R.string.signer_notification_keep_running_description),
            style = AppTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Normal,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                lineHeight = 20.sp,
            ),
            textAlign = TextAlign.Center,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppTheme.shapes.medium)
                .background(
                    color = AppTheme.extraColorScheme.surfaceVariantAlt3,
                    shape = AppTheme.shapes.medium,
                )
                .clickable { onCheckedChange(!isEnabled) }
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.signer_notification_enable_switch_label),
                style = AppTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Normal,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                ),
            )

            PrimalSwitch(
                checked = isEnabled,
                onCheckedChange = onCheckedChange,
            )
        }

        PrimalFilledButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            onClick = onDismissRequest,
        ) {
            Text(
                text = stringResource(id = R.string.signer_notification_enable_button),
                style = AppTheme.typography.titleLarge,
            )
        }
    }
}
