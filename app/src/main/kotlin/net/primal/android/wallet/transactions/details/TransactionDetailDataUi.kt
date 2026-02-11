package net.primal.android.wallet.transactions.details

import java.time.Instant
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.domain.links.CdnImage
import net.primal.domain.wallet.TxState
import net.primal.domain.wallet.TxType

data class TransactionDetailDataUi(
    val txId: String,
    val txType: TxType,
    val txState: TxState,
    val txInstant: Instant,
    val txAmountInSats: ULong,
    val txAmountInUsd: Double?,
    val txNote: String?,
    val isZap: Boolean,
    val isOnChain: Boolean,
    val isStorePurchase: Boolean,
    val invoice: String?,
    val totalFeeInSats: ULong?,
    val exchangeRate: String?,
    val onChainAddress: String?,
    val onChainTxId: String?,
    val otherUserId: String? = null,
    val otherUserAvatarCdnImage: CdnImage? = null,
    val otherUserInternetIdentifier: String? = null,
    val otherUserDisplayName: String? = null,
    val otherUserLightningAddress: String? = null,
    val otherUserLegendaryCustomization: LegendaryCustomization? = null,
)
