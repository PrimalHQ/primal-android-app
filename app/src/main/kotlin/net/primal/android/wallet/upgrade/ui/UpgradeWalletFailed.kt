package net.primal.android.wallet.upgrade.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.WalletError
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.ui.FlowStatusColumn

@Composable
fun UpgradeWalletFailed(
    modifier: Modifier,
    errorMessage: String,
    onRetryClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    Column(
        modifier = modifier.padding(top = 80.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FlowStatusColumn(
            icon = PrimalIcons.WalletError,
            iconTint = AppTheme.colorScheme.error,
            headlineText = stringResource(id = R.string.wallet_upgrade_failed_headline),
            supportText = errorMessage,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PrimalLoadingButton(
                modifier = Modifier.width(200.dp),
                text = stringResource(id = R.string.wallet_upgrade_retry_button),
                onClick = onRetryClick,
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onCloseClick) {
                Text(
                    text = stringResource(id = R.string.wallet_upgrade_close_button),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    style = AppTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}
