package net.primal.android.premium.manage.order

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import java.time.Instant
import java.time.format.FormatStyle
import kotlinx.datetime.Clock
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.utils.formatToDefaultDateFormat
import net.primal.android.core.utils.isGoogleBuild
import net.primal.android.premium.buying.home.PrimalPremiumLogoHeader
import net.primal.android.premium.ui.ManagePremiumTableRow
import net.primal.android.premium.utils.isOriginAndroid
import net.primal.android.premium.utils.isOriginIOS
import net.primal.android.theme.AppTheme
import net.primal.core.utils.CurrencyConversionUtils.toSats

@Composable
fun PremiumOrderHistoryScreen(
    viewModel: PremiumOrderHistoryViewModel,
    onExtendSubscription: (primalName: String) -> Unit,
    onClose: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    PremiumOrderHistoryScreen(
        state = uiState.value,
        onClose = onClose,
        onExtendSubscription = onExtendSubscription,
        eventPublisher = viewModel::setEvent,
    )
}

private const val DateWeight = 0.3f
private const val PurchaseWeight = 0.4f
private const val AmountWeight = 0.3f

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun PremiumOrderHistoryScreen(
    state: PremiumOrderHistoryContract.UiState,
    eventPublisher: (PremiumOrderHistoryContract.UiEvent) -> Unit,
    onExtendSubscription: (primalName: String) -> Unit,
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
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
                .padding(top = 16.dp)
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.Top),
        ) {
            item {
                SubscriptionHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = AppTheme.extraColorScheme.surfaceVariantAlt3,
                            shape = AppTheme.shapes.large,
                        )
                        .padding(top = 18.dp, bottom = 6.dp),
                    state = state,
                    onExtendSubscription = onExtendSubscription,
                    onCancelSubscription = {
                        eventPublisher(PremiumOrderHistoryContract.UiEvent.CancelSubscription)
                    },
                )
            }

            item {
                Text(
                    modifier = Modifier.padding(bottom = 8.dp, top = 24.dp),
                    text = stringResource(R.string.premium_order_history_header),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    style = AppTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                )
            }

            stickyHeader {
                Column(
                    modifier = Modifier
                        .background(
                            color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                            shape = AppTheme.shapes.large.copy(
                                bottomEnd = CornerSize(0.dp),
                                bottomStart = CornerSize(0.dp),
                            ),
                        ),
                ) {
                    ManagePremiumTableRow(
                        firstColumn = stringResource(R.string.premium_order_history_date_header),
                        firstColumnWeight = DateWeight,
                        secondColumn = stringResource(R.string.premium_order_history_purchase_header),
                        secondColumnWeight = PurchaseWeight,
                        thirdColumn = stringResource(R.string.premium_order_history_amount_header),
                        thirdColumnWeight = AmountWeight,
                        fontWeight = FontWeight.SemiBold,
                    )
                    PrimalDivider()
                }
            }

            itemsIndexed(state.orders) { index, item ->
                val isLastItem = index == state.orders.size - 1
                Column(
                    modifier = Modifier.background(
                        color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                        shape = if (isLastItem) {
                            AppTheme.shapes.large.copy(
                                topEnd = CornerSize(0.dp),
                                topStart = CornerSize(0.dp),
                            )
                        } else {
                            RectangleShape
                        },
                    ),
                ) {
                    ManagePremiumTableRow(
                        firstColumn = Instant.ofEpochSecond(item.purchasedAt).formatToDefaultDateFormat(
                            FormatStyle.MEDIUM,
                        ),
                        firstColumnWeight = DateWeight,
                        secondColumn = item.productLabel,
                        secondColumnWeight = PurchaseWeight,
                        thirdColumn = if (item.amountUsd != null) {
                            "$${item.amountUsd} USD"
                        } else if (item.amountBtc != null) {
                            "${item.amountBtc.toBigDecimal().toSats()} sats"
                        } else {
                            ""
                        },
                        thirdColumnWeight = AmountWeight,
                    )

                    if (!isLastItem) {
                        PrimalDivider()
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

@Composable
private fun SubscriptionHeader(
    modifier: Modifier,
    state: PremiumOrderHistoryContract.UiState,
    onExtendSubscription: (primalName: String) -> Unit,
    onCancelSubscription: () -> Unit,
) {
    var showCancelSubscriptionDialog by remember { mutableStateOf(false) }
    if (showCancelSubscriptionDialog) {
        CancelSubscriptionAlertDialog(
            onDismissRequest = { showCancelSubscriptionDialog = false },
            onConfirm = {
                showCancelSubscriptionDialog = false
                onCancelSubscription()
            },
        )
    }

    Column(
        modifier = modifier,
    ) {
        PrimalPremiumLogoHeader(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .padding(horizontal = 18.dp),
        )

        Text(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 18.dp),
            text = stringResource(R.string.premium_order_history_premium_valid_until),
            style = AppTheme.typography.bodyLarge,
            fontSize = 18.sp,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )

        Row(
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .padding(start = 18.dp),
                text = if (state.isLegend) {
                    stringResource(R.string.premium_order_history_premium_valid_forever)
                } else if (state.expiresAt != null) {
                    Instant.ofEpochSecond(state.expiresAt).formatToDefaultDateFormat(FormatStyle.LONG)
                } else {
                    "Unknown"
                },
                style = AppTheme.typography.bodyLarge,
                fontSize = 24.sp,
                color = AppTheme.colorScheme.onSurface,
            )

            if (state.expiresAt != null && state.expiresAt < Clock.System.now().epochSeconds) {
                Text(
                    modifier = Modifier.padding(start = 6.dp, bottom = 6.dp),
                    text = "(${
                        stringResource(
                            R.string.premium_order_history_premium_expired,
                        ).lowercase()
                    })",
                    style = AppTheme.typography.bodySmall,
                    fontSize = 14.sp,
                    color = AppTheme.colorScheme.onSurface,
                )
            }
        }

        Text(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .padding(horizontal = 18.dp),
            text = if (state.isLegend) {
                stringResource(R.string.premium_order_history_premium_hint_legend)
            } else if (state.isRecurringSubscription) {
                when {
                    state.subscriptionOrigin.isOriginAndroid() -> {
                        stringResource(R.string.premium_order_history_premium_hint_renews_play_store)
                    }

                    state.subscriptionOrigin.isOriginIOS() -> {
                        stringResource(R.string.premium_order_history_premium_hint_renews_app_store)
                    }

                    else -> {
                        stringResource(R.string.premium_order_history_premium_hint_does_not_renews)
                    }
                }
            } else {
                stringResource(R.string.premium_order_history_premium_hint_does_not_renews)
            },
            style = AppTheme.typography.bodySmall,
            fontSize = 14.sp,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )

        if (state.isRecurringSubscription) {
            TextButton(
                onClick = { showCancelSubscriptionDialog = true },
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    text = stringResource(R.string.premium_order_history_cancel_subscription_button),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colorScheme.secondary,
                    fontSize = 16.sp,
                )
            }
        } else if (!state.isLegend) {
            val expiresAt = state.expiresAt ?: 0
            val isExpired = expiresAt < Clock.System.now().epochSeconds
            TextButton(
                onClick = {
                    onExtendSubscription(state.primalName)
                },
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    text = when {
                        isExpired -> stringResource(
                            R.string.premium_order_history_renews_subscription_button,
                        )

                        isGoogleBuild() -> stringResource(
                            R.string.premium_order_history_enable_renewal_button,
                        )

                        else -> stringResource(R.string.premium_order_history_extend_subscription_button)
                    },
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colorScheme.secondary,
                    fontSize = 16.sp,
                )
            }
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
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
