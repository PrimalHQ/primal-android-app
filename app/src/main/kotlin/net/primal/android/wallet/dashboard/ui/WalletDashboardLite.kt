package net.primal.android.wallet.dashboard.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import net.primal.android.wallet.domain.CurrencyMode
import net.primal.android.wallet.repository.isValidExchangeRate

@Composable
fun WalletDashboardLite(
    modifier: Modifier,
    walletBalance: BigDecimal?,
    actions: List<WalletAction>,
    onWalletAction: (WalletAction) -> Unit,
    currencyMode: CurrencyMode,
    enabled: Boolean,
    onSwitchCurrencyMode: (currencyMode: CurrencyMode) -> Unit,
    exchangeBtcUsdRate: Double?,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        AnimatedContent(
            modifier = Modifier,
            label = "CurrencyContent",
            targetState = currencyMode,
        ) { targetCurrencyMode ->
            if (targetCurrencyMode == CurrencyMode.FIAT && exchangeBtcUsdRate.isValidExchangeRate()) {
                FiatAmountTextFromBtc(
                    modifier = Modifier
                        .graphicsLayer {
                            clip = false
                            translationY = 4.dp.toPx()
                        }
                        .clickable { onSwitchCurrencyMode(CurrencyMode.SATS) },
                    amount = walletBalance ?: BigDecimal.ZERO,
                    textSize = 32.sp,
                    exchangeBtcUsdRate = exchangeBtcUsdRate,
                )
            } else {
                BtcAmountText(
                    modifier = Modifier
                        .graphicsLayer {
                            clip = false
                            translationY = 4.dp.toPx()
                        }
                        .clickable { onSwitchCurrencyMode(CurrencyMode.FIAT) },
                    amountInBtc = walletBalance ?: BigDecimal.ZERO,
                    textSize = 32.sp,
                )
            }
        }

        WalletActionsRow(
            modifier = Modifier,
            actions = actions,
            actionSize = 48.dp,
            showLabels = false,
            onWalletAction = onWalletAction,
            enabled = enabled,
        )
    }
}
