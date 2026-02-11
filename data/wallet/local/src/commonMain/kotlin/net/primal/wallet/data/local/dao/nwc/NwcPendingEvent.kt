package net.primal.wallet.data.local.dao.nwc

import androidx.room.Embedded
import androidx.room.Relation

data class NwcPendingEvent(
    @Embedded val data: NwcPendingEventData,
    @Relation(
        parentColumn = "connectionId",
        entityColumn = "secretPubKey",
    )
    val connection: NwcConnectionData?,
)
