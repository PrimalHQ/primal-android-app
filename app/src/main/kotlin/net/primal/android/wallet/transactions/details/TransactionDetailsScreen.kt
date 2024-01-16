package net.primal.android.wallet.transactions.details

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.feed.note.FeedNoteCard
import net.primal.android.core.compose.feed.note.FeedNoteHeader
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.Copy
import net.primal.android.core.compose.icons.primaliconpack.WalletLnPayment
import net.primal.android.core.utils.ellipsizeMiddle
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.wallet.dashboard.ui.BtcAmountText
import net.primal.android.wallet.domain.TxState
import net.primal.android.wallet.domain.TxType
import net.primal.android.wallet.transactions.details.TransactionDetailsContract.UiState
import net.primal.android.wallet.transactions.list.TransactionIcon
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc
import net.primal.android.wallet.walletDepositColor
import net.primal.android.wallet.walletWithdrawColor
import timber.log.Timber

@Composable
fun TransactionDetailsScreen(
    viewModel: TransactionDetailsViewModel,
    onClose: () -> Unit,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    TransactionDetailsScreen(
        state = uiState.value,
        onClose = onClose,
        onPostClick = onPostClick,
        onProfileClick = onProfileClick,
        onHashtagClick = onHashtagClick,
        onMediaClick = onMediaClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsScreen(
    state: UiState,
    onClose: () -> Unit,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
) {
    val scrollState = rememberScrollState()
    val showTopBarDivider by remember { derivedStateOf { scrollState.value > 0 } }
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = state.txData.resolveTitle(),
                navigationIcon = PrimalIcons.ArrowBack,
                showDivider = showTopBarDivider,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(state = scrollState)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                state.txData?.let { txData ->
                    val color = when (state.txData.txType) {
                        TxType.DEPOSIT -> walletDepositColor
                        TxType.WITHDRAW -> walletWithdrawColor
                    }

                    BtcAmountText(
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(vertical = 32.dp)
                            .padding(start = 32.dp),
                        amountInBtc = txData.txAmountInSats.toBtc().toBigDecimal(),
                        textSize = 48.sp,
                        amountColor = color,
                        currencyColor = color,
                    )

                    val text = when (txData.txType) {
                        TxType.DEPOSIT -> stringResource(id = R.string.wallet_transaction_details_received_from)
                        TxType.WITHDRAW -> stringResource(id = R.string.wallet_transaction_details_sent_to)
                    }

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        text = text.uppercase(),
                        textAlign = TextAlign.Start,
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    )

                    TransactionCard(
                        txData = txData,
                        onProfileClick = onProfileClick,
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                state.feedPost?.let {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        text = stringResource(id = R.string.wallet_transaction_details_zapped_note).uppercase(),
                        textAlign = TextAlign.Start,
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    )

                    FeedNoteCard(
                        data = it,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        colors = transactionCardColors(),
                        onPostClick = onPostClick,
                        onProfileClick = onProfileClick,
                        onHashtagClick = onHashtagClick,
                        onMediaClick = onMediaClick,
                        onMuteUserClick = {},
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        },
    )
}

@Composable
private fun TransactionDetailDataUi?.resolveTitle(): String {
    return when (this?.txType) {
        TxType.DEPOSIT -> if (isZap) {
            stringResource(id = R.string.wallet_transaction_details_title_zap_received)
        } else {
            stringResource(id = R.string.wallet_transaction_details_title_payment_received)
        }

        TxType.WITHDRAW -> if (isZap) {
            stringResource(id = R.string.wallet_transaction_details_title_zap_sent)
        } else {
            stringResource(id = R.string.wallet_transaction_details_title_payment_sent)
        }

        else -> ""
    }
}

@Composable
private fun transactionCardColors(): CardColors {
    return if (LocalPrimalTheme.current.isDarkTheme) {
        CardDefaults.cardColors(
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
            contentColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )
    } else {
        CardDefaults.cardColors()
    }
}

@Composable
private fun TransactionDetailDataUi.typeToReadableString(): String {
    return when {
        isZap -> stringResource(id = R.string.wallet_transaction_details_type_nostr_zap)
        isStorePurchase -> stringResource(id = R.string.wallet_transaction_details_type_in_app_purchase)
        onChainAddress != null -> stringResource(
            id = R.string.wallet_transaction_details_type_on_chain_payment,
        )

        else -> stringResource(id = R.string.wallet_transaction_details_type_lightning_payment)
    }
}

@Composable
private fun TransactionCard(txData: TransactionDetailDataUi, onProfileClick: (String) -> Unit) {
    val numberFormat = remember { NumberFormat.getNumberInstance().apply { maximumFractionDigits = 2 } }

    val isExpandable = txData.isZap && (
        txData.txAmountInUsd != null || txData.exchangeRate != null ||
            txData.totalFeeInSats != null || txData.invoice != null
        )

    var expanded by remember { mutableStateOf(!txData.isZap) }

    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .animateContentSize(
                animationSpec = spring(stiffness = StiffnessMediumLow),
            ),
        colors = transactionCardColors(),
    ) {
        if (!txData.otherUserDisplayName.isNullOrEmpty()) {
            FeedNoteHeader(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = txData.otherUserId != null,
                        onClick = { txData.otherUserId?.let(onProfileClick) },
                    ),
                authorAvatarSize = 42.dp,
                authorDisplayName = txData.otherUserDisplayName,
                authorAvatarCdnImage = txData.otherUserAvatarCdnImage,
                authorInternetIdentifier = txData.otherUserInternetIdentifier,
                onAuthorAvatarClick = { txData.otherUserId?.let(onProfileClick) },
                label = txData.otherUserLightningAddress,
                labelStyle = AppTheme.typography.bodyMedium,
            )
        } else {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TransactionIcon(
                    background = Color(0xFF222222),
                ) {
                    Image(
                        imageVector = PrimalIcons.WalletLnPayment,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(color = AppTheme.extraColorScheme.zapped),
                    )
                }

                Column(
                    modifier = Modifier.padding(horizontal = 10.dp),
                ) {
                    Text(
                        text = txData.typeToReadableString(),
                        style = AppTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )

                    txData.otherUserLightningAddress?.let { lud16Receiver ->
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = lud16Receiver,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                        )
                    }
                }
            }
        }

        txData.txNote?.ifEmpty { null }?.let { note ->
            Text(
                text = note,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
                color = AppTheme.colorScheme.onPrimary,
                style = AppTheme.typography.bodyMedium.copy(fontSize = 16.sp),
            )
        }

        PrimalDivider()
        TransactionDetailListItem(
            section = stringResource(id = R.string.wallet_transaction_details_date_item),
            value = txData.txInstant.formatToDefaultFormat(FormatStyle.MEDIUM),
        )

        PrimalDivider()
        TransactionDetailListItem(
            section = stringResource(id = R.string.wallet_transaction_details_status_item),
            value = txData.txState.toReadableString(),
        )

        PrimalDivider()
        TransactionDetailListItem(
            section = stringResource(id = R.string.wallet_transaction_details_type_item),
            value = txData.typeToReadableString(),
        )

        if (expanded) {
            if (txData.txAmountInUsd != null || txData.exchangeRate != null) {
                val usdAmount = txData.txAmountInUsd?.toDouble() ?: txData.exchangeRate?.let { rate ->
                    txData.txAmountInSats.toBtc() / rate.toDouble()
                }

                val formattedUsdAmount = try {
                    numberFormat.format(usdAmount)
                } catch (error: IllegalArgumentException) {
                    Timber.e(error)
                    null
                }

                if (formattedUsdAmount != null) {
                    PrimalDivider()
                    TransactionDetailListItem(
                        section = stringResource(id = R.string.wallet_transaction_details_original_usd_item),
                        value = "$$formattedUsdAmount",
                    )
                }
            }

            txData.totalFeeInSats?.let { feeAmount ->
                PrimalDivider()
                TransactionDetailListItem(
                    section = stringResource(id = R.string.wallet_transaction_details_fee_item),
                    value = "${
                        numberFormat.format(
                            feeAmount.toLong(),
                        )
                    } ${stringResource(id = R.string.wallet_sats_suffix)}",
                )
            }

            txData.invoice?.let { invoice ->
                val clipboardManager = LocalClipboardManager.current

                PrimalDivider()
                TransactionDetailListItem(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            clipboardManager.setText(AnnotatedString(text = invoice))
                        },
                    ),
                    section = stringResource(id = R.string.wallet_transaction_details_invoice_item),
                    value = invoice.ellipsizeMiddle(size = 10),
                    trailingIcon = PrimalIcons.Copy,
                )
            }
        }

        if (isExpandable) {
            PrimalDivider()
            IconText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { expanded = !expanded },
                    ),
                text = stringResource(id = R.string.wallet_transaction_details_expand_collapse_hint),
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                trailingIcon = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                trailingIconTintColor = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
        }
    }
}

