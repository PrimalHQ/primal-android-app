package net.primal.android.wallet.transactions.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds
import net.primal.android.R
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.WrappedContentWithSuffix
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.WalletBitcoinPayment
import net.primal.android.core.compose.icons.primaliconpack.WalletLightningPaymentAlt
import net.primal.android.core.compose.icons.primaliconpack.WalletPay
import net.primal.android.core.compose.icons.primaliconpack.WalletReceive
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.premium.legend.LegendaryCustomization
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.dashboard.CurrencyMode
import net.primal.android.wallet.domain.TxState
import net.primal.android.wallet.domain.TxType
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc
import net.primal.android.wallet.utils.CurrencyConversionUtils.toUsd
import net.primal.android.wallet.walletDepositColor
import net.primal.android.wallet.walletTransactionIconBackgroundColor
import net.primal.android.wallet.walletWithdrawColor

@Composable
fun TransactionListItem(
    data: TransactionListItemDataUi,
    numberFormat: NumberFormat,
    onAvatarClick: (String) -> Unit,
    onClick: (String) -> Unit,
    currencyMode: CurrencyMode,
    exchangeBtcUsdRate: Double?,
) {
    val alphaScale by if (data.txState == TxState.CREATED || data.txState == TxState.PROCESSING) {
        val infiniteTransition = rememberInfiniteTransition(label = "PendingPulsing${data.txId}")
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 0.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(1.seconds.inWholeMilliseconds.toInt()),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "AlphaScale${data.txId}",
        )
    } else {
        remember { mutableFloatStateOf(1.0f) }
    }

    ListItem(
        modifier = Modifier
            .animateContentSize()
            .alpha(alphaScale)
            .clickable { onClick(data.txId) },
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.colorScheme.surfaceVariant,
        ),
        leadingContent = {
            TransactionLeadingContent(
                onChainPayment = data.isOnChainPayment,
                isPending = data.txState.isPending(),
                otherUserId = data.otherUserId,
                otherUserAvatarCdnImage = data.otherUserAvatarCdnImage,
                otherUserLegendaryCustomization = data.otherUserLegendaryCustomization,
                onAvatarClick = onAvatarClick,
            )
        },
        headlineContent = {
            val suffix = data.txCompletedAt?.formatAsTime() ?: stringResource(id = R.string.wallet_transactions_pending)

            TransactionHeadlineContent(
                wrappedText = data.otherUserDisplayName ?: when (data.isOnChainPayment) {
                    true -> stringResource(id = R.string.wallet_transaction_list_item_bitcoin)
                    false -> stringResource(id = R.string.wallet_transaction_list_item_lightning)
                },
                suffixText = " | $suffix",
            )
        },
        supportingContent = {
            TransactionSupportContent(
                txType = data.txType,
                txNote = data.txNote,
                isZap = data.isZap,
            )
        },
        trailingContent = {
            TransactionTrailingContent(
                txType = data.txType,
                txAmountInSats = BigDecimal.valueOf(data.txAmountInSats.toLong()),
                currencyMode = currencyMode,
                numberFormat = numberFormat,
                exchangeBtcUsdRate = exchangeBtcUsdRate,
            )
        },
    )
}

