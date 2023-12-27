package net.primal.android.wallet.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal

@Composable
fun WalletDashboardLite(
    modifier: Modifier,
    walletBalance: BigDecimal?,
    actions: List<WalletAction>,
    onWalletAction: (WalletAction) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        WalletBalanceText(
            modifier = Modifier.graphicsLayer {
                clip = false
                translationY = 4.dp.toPx()
            },
            walletBalance = walletBalance,
            textSize = 32.sp,
        )

        WalletActionsRow(
            modifier = Modifier,
            actions = actions,
            actionSize = 48.dp,
            showLabels = false,
            onWalletAction = onWalletAction,
        )
    }
}
