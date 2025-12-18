package net.primal.data.account.local.dao

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import net.primal.shared.data.local.encryption.Encryptable

@Entity(
    indices = [
        Index(value = ["signerPubKey"]),
        Index(value = ["userPubKey"]),
    ],
)
data class AppConnectionData(
    @PrimaryKey
    val clientPubKey: String,
    val signerPubKey: String,
    val userPubKey: String,
    val relays: Encryptable<List<String>>,
    val secret: Encryptable<String>,
    val name: Encryptable<String>?,
    val url: Encryptable<String>?,
    val image: Encryptable<String>?,
    val trustLevel: TrustLevel,
    val autoStart: Boolean,
)
