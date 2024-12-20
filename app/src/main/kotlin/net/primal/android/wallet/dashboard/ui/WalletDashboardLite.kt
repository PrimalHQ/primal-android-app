package net.primal.android.wallet.dashboard.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        AnimatedContent(
            modifier = modifier.fillMaxWidth(),
            label = "Animated currency switch",
            targetState = currencyMode,
            transitionSpec = { (slideInVertically() + fadeIn()) togetherWith fadeOut() },
        ) { targetCurrencyMode ->
            if (targetCurrencyMode == CurrencyMode.FIAT) {
                FiatAmountText(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(start = if (walletBalance != null) 32.dp else 0.dp)
                        .padding(bottom = 32.dp)
                        .clickable { onSwitchCurrencyMode(CurrencyMode.SATS) },
                    amount = walletBalance ?: BigDecimal.ZERO,
                    textSize = 48.sp,
                    exchangeBtcUsdRate = exchangeBtcUsdRate,
                )
            } else {
                BtcAmountText(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(start = if (walletBalance != null) 32.dp else 0.dp)
                        .padding(bottom = 32.dp)
                        .clickable { onSwitchCurrencyMode(CurrencyMode.FIAT) },
                    amountInBtc = walletBalance ?: BigDecimal.ZERO,
                    textSize = 48.sp,
                )
            }
        }

        WalletActionsRow(
            modifier = Modifier,
            actions = actions,
            actionSize = 48.dp,
            showLabels = false,
            onWalletAction = onWalletAction,
        )
    }
}
