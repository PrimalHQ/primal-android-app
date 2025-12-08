package net.primal.data.account.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.shared.data.local.encryption.Encryptable

@Entity
data class PendingNostrEvent(
    @PrimaryKey val eventId: String,
    val rawNostrEventJson: Encryptable<String>,
    val signerPubKey: Encryptable<String>,
    val clientPubKey: String,
)
