package net.primal.wallet.data.local.dao

import androidx.room.Embedded
import androidx.room.Relation

data class NostrWalletConnection(
    @Embedded val data: NostrWalletConnectionData,

    @Relation(
        parentColumn = "walletId",
        entityColumn = "walletId",
    )
    val info: WalletInfo,
)
