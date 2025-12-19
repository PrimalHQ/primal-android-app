package net.primal.data.account.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LocalAppData(
    @PrimaryKey val identifier: String,
    val packageName: String,
    val userPubKey: String,
    val trustLevel: TrustLevel,
)
