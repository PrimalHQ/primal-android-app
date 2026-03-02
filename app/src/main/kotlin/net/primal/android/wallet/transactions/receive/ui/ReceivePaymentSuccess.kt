package net.primal.android.wallet.transactions.receive.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.activity.LocalPrimalTheme
import net.primal.android.core.compose.ApplyEdgeToEdge
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.applyEdgeToEdge
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.WalletSuccess
import net.primal.android.wallet.ui.FlowStatusColumn
import net.primal.android.wallet.walletSuccessColor
import net.primal.android.wallet.walletSuccessContentColor
import net.primal.android.wallet.walletSuccessDimColor

@ExperimentalMaterial3Api
@Composable
fun ReceivePaymentSuccess(
    modifier: Modifier = Modifier,
    amountInSats: Long,
    onDoneClick: () -> Unit,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    val context = LocalContext.current
    val primalTheme = LocalPrimalTheme.current

    fun closingSequence() {
        onDoneClick()
        (context as ComponentActivity).applyEdgeToEdge(isDarkTheme = primalTheme.isDarkTheme)
    }

    ApplyEdgeToEdge(isDarkTheme = true)

    BackHandler {
        closingSequence()
    }

    val formattedAmount = numberFormat.format(amountInSats)

    Column(modifier = modifier.background(color = walletSuccessColor)) {
        PrimalTopAppBar(
            title = stringResource(id = R.string.wallet_receive_transaction_success_title),
            textColor = walletSuccessContentColor,
            navigationIcon = PrimalIcons.ArrowBack,
            navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
            showDivider = false,
            onNavigationIconClick = { closingSequence() },
            navigationIconTintColor = walletSuccessContentColor,
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = walletSuccessColor,
                scrolledContainerColor = walletSuccessColor,
                titleContentColor = walletSuccessContentColor,
            ),
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FlowStatusColumn(
                icon = PrimalIcons.WalletSuccess,
                iconTint = walletSuccessContentColor,
                headlineText = stringResource(
                    id = R.string.wallet_receive_transaction_success_headline,
                    formattedAmount,
                ),
                supportText = null,
                textColor = walletSuccessContentColor,
            )

            PrimalLoadingButton(
                modifier = Modifier
                    .width(200.dp)
                    .padding(bottom = 16.dp),
                text = stringResource(id = R.string.wallet_create_transaction_close_button),
                containerColor = walletSuccessDimColor,
                onClick = { closingSequence() },
            )
        }
    }
}
