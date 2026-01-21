package net.primal.wallet.data.local.dao.nwc

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.wallet.data.local.dao.WalletInfo

data class NwcConnection(
    @Embedded val data: NwcConnectionData,

    @Relation(
        parentColumn = "walletId",
        entityColumn = "walletId",
    )
    val info: WalletInfo,
)
