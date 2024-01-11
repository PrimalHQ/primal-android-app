package net.primal.android.wallet.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.utils.CurrencyConversionUtils.toSats

@Composable
fun AmountText(
    modifier: Modifier,
    amountInBtc: BigDecimal?,
    textSize: TextUnit = 42.sp,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = amountInBtc?.toSats()?.let { numberFormat.format(it.toLong()) } ?: "⌛",
            textAlign = TextAlign.Center,
            style = AppTheme.typography.displayMedium,
            fontSize = textSize,
        )

        if (amountInBtc != null) {
            Text(
                modifier = Modifier.padding(bottom = (textSize.value / 6).dp),
                text = " ${stringResource(id = R.string.wallet_sats_suffix)}",
                textAlign = TextAlign.Center,
                maxLines = 1,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
        }
    }
}
