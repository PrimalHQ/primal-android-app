package net.primal.data.account.repository.processor

import net.primal.core.utils.serialization.CommonJsonEncodeDefaults
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse
import net.primal.data.account.repository.mappers.asLocal
import net.primal.domain.account.model.SessionLog
import net.primal.domain.account.model.SignerMethodType

class SessionLogger(
    private val accountDatabase: AccountDatabase,
) {
    suspend fun processAndLog(
        sessionId: String,
        method: RemoteSignerMethod,
        response: RemoteSignerMethodResponse,
    ) {
        val sessionLog = SessionLog(
            sessionId = sessionId,
            clientPubKey = method.clientPubKey,
            methodType = method.asSignerMethodType(),
            eventKind = if (method is RemoteSignerMethod.SignEvent) method.unsignedEvent.kind else null,
            isSuccess = response is RemoteSignerMethodResponse.Success,
            methodPayload = CommonJsonEncodeDefaults.encodeToString(RemoteSignerMethod.serializer(), method),
            responsePayload = CommonJsonEncodeDefaults.encodeToString(
                RemoteSignerMethodResponse.serializer(),
                response,
            ),
        )

        accountDatabase.sessionLogs().upsert(data = sessionLog.asLocal())
    }
}

private fun RemoteSignerMethod.asSignerMethodType(): SignerMethodType {
    return when (this) {
        is RemoteSignerMethod.Connect -> SignerMethodType.CONNECT
        is RemoteSignerMethod.GetPublicKey -> SignerMethodType.GET_PUBLIC_KEY
        is RemoteSignerMethod.Nip04Decrypt -> SignerMethodType.NIP04_DECRYPT
        is RemoteSignerMethod.Nip04Encrypt -> SignerMethodType.NIP04_ENCRYPT
        is RemoteSignerMethod.Nip44Decrypt -> SignerMethodType.NIP44_DECRYPT
        is RemoteSignerMethod.Nip44Encrypt -> SignerMethodType.NIP44_ENCRYPT
        is RemoteSignerMethod.Ping -> SignerMethodType.PING
        is RemoteSignerMethod.SignEvent -> SignerMethodType.SIGN_EVENT
    }
}
