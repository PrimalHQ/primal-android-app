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
import java.math.BigDecimal
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.utils.CurrencyConversionUtils.toUsd

@Composable
fun FiatAmountText(
    modifier: Modifier,
    amount: BigDecimal?,
    textSize: TextUnit = 42.sp,
    amountColor: Color = Color.Unspecified,
    currencyColor: Color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
    exchangeBtcUsdRate: Double?,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    Row(
        modifier = modifier
            .animateContentSize(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Start,
    ) {
        if (amount != null) {
            Text(
                modifier = Modifier.padding(bottom = (textSize.value / 2 - 5).dp),
                text = "${stringResource(id = R.string.wallet_usd_prefix)} ",
                textAlign = TextAlign.Center,
                maxLines = 1,
                style = AppTheme.typography.bodyMedium,
                fontSize = textSize / 2,
                color = currencyColor,
            )
        }

        Text(
            text = amount?.toUsd(exchangeBtcUsdRate)?.let { numberFormat.format(it.toFloat()) } ?: "⌛",
            textAlign = TextAlign.Center,
            style = AppTheme.typography.displayMedium,
            fontSize = textSize,
            color = amountColor,
        )

        if (amount != null) {
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
}
