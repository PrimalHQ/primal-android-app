package net.primal.data.account.local.dao.apps.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.data.account.local.dao.apps.TrustLevel

@Entity
data class LocalAppData(
    @PrimaryKey val identifier: String,
    val packageName: String,
    val userPubKey: String,
    val trustLevel: TrustLevel,
)
