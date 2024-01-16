package net.primal.android.wallet.transactions.list

import java.time.Instant
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.wallet.domain.TxType

data class TransactionDataUi(
    val txId: String,
    val txType: TxType,
    val txInstant: Instant,
    val txAmountInSats: ULong,
    val txNote: String?,
    val isZap: Boolean,
    val isStorePurchase: Boolean,
    val otherUserId: String? = null,
    val otherUserAvatarCdnImage: CdnImage? = null,
    val otherUserDisplayName: String? = null,
)
