package net.primal.android.core.compose.signer

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import net.primal.android.R
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.service.PrimalRemoteSignerService
import net.primal.android.core.utils.getNotificationSettingsIntent
import net.primal.android.core.utils.hasNotificationPermission
import net.primal.android.nostrconnect.ui.NostrConnectBottomSheetDragHandle
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EnableSignerNotificationsBottomSheet(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val channelId = PrimalRemoteSignerService.CHANNEL_ID

    var isEnabled by remember { mutableStateOf(context.hasNotificationPermission(channelId)) }

    val systemSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        isEnabled = context.hasNotificationPermission(channelId)
    }

    val notificationsPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS) {
            isEnabled = context.hasNotificationPermission(channelId)
        }
    } else {
        null
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isEnabled = context.hasNotificationPermission(channelId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    fun launchSettings() {
        systemSettingsLauncher.launch(context.getNotificationSettingsIntent(channelId))
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        dragHandle = { NostrConnectBottomSheetDragHandle() },
    ) {
        EnableSignerNotificationsContent(
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
    isEnabled: Boolean,
    onDismissRequest: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(id = R.string.signer_notification_enable_title),
                style = AppTheme.typography.titleLarge,
                color = AppTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(id = R.string.signer_notification_enable_description),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                    shape = AppTheme.shapes.medium,
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.signer_notification_enable_switch_label),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )

            PrimalSwitch(
                checked = isEnabled,
                onCheckedChange = onCheckedChange,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        PrimalFilledButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            onClick = onDismissRequest,
        ) {
            Text(
                text = stringResource(id = R.string.signer_notification_enable_button),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
