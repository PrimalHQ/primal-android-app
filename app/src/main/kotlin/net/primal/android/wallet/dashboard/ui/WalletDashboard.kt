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
import net.primal.android.wallet.dashboard.CurrencyMode

@Composable
fun WalletDashboard(
    modifier: Modifier,
    walletBalance: BigDecimal?,
    exchangeBtcUsdRate: Double?,
    actions: List<WalletAction>,
    onWalletAction: (WalletAction) -> Unit,
    onSwitchCurrencyMode: (currencyMode: CurrencyMode) -> Unit,
    enabled: Boolean = true,
    currencyMode: CurrencyMode = CurrencyMode.SATOSHI,
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AmountText(
            modifier = Modifier
                .wrapContentWidth()
                .padding(start = if (walletBalance != null) 32.dp else 0.dp)
                .padding(bottom = 32.dp),
            amount = walletBalance ?: BigDecimal.ZERO,
            textSize = 48.sp,
            currencyMode = currencyMode,
            onSwitchCurrencyMode = onSwitchCurrencyMode,
            exchangeBtcUsdRate = exchangeBtcUsdRate,
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
