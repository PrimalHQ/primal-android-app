package net.primal.wallet.data.local.dao

import androidx.room.Embedded
import androidx.room.Relation

data class ActiveWallet(
    @Embedded val active: ActiveWalletData,

    @Relation(
        parentColumn = "walletId",
        entityColumn = "walletId",
    )
    val info: WalletInfo? = null,

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
