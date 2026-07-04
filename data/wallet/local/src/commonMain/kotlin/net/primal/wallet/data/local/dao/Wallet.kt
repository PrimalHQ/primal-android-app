package net.primal.wallet.data.local.dao

import androidx.room3.Embedded
import androidx.room3.Relation

data class Wallet(
    @Embedded val info: WalletInfo,

    @Relation(
        parentColumns = ["walletId"],
        entityColumns = ["walletId"],
    )
    val links: List<WalletUserLink> = emptyList(),

    @Relation(
        parentColumns = ["walletId"],
        entityColumns = ["walletId"],
    )
    val primal: PrimalWalletData? = null,

    @Relation(
        parentColumns = ["walletId"],
        entityColumns = ["walletId"],
    )
    val nwc: NostrWalletData? = null,

    @Relation(
        parentColumns = ["walletId"],
        entityColumns = ["walletId"],
    )
    val spark: SparkWalletData? = null,

    @Relation(
        parentColumns = ["walletId"],
        entityColumns = ["walletId"],
    )
    val settings: WalletSettings? = null,
)
