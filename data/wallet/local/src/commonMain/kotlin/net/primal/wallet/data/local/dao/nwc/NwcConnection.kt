package net.primal.wallet.data.local.dao.nwc

import androidx.room3.Embedded
import androidx.room3.Relation
import net.primal.wallet.data.local.dao.WalletInfo

data class NwcConnection(
    @Embedded val data: NwcConnectionData,

    @Relation(
        parentColumns = ["walletId"],
        entityColumns = ["walletId"],
    )
    val info: WalletInfo,
)
