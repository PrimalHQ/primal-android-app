package net.primal.data.account.local.dao

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import net.primal.shared.data.local.encryption.Encryptable

@OptIn(ExperimentalUuidApi::class)
@Entity(
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["clientPubKey"]),
    ],
)
data class SignerLogData(
    @PrimaryKey
    val logId: String = Uuid.random().toString(),
    val sessionId: String,
    val clientPubKey: Encryptable<String>,
    val methodType: String,
    val eventKind: Int?,
    val isSuccess: Boolean,
    val requestPayload: Encryptable<String>,
    val responsePayload: Encryptable<String>,
    val createdAt: Long = Clock.System.now().epochSeconds,
)
