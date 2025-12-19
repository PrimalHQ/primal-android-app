package net.primal.data.account.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.shared.data.local.encryption.Encryptable

@Entity
data class LocalAppData(
    @PrimaryKey val identifier: String,
    val packageName: String,
    val userPubKey: String,
    val image: Encryptable<String>?,
    val name: Encryptable<String>?,
    val trustLevel: TrustLevel,
)
