package net.primal.android.wallet.store.inapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import net.primal.android.R
import net.primal.android.core.compose.AdjustTemporarilySystemBarColors
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.theme.AppTheme

@ExperimentalMaterial3Api
@Composable
fun InAppPurchaseBuyBottomSheet(onDismiss: () -> Unit) {
    val viewModel = hiltViewModel<InAppPurchaseBuyViewModel>()
    val uiState = viewModel.state.collectAsState()
    InAppPurchaseBuyBottomSheet(
        state = uiState.value,
        onDismiss = onDismiss,
    )
}

@ExperimentalMaterial3Api
@Composable
fun InAppPurchaseBuyBottomSheet(state: InAppPurchaseBuyContract.UiState, onDismiss: () -> Unit) {
    AdjustTemporarilySystemBarColors(
        navigationBarColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
    )
    ModalBottomSheet(
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        tonalElevation = 0.dp,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CurrencyText(
                modifier = Modifier.padding(bottom = 48.dp),
                topLabel = stringResource(id = R.string.wallet_in_app_purchase_pay_label).uppercase(),
                amount = "4.99",
                currency = "USD",
            )

            CurrencyText(
                modifier = Modifier.padding(bottom = 48.dp),
                topLabel = stringResource(id = R.string.wallet_in_app_purchase_to_receive_label).uppercase(),
                amount = "10.888",
                currency = "sats",
            )

            PrimalFilledButton(
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.8f)
                    .height(56.dp),
                onClick = onDismiss,
            ) {
                Text(text = stringResource(id = R.string.wallet_in_app_purchase_now_button))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CurrencyText(
    topLabel: String,
    amount: String,
    currency: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(fraction = 0.6f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 4.dp),
            text = topLabel,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            style = AppTheme.typography.bodyMedium,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                    shape = AppTheme.shapes.large,
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = amount,
                textAlign = TextAlign.Center,
                style = AppTheme.typography.displaySmall,
            )

            Text(
                modifier = Modifier.padding(top = 12.dp),
                text = currency,
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
        }
    }
}
