package net.primal.android.core.compose.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.NostrConnectSession
import net.primal.android.core.compose.indicator.IndicatorOverlay
import net.primal.android.theme.AppTheme

@Composable
fun RemoteSessionIndicatorOverlay(
    viewModel: RemoteSessionIndicatorViewModel,
    isNetworkUnavailable: Boolean,
    content: @Composable () -> Unit,
) {
    val uiState by viewModel.state.collectAsState()

    IndicatorOverlay(
        showIndicator = uiState.isRemoteSessionActive,
        indicatorText = stringResource(id = R.string.app_nostr_connect_session_active),
        indicatorIcon = PrimalIcons.NostrConnectSession,
        indicatorIconTint = AppTheme.colorScheme.surfaceVariant,
        floatingIcon = PrimalIcons.NostrConnectSession,
        floatingIconTint = AppTheme.colorScheme.onPrimary,
        floatingIconTopPadding = if (isNetworkUnavailable) 64.dp else 16.dp,
        content = content,
    )
}
