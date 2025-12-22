package net.primal.data.account.local.dao.apps.remote

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.shared.data.local.encryption.Encryptable

@Entity
data class RemoteAppPendingNostrEvent(
    @PrimaryKey val eventId: String,
    val rawNostrEventJson: Encryptable<String>,
    val signerPubKey: String,
    val clientPubKey: String,
)
