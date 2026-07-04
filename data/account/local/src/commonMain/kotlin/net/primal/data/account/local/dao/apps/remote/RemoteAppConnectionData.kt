package net.primal.data.account.local.dao.apps.remote

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import net.primal.data.account.local.dao.apps.TrustLevel
import net.primal.shared.data.local.encryption.Encryptable

@Entity(
    indices = [
        Index(value = ["signerPubKey"]),
        Index(value = ["userPubKey"]),
    ],
)
data class RemoteAppConnectionData(
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
