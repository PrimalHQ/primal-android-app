package net.primal.wallet.data.local.dao.nwc

import androidx.room3.Embedded
import androidx.room3.Relation

data class NwcPendingEvent(
    @Embedded val data: NwcPendingEventData,
    @Relation(
        parentColumns = ["connectionId"],
        entityColumns = ["secretPubKey"],
    )
    val connection: NwcConnectionData?,
)
