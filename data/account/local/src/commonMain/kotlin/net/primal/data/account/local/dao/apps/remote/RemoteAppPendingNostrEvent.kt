package net.primal.data.account.local.dao.apps.remote

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import net.primal.shared.data.local.encryption.Encryptable

@Entity
data class RemoteAppPendingNostrEvent(
    @PrimaryKey val eventId: String,
    val rawNostrEventJson: Encryptable<String>,
    val signerPubKey: String,
    val clientPubKey: String,
)
