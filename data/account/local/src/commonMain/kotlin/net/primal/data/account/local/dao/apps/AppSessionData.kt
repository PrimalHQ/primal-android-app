package net.primal.data.account.local.dao.apps

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Entity
data class AppSessionData(
    @PrimaryKey
    val sessionId: String = Uuid.random().toString(),
    val sessionType: AppSessionType,
    val appIdentifier: String,
    val startedAt: Long = Clock.System.now().epochSeconds,
    val endedAt: Long? = null,
)
