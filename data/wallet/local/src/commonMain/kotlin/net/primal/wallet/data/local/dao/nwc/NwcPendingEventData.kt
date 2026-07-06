package net.primal.wallet.data.local.dao.nwc

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import net.primal.shared.data.local.encryption.Encryptable

@Entity
data class NwcPendingEventData(
    @PrimaryKey val eventId: String,
    val userId: String,
    val connectionId: String,
    val rawNostrEventJson: Encryptable<String>,
    val createdAt: Long,
)
