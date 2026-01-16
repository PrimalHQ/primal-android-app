package net.primal.android.wallet.upgrade.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import net.primal.android.wallet.ui.FlowStatusColumn

@Composable
fun UpgradeWalletFailed(
    modifier: Modifier,
    errorMessage: String,
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

        PrimalLoadingButton(
            modifier = Modifier
                .width(200.dp)
                .padding(bottom = 16.dp),
            text = stringResource(id = R.string.wallet_upgrade_close_button),
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
            onClick = onCloseClick,
        )
    }
}
