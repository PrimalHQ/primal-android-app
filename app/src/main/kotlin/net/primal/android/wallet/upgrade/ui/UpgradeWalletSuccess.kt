package net.primal.android.wallet.upgrade.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.ApplyEdgeToEdge
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.WalletSuccess
import net.primal.android.theme.AppTheme
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
                .padding(bottom = 32.dp)
                .padding(horizontal = 32.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    modifier = Modifier.size(160.dp),
                    imageVector = PrimalIcons.WalletSuccess,
                    contentDescription = null,
                    tint = walletSuccessContentColor,
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(id = R.string.wallet_upgrade_success_headline),
                    textAlign = TextAlign.Center,
                    style = AppTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = Color.White,
                    ),
                )
            }

            PrimalLoadingButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.wallet_upgrade_done_button),
                containerColor = walletSuccessDimColor,
                onClick = onDoneClick,
            )
        }
    }
}
