package net.primal.data.account.repository.mappers

import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import net.primal.data.account.local.dao.SignerLogData
import net.primal.domain.account.model.SignerLog
import net.primal.shared.data.local.encryption.asEncryptable

@OptIn(ExperimentalUuidApi::class)
fun SignerLog.asLocal(): SignerLogData {
    return SignerLogData(
        logId = Uuid.random().toString(),
        sessionId = this.sessionId,
        clientPubKey = this.clientPubKey.asEncryptable(),
        methodType = this.methodType,
        eventKind = this.eventKind,
        isSuccess = this.isSuccess,
        requestPayload = this.methodPayload.asEncryptable(),
        responsePayload = this.responsePayload.asEncryptable(),
        createdAt = Clock.System.now().epochSeconds,
    )
}
