package net.primal.android.wallet.transactions.list

import java.time.Instant
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.wallet.domain.TxState
import net.primal.android.wallet.domain.TxType

data class TransactionListItemDataUi(
    val txId: String,
    val txType: TxType,
    val txState: TxState,
    val txCreatedAt: Instant,
    val txUpdatedAt: Instant,
    val txCompletedAt: Instant? = null,
    val txAmountInSats: ULong,
    val txNote: String?,
    val isZap: Boolean,
    val isStorePurchase: Boolean,
    val isOnChainPayment: Boolean,
    val otherUserId: String? = null,
    val otherUserAvatarCdnImage: CdnImage? = null,
    val otherUserDisplayName: String? = null,
    val otherUserLegendaryCustomization: LegendaryCustomization? = null,
)
