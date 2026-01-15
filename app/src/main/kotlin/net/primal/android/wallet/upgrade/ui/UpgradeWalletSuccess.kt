package net.primal.android.wallet.upgrade.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.ApplyEdgeToEdge
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.WalletSuccess
import net.primal.android.wallet.ui.FlowStatusColumn
import net.primal.android.wallet.walletSuccessColor
import net.primal.android.wallet.walletSuccessContentColor
import net.primal.android.wallet.walletSuccessDimColor

@ExperimentalMaterial3Api
@Composable
fun UpgradeWalletSuccess(modifier: Modifier, onDoneClick: () -> Unit) {
    ApplyEdgeToEdge(isDarkTheme = true)

    Column(modifier = modifier.background(color = walletSuccessColor)) {
        PrimalTopAppBar(
            title = stringResource(id = R.string.wallet_upgrade_success_title),
            textColor = walletSuccessContentColor,
            showDivider = false,
            navigationIconTintColor = walletSuccessContentColor,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = walletSuccessColor,
                scrolledContainerColor = walletSuccessColor,
                titleContentColor = walletSuccessContentColor,
            ),
        )
        Column(
            modifier = Modifier
                .padding(top = 80.dp, bottom = 32.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FlowStatusColumn(
                icon = PrimalIcons.WalletSuccess,
                iconTint = walletSuccessContentColor,
                headlineText = stringResource(id = R.string.wallet_upgrade_success_headline),
                supportText = null,
                textColor = walletSuccessContentColor,
            )

            PrimalLoadingButton(
                modifier = Modifier
                    .width(200.dp)
                    .padding(bottom = 16.dp),
                text = stringResource(id = R.string.wallet_upgrade_done_button),
                containerColor = walletSuccessDimColor,
                onClick = onDoneClick,
            )
        }
    }
}
