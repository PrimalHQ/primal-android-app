package net.primal.android.wallet.dashboard.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.dashboard.CurrencyMode
import net.primal.android.wallet.utils.CurrencyConversionUtils.toSats
import net.primal.android.wallet.utils.CurrencyConversionUtils.toUsd

@Composable
fun AmountText(
    modifier: Modifier,
    amount: BigDecimal?,
    textSize: TextUnit = 42.sp,
    amountColor: Color = Color.Unspecified,
    currencyColor: Color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
    currencyMode: CurrencyMode,
    onSwitchCurrencyMode: (currencyMode: CurrencyMode) -> Unit,
    exchangeBtcUsdRate: Double?,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    Row(
        modifier = modifier
            .clickable {
                if (currencyMode == CurrencyMode.SATOSHI) {
                    onSwitchCurrencyMode(CurrencyMode.USD)
                } else {
                    onSwitchCurrencyMode(CurrencyMode.SATOSHI)
                }
            }
            .animateContentSize(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = if (currencyMode == CurrencyMode.SATOSHI) {
                amount?.toSats()?.let { numberFormat.format(it.toLong()) }
            } else {
                amount?.toUsd(exchangeBtcUsdRate)?.let { numberFormat.format(it.toFloat()) }
            } ?: "⌛",
            textAlign = TextAlign.Center,
            style = AppTheme.typography.displayMedium,
            fontSize = textSize,
            color = amountColor,
        )

        if (amount != null) {
            Text(
                modifier = Modifier.padding(bottom = (textSize.value / 6).dp),
                text = if (currencyMode == CurrencyMode.SATOSHI) {
                    " ${stringResource(id = R.string.wallet_sats_suffix)}"
                } else {
                    " ${stringResource(id = R.string.wallet_usd_suffix)}"
                },
                textAlign = TextAlign.Center,
                maxLines = 1,
                style = AppTheme.typography.bodyMedium,
                color = currencyColor,
            )
        }
    }
}
