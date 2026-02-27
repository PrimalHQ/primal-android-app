package net.primal.android.settings.wallet.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.core.utils.CurrencyConversionUtils.toSats

private val WalletBackupColor = Color(0xFFFE3D2F)

@Composable
fun WalletBackupWidget(
    walletBalanceInBtc: String?,
    onBackupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    val sats = walletBalanceInBtc?.toSats()?.let { numberFormat.format(it.toLong()) } ?: "0"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = WalletBackupColor.copy(alpha = 0.20f),
                shape = AppTheme.shapes.large,
            )
            .background(
                color = WalletBackupColor.copy(alpha = 0.12f),
                shape = AppTheme.shapes.large,
            )
            .padding(all = 20.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = stringResource(id = R.string.settings_wallet_backup_balance, sats),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.settings_wallet_backup_subtitle),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrimalLoadingButton(
            text = stringResource(id = R.string.settings_wallet_backup_button),
            onClick = onBackupClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            containerColor = WalletBackupColor,
            contentColor = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Preview
@Composable
private fun PreviewWalletBackupWidget() {
    PrimalTheme(primalTheme = PrimalTheme.Midnight) {
        WalletBackupWidget(
            walletBalanceInBtc = "0.00010101",
            onBackupClick = {},
        )
    }
}
