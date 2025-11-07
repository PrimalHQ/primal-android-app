package net.primal.data.account.repository.processor

import net.primal.core.utils.serialization.CommonJsonEncodeDefaults
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse
import net.primal.domain.account.model.SignerLog
import net.primal.domain.account.repository.SignerLogRepository

internal class SignerLogProcessor(
    private val signerLogRepository: SignerLogRepository,
) {
    suspend fun processAndLog(
        sessionId: String,
        method: RemoteSignerMethod,
        response: RemoteSignerMethodResponse,
    ) {
        val json = CommonJsonEncodeDefaults
        val requestPayload = json.encodeToString(RemoteSignerMethod.serializer(), method)
        val responsePayload = json.encodeToString(RemoteSignerMethodResponse.serializer(), response)
        val isSuccess = response is RemoteSignerMethodResponse.Success

        val log = SignerLog(
            sessionId = sessionId,
            clientPubKey = method.clientPubKey,
            methodType = method::class.simpleName ?: "Unknown",
            eventKind = if (method is RemoteSignerMethod.SignEvent) method.unsignedEvent.kind else null,
            isSuccess = isSuccess,
            methodPayload = requestPayload,
            responsePayload = responsePayload,
        )

        signerLogRepository.saveLog(log = log)
    }
}
