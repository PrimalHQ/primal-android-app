package net.primal.android.wallet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.WalletChangeCurrency
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.domain.CurrencyMode
import net.primal.android.wallet.transactions.send.create.ui.formatSats
import net.primal.android.wallet.transactions.send.create.ui.formatUsd

@Composable
fun TransactionAmountSubtext(
    currencyMode: CurrencyMode,
    amountSats: String,
    amountUsd: String,
) {
    val amount = when (currencyMode) {
        CurrencyMode.FIAT -> {
            amountSats
        }
        CurrencyMode.SATS -> {
            amountUsd
        }
    }

    Row(
        modifier = Modifier.padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val numberFormat = remember { NumberFormat.getNumberInstance() }

        Text(
            text = if (currencyMode != CurrencyMode.FIAT) {
                amount.formatUsd(numberFormat)
            } else {
                amount.formatSats(numberFormat)
            },
            textAlign = TextAlign.Center,
            maxLines = 1,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
        )

        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = PrimalIcons.WalletChangeCurrency,
            contentDescription = null,
            tint = AppTheme.colorScheme.tertiary,
        )
    }
}
