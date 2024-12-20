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
import net.primal.android.wallet.dashboard.CurrencyMode

@Composable
fun WalletDashboardLite(
    modifier: Modifier,
    walletBalance: BigDecimal?,
    actions: List<WalletAction>,
    onWalletAction: (WalletAction) -> Unit,
    currencyMode: CurrencyMode,
    onSwitchCurrencyMode: (currencyMode: CurrencyMode) -> Unit,
    exchangeBtcUsdRate: Double?,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        AmountText(
            modifier = Modifier.graphicsLayer {
                clip = false
                translationY = 4.dp.toPx()
            },
            amount = walletBalance ?: BigDecimal.ZERO,
            textSize = 32.sp,
            currencyMode = currencyMode,
            onSwitchCurrencyMode = onSwitchCurrencyMode,
            exchangeBtcUsdRate = exchangeBtcUsdRate,
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
