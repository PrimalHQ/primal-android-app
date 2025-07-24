package net.primal.android.wallet.transactions.list

import java.time.Instant
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.domain.links.CdnImage
import net.primal.domain.wallet.TxState
import net.primal.domain.wallet.TxType

sealed class TransactionListItemUi {
    data class Header(val day: String) : TransactionListItemUi()

    data class TxData(
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
    ) : TransactionListItemUi()
}
// data class TransactionListItemDataUi(
//    val txId: String,
//    val txType: TxType,
//    val txState: TxState,
//    val txCreatedAt: Instant,
//    val txUpdatedAt: Instant,
//    val txCompletedAt: Instant? = null,
//    val txAmountInSats: ULong,
//    val txNote: String?,
//    val isZap: Boolean,
//    val isStorePurchase: Boolean,
//    val isOnChainPayment: Boolean,
//    val otherUserId: String? = null,
//    val otherUserAvatarCdnImage: CdnImage? = null,
//    val otherUserDisplayName: String? = null,
//    val otherUserLegendaryCustomization: LegendaryCustomization? = null,
// )
