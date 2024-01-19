package net.primal.android.wallet.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal

@Composable
fun WalletDashboard(
    modifier: Modifier,
    enabled: Boolean = true,
    walletBalance: BigDecimal?,
    actions: List<WalletAction>,
    onWalletAction: (WalletAction) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BtcAmountText(
            modifier = Modifier
                .wrapContentWidth()
                .padding(start = if (walletBalance != null) 32.dp else 0.dp)
                .padding(bottom = 32.dp),
            amountInBtc = walletBalance ?: BigDecimal.ZERO,
            textSize = 48.sp,
        )

        WalletActionsRow(
            modifier = Modifier.fillMaxWidth(),
            actionSize = 80.dp,
            actions = actions,
            enabled = enabled,
            onWalletAction = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onWalletAction(it)
            },
        )
    }
}
