package net.primal.wallet.data.local.dao

import androidx.room.Embedded
import androidx.room.Relation

data class Wallet(
    @Embedded val info: WalletInfo,

    @Relation(
        parentColumn = "walletId",
        entityColumn = "walletId",
    )
    val primal: PrimalWalletData? = null,

    @Relation(
        parentColumn = "walletId",
        entityColumn = "walletId",
    )
    val nwc: NostrWalletData? = null,

    @Relation(
        parentColumn = "walletId",
        entityColumn = "walletId",
    )
    val spark: SparkWalletData? = null,

    @Relation(
        parentColumn = "walletId",
        entityColumn = "walletId",
    )
    val settings: WalletSettings? = null,
)
