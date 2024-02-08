package net.primal.android.wallet.transactions.send.create.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.compose.button.PrimalLoadingButton

@Composable
fun TransactionSending(
    modifier: Modifier,
    amountInSats: Long,
    receiver: String?,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TransactionStatusColumn(
            icon = null,
            headlineText = stringResource(id = R.string.wallet_create_transaction_sending_headline),
            supportText = if (receiver != null) {
                stringResource(
                    id = R.string.wallet_create_transaction_transaction_description,
                    numberFormat.format(amountInSats),
                    receiver,
                )
            } else {
                stringResource(
                    id = R.string.wallet_create_transaction_transaction_description_lite,
                    numberFormat.format(amountInSats),
                )
            },
        )

        PrimalLoadingButton(
            modifier = Modifier
                .width(200.dp)
                .alpha(0f),
        )
    }
}
