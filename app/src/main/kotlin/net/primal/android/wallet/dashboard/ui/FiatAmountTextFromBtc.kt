package net.primal.android.wallet.dashboard.ui

import androidx.compose.animation.animateContentSize
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
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.theme.AppTheme
import net.primal.core.utils.CurrencyConversionUtils.toUsd

@Composable
fun FiatAmountTextFromBtc(
    modifier: Modifier,
    amount: BigDecimal?,
    textSize: TextUnit = 42.sp,
    amountColor: Color = Color.Unspecified,
    currencyColor: Color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
    exchangeBtcUsdRate: Double?,
) {
    val formattedAmount = amount?.toUsd(exchangeBtcUsdRate)?.toPlainString() ?: "⌛"
    SharedFiatAmountDisplay(
        modifier = modifier,
        amount = formattedAmount,
        textSize = textSize,
        amountColor = amountColor,
        currencyColor = currencyColor,
    )
}

@Composable
fun FiatAmountTextFromUsd(
    modifier: Modifier,
    amount: String?,
    textSize: TextUnit = 42.sp,
    amountColor: Color = Color.Unspecified,
    currencyColor: Color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    val formattedAmount = amount?.let {
        if (it.contains(".")) {
            val integerPart = it.substringBefore('.')
            val decimalPart = it.substringAfter('.')
            numberFormat.format(integerPart.toFloat()) + ".$decimalPart"
        } else {
            numberFormat.format(it.toFloat())
        }
    } ?: "⌛"

    SharedFiatAmountDisplay(
        modifier = modifier.padding(end = (textSize.value / 3).dp),
        amount = formattedAmount,
        textSize = textSize,
        amountColor = amountColor,
        currencyColor = currencyColor,
    )
}

@Composable
private fun SharedFiatAmountDisplay(
    modifier: Modifier,
    amount: String,
    textSize: TextUnit,
    amountColor: Color,
    currencyColor: Color,
) {
    Row(
        modifier = modifier
            .animateContentSize(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            modifier = Modifier.padding(bottom = (textSize.value / 2 - 5).dp),
            text = "${stringResource(id = R.string.wallet_usd_prefix)} ",
            textAlign = TextAlign.Center,
            maxLines = 1,
            style = AppTheme.typography.bodyMedium,
            fontSize = textSize / 2,
            color = currencyColor,
        )

        Text(
            text = amount,
            textAlign = TextAlign.Center,
            style = AppTheme.typography.displayMedium,
            fontSize = textSize,
            color = amountColor,
        )

        Text(
            modifier = Modifier.padding(bottom = (textSize.value / 6).dp),
            text = " ${stringResource(id = R.string.wallet_usd_suffix)}",
            textAlign = TextAlign.Center,
            maxLines = 1,
            style = AppTheme.typography.bodyMedium,
            color = currencyColor,
        )
    }
}