@Composable
private fun TxState.toReadableString(): String {
    return when (this) {
        TxState.CREATED -> stringResource(id = R.string.wallet_transaction_details_status_created)
        TxState.PROCESSING -> stringResource(id = R.string.wallet_transaction_details_status_processing)
        TxState.SUCCEEDED -> stringResource(id = R.string.wallet_transaction_details_status_succeeded)
        TxState.FAILED -> stringResource(id = R.string.wallet_transaction_details_status_failed)
        TxState.CANCELED -> stringResource(id = R.string.wallet_transaction_details_status_canceled)
    }
}

@Composable
fun Instant.formatToDefaultFormat(dateTimeStyle: FormatStyle): String {
    val zoneId: ZoneId = ZoneId.systemDefault()
    val locale: Locale = Locale.getDefault()

    val formatter: DateTimeFormatter = DateTimeFormatter
        .ofLocalizedDateTime(dateTimeStyle)
        .withLocale(locale)

    return formatter.format(this.atZone(zoneId))
}

@Composable
private fun TransactionDetailListItem(
    section: String,
    value: String,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = section,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )

        IconText(
            text = value,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            trailingIcon = trailingIcon,
            trailingIconTintColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )
    }
}

class TransactionParameterProvider : PreviewParameterProvider<TransactionDetailDataUi> {
    override val values: Sequence<TransactionDetailDataUi>
        get() = sequenceOf(
            TransactionDetailDataUi(
                txId = "123",
                txType = TxType.DEPOSIT,
                txAmountInSats = 9999.toULong(),
                txNote = "Bought sats from Primal",
                txInstant = Instant.now(),
                otherUserId = "storeId",
                otherUserAvatarCdnImage = null,
                isZap = false,
                isStorePurchase = true,
                exchangeRate = null,
                txAmountInUsd = null,
                txState = TxState.SUCCEEDED,
                invoice = "",
                onChainAddress = null,
                totalFeeInSats = null,
            ),
        )
}

@Preview
@Composable
fun PreviewTransactionDetail(
    @PreviewParameter(provider = TransactionParameterProvider::class)
    txDataParam: TransactionDetailDataUi,
) {
    PrimalTheme(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface {
            TransactionDetailsScreen(
                state = UiState(
                    txData = txDataParam,
                ),
                onClose = {},
                onProfileClick = {},
                onPostClick = {},
                onHashtagClick = {},
                onMediaClick = { _, _ -> },
            )
        }
    }
}
