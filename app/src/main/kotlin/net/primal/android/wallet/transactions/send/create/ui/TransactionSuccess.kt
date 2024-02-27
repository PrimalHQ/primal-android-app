package net.primal.android.wallet.transactions.send.create.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.core.compose.ApplySystemBarColors
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.applySystemColors
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.WalletSuccess
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.walletSuccessColor
import net.primal.android.wallet.walletSuccessContentColor
import net.primal.android.wallet.walletSuccessDimColor

@ExperimentalMaterial3Api
@Composable
fun TransactionSuccess(
    modifier: Modifier,
    amountInSats: Long,
    receiver: String?,
    onDoneClick: () -> Unit,
) {
    var isClosing by remember { mutableStateOf(false) }
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    val primalTheme = LocalPrimalTheme.current
    val localView = LocalView.current

    val closingSequence = {
        applySystemColors(primalTheme = primalTheme, localView = localView)
        isClosing = true
        onDoneClick()
    }

    val backgroundColor = if (!isClosing) walletSuccessColor else AppTheme.colorScheme.surface

    ApplySystemBarColors(statusBarColor = walletSuccessColor, navigationBarColor = walletSuccessColor)

    BackHandler {
        closingSequence()
    }

    Column(modifier = modifier.background(color = backgroundColor)) {
        PrimalTopAppBar(
            modifier = if (isClosing) Modifier.alpha(0.0f) else Modifier,
            title = stringResource(id = R.string.wallet_create_transaction_success_title),
            textColor = walletSuccessContentColor,
            navigationIcon = PrimalIcons.ArrowBack,
            navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
            showDivider = false,
            onNavigationIconClick = { closingSequence() },
            navigationIconTintColor = if (!isClosing) walletSuccessContentColor else AppTheme.colorScheme.onSurface,
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = backgroundColor,
                scrolledContainerColor = backgroundColor,
            ),
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TransactionStatusColumn(
                icon = PrimalIcons.WalletSuccess,
                iconTint = walletSuccessContentColor,
                headlineText = stringResource(id = R.string.wallet_create_transaction_success_headline),
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
                textColor = walletSuccessContentColor,
            )

            PrimalLoadingButton(
                modifier = Modifier
                    .width(200.dp)
                    .padding(bottom = 16.dp)
                    .then(if (isClosing) Modifier.alpha(0.0f) else Modifier),
                text = stringResource(id = R.string.wallet_create_transaction_done_button),
                containerColor = walletSuccessDimColor,
                onClick = { closingSequence() },
            )
        }
    }
}
