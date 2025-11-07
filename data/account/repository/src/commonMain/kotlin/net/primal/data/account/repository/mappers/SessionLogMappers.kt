package net.primal.data.account.repository.mappers

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import net.primal.data.account.local.dao.SessionLogData
import net.primal.domain.account.model.SessionLog
import net.primal.shared.data.local.encryption.asEncryptable

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
fun SessionLog.asLocal(): SessionLogData {
    return SessionLogData(
        logId = Uuid.random().toString(),
        sessionId = this.sessionId,
        clientPubKey = this.clientPubKey.asEncryptable(),
        methodType = this.methodType,
        eventKind = this.eventKind?.asEncryptable(),
        isSuccess = this.isSuccess.asEncryptable(),
        requestPayload = this.methodPayload.asEncryptable(),
        responsePayload = this.responsePayload.asEncryptable(),
        createdAt = Clock.System.now().epochSeconds,
    )
}

fun SessionLogData.asDomain(): SessionLog {
    return SessionLog(
        sessionId = this.sessionId,
        clientPubKey = this.clientPubKey.decrypted,
        methodType = this.methodType,
        eventKind = this.eventKind?.decrypted,
        isSuccess = this.isSuccess.decrypted,
        methodPayload = this.requestPayload.decrypted,
        responsePayload = this.responsePayload.decrypted,
    )
}
