package net.primal.android.wallet.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import net.primal.android.wallet.dashboard.ui.BtcAmountText
import net.primal.android.wallet.dashboard.ui.FiatAmountTextFromUsd
import net.primal.android.wallet.domain.CurrencyMode
import net.primal.android.wallet.repository.isValidExchangeRate
import net.primal.core.utils.CurrencyConversionUtils.toSats

@Composable
fun TransactionAmountText(
    currentExchangeRate: Double?,
    currentCurrencyMode: CurrencyMode,
    amountInBtc: String,
    amountInUsd: String,
    onAmountClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {
                onAmountClick()
            },
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (currentCurrencyMode) {
            CurrencyMode.SATS -> {
                BtcAmountText(
                    modifier = Modifier
                        .padding(start = 32.dp)
                        .height(72.dp),
                    amountInBtc = BigDecimal.parseString(amountInBtc),
                    textSize = 48.sp,
                )
            }

            CurrencyMode.FIAT -> {
                FiatAmountTextFromUsd(
                    modifier = Modifier
                        .padding(start = 32.dp)
                        .height(72.dp),
                    amount = amountInUsd,
                    textSize = 48.sp,
                )
            }
        }

        if (currentExchangeRate.isValidExchangeRate()) {
            TransactionAmountSubtext(
                currencyMode = currentCurrencyMode,
                amountSats = amountInBtc.toSats().toString(),
                amountUsd = amountInUsd,
            )
        }
    }
}
