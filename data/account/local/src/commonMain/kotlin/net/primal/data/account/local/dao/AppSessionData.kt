package net.primal.data.account.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Entity
data class AppSessionData(
    @PrimaryKey
    val sessionId: String = Uuid.random().toString(),
    val clientPubKey: String,
    val startedAt: Long = Clock.System.now().epochSeconds,
    val endedAt: Long? = null,
    val activeRelayCount: Int = 0,
)
