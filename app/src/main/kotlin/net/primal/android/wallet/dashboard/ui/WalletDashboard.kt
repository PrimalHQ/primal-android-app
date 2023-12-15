package net.primal.android.wallet.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import net.primal.android.user.domain.PrimalWallet
import net.primal.android.user.domain.WalletPreference

@Composable
fun WalletDashboard(
    modifier: Modifier,
    walletBalance: BigDecimal?,
    primalWallet: PrimalWallet,
    walletPreference: WalletPreference,
    actions: List<WalletAction>,
    onWalletAction: (WalletAction) -> Unit,
    onWalletPreferenceChanged: (WalletPreference) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WalletBalanceText(
            modifier = Modifier.wrapContentWidth()
                .padding(start = if (walletBalance != null) 32.dp else 0.dp)
                .padding(bottom = 32.dp),
            walletBalance,
        )

        WalletActionsRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            actionSize = 80.dp,
            actions = actions,
            onWalletAction = onWalletAction,
        )

//        Text(
//            modifier = Modifier.padding(vertical = 16.dp),
//            text = "${primalWallet.lightningAddress} (pref=$walletPreference)",
//            textAlign = TextAlign.Center,
//            color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
//            style = AppTheme.typography.bodyMedium,
//        )

//        Row(
//            horizontalArrangement = Arrangement.SpaceEvenly,
//        ) {
//            PrimalLoadingButton(
//                modifier = Modifier.padding(horizontal = 4.dp),
//                text = "Prefer Primal",
//                onClick = {
//                    onWalletPreferenceChanged(WalletPreference.PrimalWallet)
//                },
//            )
//
//            PrimalLoadingButton(
//                modifier = Modifier.padding(horizontal = 4.dp),
//                text = "Prefer NWC",
//                onClick = {
//                    onWalletPreferenceChanged(WalletPreference.NostrWalletConnect)
//                },
//            )
//        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
