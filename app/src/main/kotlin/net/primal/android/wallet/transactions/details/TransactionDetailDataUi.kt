package net.primal.android.wallet.transactions.details

import java.time.Instant
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.wallet.domain.TxState
import net.primal.android.wallet.domain.TxType

data class TransactionDetailDataUi(
    val txId: String,
    val txType: TxType,
    val txState: TxState,
    val txInstant: Instant,
    val txAmountInSats: ULong,
    val txAmountInUsd: String?,
    val txNote: String?,
    val isZap: Boolean,
    val isStorePurchase: Boolean,
    val invoice: String?,
    val totalFeeInSats: ULong?,
    val exchangeRate: String?,
    val onChainAddress: String?,
    val otherUserId: String? = null,
    val otherUserAvatarCdnImage: CdnImage? = null,
    val otherUserInternetIdentifier: String? = null,
    val otherUserDisplayName: String? = null,
    val otherUserLightningAddress: String? = null,
)
