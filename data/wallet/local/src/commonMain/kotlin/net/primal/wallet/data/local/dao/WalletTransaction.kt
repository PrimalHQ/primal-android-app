package net.primal.wallet.data.local.dao

import androidx.room.Embedded
import androidx.room.Relation

data class WalletTransaction(
    @Embedded val info: WalletTransactionData,

    @Relation(
        parentColumn = "transactionId",
        entityColumn = "transactionId",
    )
    val primal: PrimalTransactionData?,

    @Relation(
        parentColumn = "transactionId",
        entityColumn = "transactionId",
    )
    val nwc: NostrTransactionData?,
)
