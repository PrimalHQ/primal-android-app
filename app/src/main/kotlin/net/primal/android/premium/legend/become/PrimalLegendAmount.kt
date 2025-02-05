package net.primal.android.premium.legend.become

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*
import net.primal.android.premium.legend.become.PremiumBecomeLegendContract.Companion.LEGEND_THRESHOLD_IN_USD
import net.primal.android.premium.legend.become.amount.AltAmountText
import net.primal.android.premium.legend.become.amount.MainAmountText

@Composable
fun PrimalLegendAmount(
    btcValue: BigDecimal,
    exchangeBtcUsdRate: Double?,
    coerceMinAmount: Boolean = false,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MainAmountText(
            modifier = Modifier
                .padding(start = 32.dp)
                .alpha(alpha = if (btcValue == BigDecimal.ZERO) 0.5f else 1.0f),
            amount = String.format(Locale.US, "%.8f", btcValue),
            currency = "BTC",
            textSize = 44.sp,
        )

        val usdValue = if (exchangeBtcUsdRate != null) {
            btcValue * exchangeBtcUsdRate.toBigDecimal()
        } else {
            null
        }

        if (usdValue != null) {
            val numberFormat = remember { NumberFormat.getNumberInstance() }

            AltAmountText(
                modifier = Modifier.padding(top = 8.dp),
                amount = if (coerceMinAmount) {
                    numberFormat.format(usdValue.toInt())
                } else {
                    numberFormat.format(usdValue.toInt().coerceAtLeast(minimumValue = LEGEND_THRESHOLD_IN_USD))
                },
                currency = "USD",
            )
        }
    }
}
