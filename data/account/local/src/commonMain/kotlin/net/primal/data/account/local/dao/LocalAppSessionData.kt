package net.primal.data.account.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock

@Entity
data class LocalAppSessionData(
    @PrimaryKey val sessionId: String,
    val appPackageName: String,
    val startedAt: Long = Clock.System.now().epochSeconds,
    val endedAt: Long? = null,
)
