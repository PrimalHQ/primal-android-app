package net.primal.android.wallet.store.inapp

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.R
import net.primal.android.core.compose.AdjustTemporarilySystemBarColors
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.findActivity
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.store.domain.SatsPurchaseQuote
import net.primal.android.wallet.utils.CurrencyConversionUtils.toSats

@ExperimentalMaterial3Api
@Composable
fun InAppPurchaseBuyBottomSheet(onDismiss: () -> Unit) {
    val viewModel = hiltViewModel<InAppPurchaseBuyViewModel>()
    val uiState = viewModel.state.collectAsState()

    BackHandler {
        viewModel.setEvent(InAppPurchaseBuyContract.UiEvent.ClearQuote)
        onDismiss()
    }

    LaunchedEffect(viewModel, onDismiss) {
        withContext(Dispatchers.IO) {
            while (true) {
                viewModel.setEvent(InAppPurchaseBuyContract.UiEvent.RefreshQuote)
                delay(1.minutes)
            }
        }
    }

    LaunchedEffect(viewModel, onDismiss) {
        viewModel.effects.collect {
            when (it) {
                InAppPurchaseBuyContract.SideEffect.PurchaseConfirmed -> onDismiss()
            }
        }
    }

    LaunchedErrorHandler(viewModel = viewModel)

    InAppPurchaseBuyBottomSheet(
        state = uiState.value,
        eventPublisher = { viewModel.setEvent(it) },
        onDismiss = {
            viewModel.setEvent(InAppPurchaseBuyContract.UiEvent.ClearQuote)
            onDismiss()
        },
    )
}

@ExperimentalMaterial3Api
@Composable
fun InAppPurchaseBuyBottomSheet(
    state: InAppPurchaseBuyContract.UiState,
    eventPublisher: (InAppPurchaseBuyContract.UiEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    val activity = LocalContext.current.findActivity()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    AdjustTemporarilySystemBarColors(
        navigationBarColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
    )
    ModalBottomSheet(
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        sheetState = sheetState,
        tonalElevation = 0.dp,
        onDismissRequest = onDismiss,
    ) {
        if (activity != null && state.inAppSupported) {
            PurchaseQuoteColumn(
                quote = state.quote,
                onPurchaseRequest = {
                    eventPublisher(InAppPurchaseBuyContract.UiEvent.RequestPurchase(activity))
                },
            )
        } else {
            InAppPurchaseNotSupportedNotice()
        }
    }
}

@Composable
private fun PurchaseQuoteColumn(quote: SatsPurchaseQuote?, onPurchaseRequest: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CurrencyText(
            modifier = Modifier.padding(bottom = 48.dp),
            topLabel = stringResource(id = R.string.wallet_in_app_purchase_pay_label).uppercase(),
            amount = quote?.purchaseAmount,
            currency = quote?.purchaseCurrency,
        )

        CurrencyText(
            modifier = Modifier.padding(bottom = 48.dp),
            topLabel = stringResource(id = R.string.wallet_in_app_purchase_to_receive_label).uppercase(),
            amount = quote?.amountInBtc?.toSats()?.toString(),
            currency = stringResource(id = R.string.wallet_sats_suffix),
        )

        PrimalFilledButton(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.8f)
                .height(56.dp),
            enabled = quote != null,
            onClick = { onPurchaseRequest() },
        ) {
            Text(text = stringResource(id = R.string.wallet_in_app_purchase_now_button))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CurrencyText(
    topLabel: String,
    amount: String?,
    currency: String?,
    modifier: Modifier = Modifier,
    symbol: String? = null,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
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
            if (amount != null) {
                Text(
                    text = if (symbol != null) {
                        "$symbol ${numberFormat.format(amount.toDouble())}"
                    } else {
                        numberFormat.format(amount.toDouble())
                    },
                    textAlign = TextAlign.Center,
                    style = AppTheme.typography.displaySmall,
                    color = AppTheme.colorScheme.onSurface,
                )

                if (currency != null) {
                    Text(
                        modifier = Modifier.padding(top = 12.dp),
                        text = " $currency",
                        style = AppTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    )
                }
            } else {
                PrimalLoadingSpinner(
                    paddingValues = PaddingValues(all = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun InAppPurchaseNotSupportedNotice() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(fraction = 0.8f),
            text = stringResource(id = R.string.wallet_in_app_purchase_not_supported),
            textAlign = TextAlign.Center,
            style = AppTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun LaunchedErrorHandler(viewModel: InAppPurchaseBuyViewModel) {
    val genericMessage = stringResource(id = R.string.app_generic_error)
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    LaunchedEffect(viewModel) {
        viewModel.state
            .filter { it.error != null }
            .map { it.error }
            .filterNotNull()
            .collect {
                uiScope.launch {
                    Toast.makeText(
                        context,
                        genericMessage,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }
}
