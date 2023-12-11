package net.primal.android.wallet.transactions

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import net.primal.android.R
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.WrappedContentWithSuffix
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.WalletLnPayment
import net.primal.android.core.compose.icons.primaliconpack.WalletPay
import net.primal.android.core.compose.icons.primaliconpack.WalletReceive
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.wallet.domain.TxType
import net.primal.android.wallet.walletDepositColor
import net.primal.android.wallet.walletWithdrawColor

@Composable
fun TransactionListItem(
    data: TransactionDataUi,
    numberFormat: NumberFormat,
    onAvatarClick: (String) -> Unit,
) {
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.colorScheme.surfaceVariant,
        ),
        leadingContent = {
            TransactionLeadingContent(
                otherUserId = data.otherUserId,
                otherUserAvatarCdnImage = data.otherUserAvatarCdnImage,
                onAvatarClick = onAvatarClick,
            )
        },
        headlineContent = {
            TransactionHeadlineContent(
                wrappedText = data.otherUserDisplayName ?: when (data.txType) {
                    TxType.DEPOSIT -> stringResource(id = R.string.wallet_transaction_list_item_received)
                    TxType.WITHDRAW -> stringResource(id = R.string.wallet_transaction_list_item_sent)
                },
                suffixText = " | ${data.txInstant.formatAsTime()}",
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
                txAmountInSats = numberFormat.format(data.txAmountInSats.toLong()),
            )
        },
    )
}

@Composable
private fun TransactionLeadingContent(
    otherUserId: String?,
    otherUserAvatarCdnImage: CdnImage?,
    onAvatarClick: (String) -> Unit,
) {
    when {
        otherUserId != null -> {
            AvatarThumbnail(
                avatarCdnImage = otherUserAvatarCdnImage,
                onClick = { onAvatarClick(otherUserId) },
            )
        }

        else -> {
            TransactionIcon {
                Image(
                    imageVector = PrimalIcons.WalletLnPayment,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color = AppTheme.extraColorScheme.zapped),
                )
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
private fun TransactionTrailingContent(txAmountInSats: String, txType: TxType) {
    Column(
        horizontalAlignment = Alignment.End,
    ) {
        IconText(
            text = txAmountInSats,
            iconSize = 16.sp,
            trailingIcon = when (txType) {
                TxType.DEPOSIT -> PrimalIcons.WalletReceive
                TxType.WITHDRAW -> PrimalIcons.WalletPay
            },
            trailingIconTintColor = when (txType) {
                TxType.DEPOSIT -> walletDepositColor
                TxType.WITHDRAW -> walletWithdrawColor
            },
            style = AppTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colorScheme.onSurface,
        )
        IconText(
            text = "${stringResource(id = R.string.wallet_sats_suffix)} ",
            iconSize = 16.sp,
            trailingIcon = PrimalIcons.WalletReceive,
            trailingIconTintColor = Color.Transparent,
            style = AppTheme.typography.bodySmall,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
        )
    }
}

@Composable
private fun TransactionIcon(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                shape = CircleShape,
            )
            .size(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

private fun Instant.formatAsTime(): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return formatter.format(this.atZone(ZoneId.systemDefault()))
}

class TxDataProvider : PreviewParameterProvider<TransactionDataUi> {
    override val values: Sequence<TransactionDataUi>
        get() = sequenceOf(
            TransactionDataUi(
                txId = "123",
                txType = TxType.DEPOSIT,
                txAmountInSats = 9999.toULong(),
                txNote = "Bought sats from Primal",
                txInstant = Instant.now(),
                otherUserId = "storeId",
                otherUserAvatarCdnImage = null,
                isZap = false,
                isStorePurchase = true,
            ),
            TransactionDataUi(
                txId = "123",
                txType = TxType.DEPOSIT,
                txAmountInSats = 333.toULong(),
                txNote = null,
                txInstant = Instant.now(),
                otherUserId = null,
                otherUserAvatarCdnImage = null,
                isZap = false,
                isStorePurchase = false,
            ),
            TransactionDataUi(
                txId = "123",
                txType = TxType.WITHDRAW,
                txAmountInSats = 111.toULong(),
                txNote = null,
                txInstant = Instant.now(),
                otherUserId = null,
                otherUserAvatarCdnImage = null,
                isZap = false,
                isStorePurchase = false,
            ),
            TransactionDataUi(
                txId = "123",
                txType = TxType.DEPOSIT,
                txAmountInSats = 256.toULong(),
                txNote = null,
                txInstant = Instant.now(),
                otherUserId = "abcId",
                otherUserAvatarCdnImage = null,
                isZap = true,
                isStorePurchase = false,
            ),
            TransactionDataUi(
                txId = "123",
                txType = TxType.WITHDRAW,
                txAmountInSats = 888.toULong(),
                txNote = null,
                txInstant = Instant.now(),
                otherUserId = "abcId",
                otherUserAvatarCdnImage = null,
                isZap = true,
                isStorePurchase = false,
            ),
            TransactionDataUi(
                txId = "123",
                txType = TxType.DEPOSIT,
                txAmountInSats = 1024.toULong(),
                txNote = "LFG!",
                txInstant = Instant.now(),
                otherUserId = "abdId",
                otherUserAvatarCdnImage = null,
                isZap = true,
                isStorePurchase = false,
            ),
            TransactionDataUi(
                txId = "123",
                txType = TxType.WITHDRAW,
                txAmountInSats = 128.toULong(),
                txNote = "Zap Comment",
                txInstant = Instant.now(),
                otherUserId = "abcId",
                otherUserAvatarCdnImage = null,
                isZap = true,
                isStorePurchase = false,
            ),
        )
}

@Preview
@Composable
fun PreviewTransactionListItem(
    @PreviewParameter(provider = TxDataProvider::class)
    tx: TransactionDataUi,
) {
    PrimalTheme(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface {
            TransactionListItem(
                data = tx,
                numberFormat = NumberFormat.getNumberInstance(),
                onAvatarClick = {},
            )
        }
    }
}
