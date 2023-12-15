package net.primal.android.wallet.dashboard.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.QrCode
import net.primal.android.core.compose.icons.primaliconpack.WalletPay
import net.primal.android.core.compose.icons.primaliconpack.WalletReceive
import net.primal.android.theme.AppTheme

@Composable
fun WalletActionsRow(
    modifier: Modifier = Modifier,
    actions: List<WalletAction>,
    actionSize: Dp,
    showLabels: Boolean = true,
    onWalletAction: (WalletAction) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        actions.forEach {
            WalletActionButton(
                modifier = Modifier.padding(horizontal = 4.dp).size(actionSize),
                onClick = { onWalletAction(it) },
                text = if (showLabels) stringResource(id = it.textResId) else null,
                icon = it.imageVector,
            )
        }
    }
}

@Composable
private fun WalletActionButton(
    modifier: Modifier,
    text: String?,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PrimalCircleButton(
            modifier = modifier,
            onClick = onClick,
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
            contentColor = AppTheme.colorScheme.onSurface,
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = icon,
                contentDescription = null,
            )
        }

        if (text != null) {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = text.uppercase(),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
        }
    }
}

enum class WalletAction(
    @StringRes val textResId: Int,
    val imageVector: ImageVector,
) {
    Send(textResId = R.string.wallet_action_button_send, imageVector = PrimalIcons.WalletPay),
    Scan(textResId = R.string.wallet_action_button_scan, imageVector = PrimalIcons.QrCode),
    Receive(textResId = R.string.wallet_action_button_receive, imageVector = PrimalIcons.WalletReceive),
}