@Composable
private fun TransactionLeadingContent(
    onChainPayment: Boolean,
    isPending: Boolean,
    otherUserId: String?,
    otherUserAvatarCdnImage: CdnImage?,
    otherUserLegendaryCustomization: LegendaryCustomization?,
    onAvatarClick: (String) -> Unit,
) {
    when {
        otherUserId != null -> {
            UniversalAvatarThumbnail(
                avatarCdnImage = otherUserAvatarCdnImage,
                onClick = { onAvatarClick(otherUserId) },
                legendaryCustomization = otherUserLegendaryCustomization,
            )
        }

        else -> {
            TransactionIcon(background = walletTransactionIconBackgroundColor) {
                Image(
                    imageVector = when (onChainPayment) {
                        true -> PrimalIcons.WalletBitcoinPayment
                        false -> PrimalIcons.WalletLightningPaymentAlt
                    },
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color = AppTheme.extraColorScheme.zapped),
                )

                if (isPending) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomEnd,
                    ) {
                        Image(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(color = AppTheme.colorScheme.surfaceVariant),
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(color = AppTheme.extraColorScheme.onSurfaceVariantAlt1),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionHeadlineContent(wrappedText: String, suffixText: String) {
    WrappedContentWithSuffix(
        wrappedContent = {
            Text(
                text = wrappedText,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        suffixFixedContent = {
            Text(
                text = suffixText,
                maxLines = 1,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            )
        },
    )
}

@Composable
private fun TransactionSupportContent(
    txNote: String?,
    txType: TxType,
    isZap: Boolean,
) {
    val supportText = if (!txNote.isNullOrBlank()) {
        txNote
    } else {
        when (txType) {
            TxType.DEPOSIT -> if (isZap) {
                stringResource(id = R.string.wallet_transaction_list_item_zap_received)
            } else {
                stringResource(id = R.string.wallet_transaction_list_item_payment_received)
            }

            TxType.WITHDRAW -> if (isZap) {
                stringResource(id = R.string.wallet_transaction_list_item_zap_sent)
            } else {
                stringResource(id = R.string.wallet_transaction_list_item_payment_sent)
            }
        }
    }

    Text(
        text = supportText,
        style = AppTheme.typography.bodyMedium,
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
        maxLines = 1,
    )
}

@Composable
private fun TransactionTrailingContent(
    txAmountInSats: BigDecimal,
    txType: TxType,
    currencyMode: CurrencyMode,
    numberFormat: NumberFormat,
    exchangeBtcUsdRate: Double?,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AnimatedContent(
            modifier = Modifier.width(50.dp),
            label = "CurrencyContent",
            targetState = currencyMode,
            transitionSpec = { (slideInVertically() + fadeIn()) togetherWith fadeOut() },
        ) { targetCurrencyMode ->
            Column(
                horizontalAlignment = Alignment.End,
            ) {
                val text = if (targetCurrencyMode == CurrencyMode.FIAT) {
                    BigDecimal.valueOf(txAmountInSats.toBtc()).toUsd(exchangeBtcUsdRate)
                        .let { numberFormat.format(it.toFloat()) }
                } else {
                    numberFormat.format(txAmountInSats.toLong())
                }
                val suffix = if (targetCurrencyMode == CurrencyMode.FIAT) {
                    R.string.wallet_usd_suffix
                } else {
                    R.string.wallet_sats_suffix
                }

                Text(
                    text = text,
                    style = AppTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colorScheme.onSurface,
                )

                Text(
                    text = stringResource(id = suffix),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                )
            }
        }

        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = when (txType) {
                TxType.DEPOSIT -> PrimalIcons.WalletReceive
                TxType.WITHDRAW -> PrimalIcons.WalletPay
            },
            contentDescription = null,
            tint = when (txType) {
                TxType.DEPOSIT -> walletDepositColor
                TxType.WITHDRAW -> walletWithdrawColor
            },
        )
    }
}

private fun Instant.formatAsTime(): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return formatter.format(this.atZone(ZoneId.systemDefault()))
}

class TxDataProvider : PreviewParameterProvider<TransactionListItemDataUi> {
    override val values: Sequence<TransactionListItemDataUi>
        get() = sequenceOf(
            TransactionListItemDataUi(
                txId = "123",
                txType = TxType.DEPOSIT,
                txState = TxState.SUCCEEDED,
                txAmountInSats = 9999.toULong(),
                txNote = "Bought sats from Primal",
                txCreatedAt = Instant.now(),
                txUpdatedAt = Instant.now(),
                txCompletedAt = Instant.now(),
                otherUserId = "storeId",
                otherUserAvatarCdnImage = null,
                isZap = false,
                isStorePurchase = true,
                isOnChainPayment = false,
            ),
            TransactionListItemDataUi(
                txId = "123",
                txType = TxType.DEPOSIT,
                txState = TxState.SUCCEEDED,
                txAmountInSats = 333.toULong(),
                txNote = null,
                txCreatedAt = Instant.now(),
                txUpdatedAt = Instant.now(),
                txCompletedAt = Instant.now(),
                otherUserId = null,
                otherUserAvatarCdnImage = null,
                isZap = false,
                isStorePurchase = false,
                isOnChainPayment = false,
            ),
            TransactionListItemDataUi(
                txId = "123",
                txType = TxType.WITHDRAW,
                txState = TxState.SUCCEEDED,
                txAmountInSats = 111.toULong(),
                txNote = null,
                txCreatedAt = Instant.now(),
                txUpdatedAt = Instant.now(),
                txCompletedAt = Instant.now(),
                otherUserId = null,
                otherUserAvatarCdnImage = null,
                isZap = false,
                isStorePurchase = false,
                isOnChainPayment = false,
            ),
            TransactionListItemDataUi(
                txId = "123",
                txType = TxType.DEPOSIT,
                txState = TxState.SUCCEEDED,
                txAmountInSats = 128256.toULong(),
                txNote = null,
                txCreatedAt = Instant.now(),
                txUpdatedAt = Instant.now(),
                txCompletedAt = Instant.now(),
                otherUserId = "abcId",
                otherUserAvatarCdnImage = null,
                isZap = false,
                isStorePurchase = false,
                isOnChainPayment = true,
            ),
            TransactionListItemDataUi(
                txId = "123",
                txType = TxType.DEPOSIT,
                txState = TxState.SUCCEEDED,
                txAmountInSats = 100.toULong(),
                txNote = null,
                txCreatedAt = Instant.now(),
                txUpdatedAt = Instant.now(),
                txCompletedAt = Instant.now(),
                otherUserId = "abcId",
                otherUserAvatarCdnImage = null,
                isZap = true,
                isStorePurchase = false,
                isOnChainPayment = false,
            ),
            TransactionListItemDataUi(
                txId = "123",
                txType = TxType.WITHDRAW,
                txState = TxState.SUCCEEDED,
                txAmountInSats = 888.toULong(),
                txNote = null,
                txCreatedAt = Instant.now(),
                txUpdatedAt = Instant.now(),
                txCompletedAt = Instant.now(),
                otherUserId = "abcId",
                otherUserAvatarCdnImage = null,
                isZap = true,
                isStorePurchase = false,
                isOnChainPayment = false,
            ),
            TransactionListItemDataUi(
                txId = "123",
                txType = TxType.DEPOSIT,
                txState = TxState.SUCCEEDED,
                txAmountInSats = 1024.toULong(),
                txNote = "LFG!",
                txCreatedAt = Instant.now(),
                txUpdatedAt = Instant.now(),
                txCompletedAt = Instant.now(),
                otherUserId = "abdId",
                otherUserAvatarCdnImage = null,
                isZap = true,
                isStorePurchase = false,
                isOnChainPayment = false,
            ),
            TransactionListItemDataUi(
                txId = "123",
                txType = TxType.WITHDRAW,
                txState = TxState.SUCCEEDED,
                txAmountInSats = 128.toULong(),
                txNote = "Zap Comment",
                txCompletedAt = Instant.now(),
                txUpdatedAt = Instant.now(),
                txCreatedAt = Instant.now(),
                otherUserId = "abcId",
                otherUserAvatarCdnImage = null,
                isZap = true,
                isStorePurchase = false,
                isOnChainPayment = false,
            ),
        )
}

@Preview
@Composable
fun PreviewTransactionListItem(@PreviewParameter(provider = TxDataProvider::class) tx: TransactionListItemDataUi) {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface {
            TransactionListItem(
                data = tx,
                numberFormat = NumberFormat.getNumberInstance(),
                onAvatarClick = {},
                onClick = {},
                currencyMode = CurrencyMode.SATS,
                exchangeBtcUsdRate = 100000.0,
            )
        }
    }
}
