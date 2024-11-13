package net.primal.android.premium.manage.order

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.format.FormatStyle
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.utils.formatToDefaultDateFormat
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.utils.CurrencyConversionUtils.toSats

@Composable
fun PremiumOrderHistoryScreen(viewModel: PremiumOrderHistoryViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()

    PremiumOrderHistoryScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun PremiumOrderHistoryScreen(
    state: PremiumOrderHistoryContract.UiState,
    eventPublisher: (PremiumOrderHistoryContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.premium_order_history_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        bottomBar = {
            if (state.isRecurringSubscription) {
                CancelSubscriptionButton(
                    cancelling = state.cancellingSubscription,
                    onCancelConfirmed = {
                        eventPublisher(PremiumOrderHistoryContract.UiEvent.CancelSubscription)
                    },
                )
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
                .padding(top = 16.dp)
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.Top)
                .background(
                    color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                    shape = AppTheme.shapes.large,
                ),
        ) {
            stickyHeader {
                Column {
                    TableRow(
                        date = stringResource(R.string.premium_order_history_date_header),
                        label = stringResource(R.string.premium_order_history_purchase_header),
                        amount = stringResource(R.string.premium_order_history_amount_header),
                        fontWeight = FontWeight.SemiBold,
                    )
                    PrimalDivider()
                }
            }

            items(state.orders) {
                Column {
                    TableRow(
                        date = Instant.ofEpochSecond(it.purchasedAt).formatToDefaultDateFormat(
                            FormatStyle.MEDIUM,
                        ),
                        label = it.productLabel,
                        amount = if (it.amountUsd != null) {
                            "$${it.amountUsd} USD"
                        } else if (it.amountBtc != null) {
                            "${it.amountBtc.toBigDecimal().toSats()} sats"
                        } else {
                            ""
                        },
                    )
                    PrimalDivider()
                }
            }
        }
    }
}

@Composable
private fun TableRow(
    date: String,
    label: String,
    amount: String,
    fontWeight: FontWeight = FontWeight.Normal,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Text(
            modifier = Modifier.weight(0.3f),
            text = date,
            style = AppTheme.typography.bodyMedium,
            fontSize = 15.sp,
            color = AppTheme.colorScheme.onSurface,
            fontWeight = fontWeight,
        )

        Text(
            modifier = Modifier
                .weight(0.4f)
                .padding(start = 16.dp, end = 32.dp),
            text = label,
            style = AppTheme.typography.bodyMedium,
            fontSize = 15.sp,
            color = AppTheme.colorScheme.onSurface,
            fontWeight = fontWeight,
        )

        Text(
            modifier = Modifier.weight(0.3f),
            text = amount,
            style = AppTheme.typography.bodyMedium,
            fontSize = 15.sp,
            color = AppTheme.colorScheme.onSurface,
            fontWeight = fontWeight,
        )
    }
}

@Composable
private fun CancelSubscriptionButton(cancelling: Boolean, onCancelConfirmed: () -> Unit) {
    var showCancelSubscriptionDialog by remember { mutableStateOf(false) }
    if (showCancelSubscriptionDialog) {
        CancelSubscriptionAlertDialog(
            onDismissRequest = { showCancelSubscriptionDialog = false },
            onConfirm = {
                showCancelSubscriptionDialog = false
                onCancelConfirmed()
            },
        )
    }

    PrimalLoadingButton(
        loading = cancelling,
        enabled = !cancelling,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        text = stringResource(R.string.premium_order_history_cancel_subscription_button),
    )
}

@Composable
private fun CancelSubscriptionAlertDialog(onDismissRequest: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        containerColor = AppTheme.colorScheme.surfaceVariant,
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(R.string.premium_order_history_cancel_dialog_title),
            )
        },
        text = {
            Text(
                text = stringResource(R.string.premium_order_history_cancel_dialog_text),
            )
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = stringResource(R.string.premium_order_history_cancel_dialog_dismiss_button),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.premium_order_history_cancel_dialog_confirm_button),
                )
            }
        },
    )
}
