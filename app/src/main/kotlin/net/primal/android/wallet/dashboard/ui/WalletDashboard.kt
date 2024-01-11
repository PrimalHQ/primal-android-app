package net.primal.android.wallet.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AmountText(
            modifier = Modifier.wrapContentWidth()
                .padding(start = if (walletBalance != null) 32.dp else 0.dp)
                .padding(bottom = 32.dp),
            amountInBtc = walletBalance,
            textSize = 48.sp,
        )

        WalletActionsRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            actionSize = 80.dp,
            actions = actions,
            enabled = enabled,
            onWalletAction = onWalletAction,
        )
    }
}
