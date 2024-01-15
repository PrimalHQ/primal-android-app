package net.primal.android.wallet.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.android.wallet.domain.SubWallet
import net.primal.android.wallet.domain.TxState
import net.primal.android.wallet.domain.TxType

@Entity
data class WalletTransactionData(
    @PrimaryKey
    val id: String,
    val walletLightningAddress: String,
    val type: TxType,
    val state: TxState,
    val createdAt: Long,
    val completedAt: Long?,
    val amountInBtc: String,
    val isZap: Boolean,
    val isStorePurchase: Boolean,
    val userId: String,
    val userSubWallet: SubWallet,
    val userLightningAddress: String?,
    val otherUserId: String?,
    val otherLightningAddress: String?,
    val note: String?,
    val zapNoteId: String?,
    val zapNoteAuthorId: String?,
    val zappedByUserId: String?,
)
