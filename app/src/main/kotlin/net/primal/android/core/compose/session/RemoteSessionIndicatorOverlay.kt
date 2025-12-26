package net.primal.android.core.compose.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.NostrConnectSession
import net.primal.android.core.compose.indicator.IndicatorOverlay
import net.primal.android.core.compose.signer.EnableSignerNotificationsBottomSheet
import net.primal.android.core.service.PrimalRemoteSignerService
import net.primal.android.core.utils.hasNotificationPermission
import net.primal.android.theme.AppTheme

@Composable
fun RemoteSessionIndicatorOverlay(
    viewModel: RemoteSessionIndicatorViewModel,
    isNetworkUnavailable: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val uiState by viewModel.state.collectAsState()
    val context = LocalContext.current

    var showNotificationPrompt by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.isRemoteSessionActive) {
        if (uiState.isRemoteSessionActive) {
            val hasPermission = context.hasNotificationPermission(PrimalRemoteSignerService.CHANNEL_ID)
            if (!hasPermission) {
                showNotificationPrompt = true
            }
        }
    }

    if (showNotificationPrompt) {
        EnableSignerNotificationsBottomSheet(
            onDismissRequest = { showNotificationPrompt = false },
        )
    }

    IndicatorOverlay(
        showIndicator = uiState.isRemoteSessionActive,
        indicatorText = stringResource(id = R.string.app_nostr_connect_session_active),
        indicatorIcon = PrimalIcons.NostrConnectSession,
        indicatorIconTint = AppTheme.colorScheme.surfaceVariant,
        floatingIcon = PrimalIcons.NostrConnectSession,
        floatingIconTint = AppTheme.colorScheme.onPrimary,
        floatingIconTopPadding = if (isNetworkUnavailable) 64.dp else 16.dp,
        onClick = onClick,
        content = content,
    )
}
