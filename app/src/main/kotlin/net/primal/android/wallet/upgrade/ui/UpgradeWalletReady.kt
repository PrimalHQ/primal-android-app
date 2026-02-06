package net.primal.android.wallet.upgrade.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.theme.AppTheme

@Composable
fun UpgradeWalletReady(modifier: Modifier = Modifier, onStartUpgrade: () -> Unit) {
    Column(
        modifier = modifier.padding(top = 80.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.8f)
                    .padding(bottom = 16.dp),
                text = stringResource(id = R.string.wallet_upgrade_ready_headline),
                textAlign = TextAlign.Center,
                style = AppTheme.typography.headlineSmall,
            )
            Text(
                modifier = Modifier.fillMaxWidth(fraction = 0.8f),
                text = stringResource(id = R.string.wallet_upgrade_ready_description),
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodyLarge.copy(
                    lineHeight = 28.sp,
                ),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            )
        }

        PrimalLoadingButton(
            modifier = Modifier
                .width(200.dp)
                .padding(bottom = 16.dp),
            text = stringResource(id = R.string.wallet_upgrade_start_button),
            onClick = onStartUpgrade,
        )
    }
}
