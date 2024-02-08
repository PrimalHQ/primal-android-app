package net.primal.android.wallet.transactions.send.create.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.WalletError
import net.primal.android.theme.AppTheme

@Composable
fun TransactionFailed(
    modifier: Modifier,
    errorMessage: String,
    onCloseClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TransactionStatusColumn(
            icon = PrimalIcons.WalletError,
            iconTint = AppTheme.colorScheme.error,
            headlineText = stringResource(id = R.string.wallet_create_transaction_failed_headline),
            supportText = errorMessage,
        )

        PrimalLoadingButton(
            modifier = Modifier.width(200.dp),
            text = stringResource(id = R.string.wallet_create_transaction_close_button),
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
            onClick = onCloseClick,
        )
    }
}
