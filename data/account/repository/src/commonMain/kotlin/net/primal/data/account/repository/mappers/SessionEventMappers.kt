package net.primal.data.account.repository.mappers

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import net.primal.core.utils.getIfTypeOrNull
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.account.local.dao.RequestState as RequestStatePO
import net.primal.data.account.local.dao.SessionEventData
import net.primal.data.account.local.dao.SignerMethodType
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse
import net.primal.data.account.remote.method.model.withPubKey
import net.primal.data.account.remote.utils.PERM_ID_CONNECT
import net.primal.data.account.remote.utils.PERM_ID_GET_PUBLIC_KEY
import net.primal.data.account.remote.utils.PERM_ID_NIP04_DECRYPT
import net.primal.data.account.remote.utils.PERM_ID_NIP04_ENCRYPT
import net.primal.data.account.remote.utils.PERM_ID_NIP44_DECRYPT
import net.primal.data.account.remote.utils.PERM_ID_NIP44_ENCRYPT
import net.primal.data.account.remote.utils.PERM_ID_PING
import net.primal.data.account.remote.utils.PERM_ID_PREFIX_SIGN_EVENT
import net.primal.domain.account.model.RequestState as RequestStateDO
import net.primal.domain.account.model.SessionEvent
import net.primal.shared.data.local.encryption.asEncryptable

@OptIn(ExperimentalUuidApi::class)
fun buildSessionEventData(
    sessionId: String,
    signerPubKey: String,
    requestedAt: Long,
    requestType: SignerMethodType,
    completedAt: Long?,
    method: RemoteSignerMethod?,
    response: RemoteSignerMethodResponse?,
    requestState: RequestStatePO?,
): SessionEventData? {
    if (requestType == SignerMethodType.Ping) return null

    val resolvedRequestState = requestState ?: when (response) {
        is RemoteSignerMethodResponse.Error -> RequestStatePO.Rejected
        is RemoteSignerMethodResponse.Success -> RequestStatePO.Approved
        null -> RequestStatePO.PendingUserAction
    }

    return SessionEventData(
        eventId = method?.id ?: response?.id ?: Uuid.random().toString(),
        sessionId = sessionId,
        signerPubKey = signerPubKey.asEncryptable(),
        clientPubKey = method?.clientPubKey?.asEncryptable() ?: response?.clientPubKey?.asEncryptable() ?: return null,
        requestState = resolvedRequestState,
        requestedAt = requestedAt,
        completedAt = completedAt,
        requestType = requestType,
        requestPayload = method.encodeToJsonString().asEncryptable(),
        responsePayload = response?.encodeToJsonString()?.asEncryptable(),
        eventKind = method.getIfTypeOrNull(RemoteSignerMethod.SignEvent::unsignedEvent)?.kind?.asEncryptable(),
    )
}

fun SessionEventData.asDomain(): SessionEvent? {
    val responsePayload = this.responsePayload?.decrypted
    val requestPayload = this.requestPayload?.decrypted

    return when (this.requestType) {
        SignerMethodType.GetPublicKey -> SessionEvent.GetPublicKey(
            eventId = this.eventId,
            sessionId = this.sessionId,
            requestState = this.requestState.asDomain(),
            requestedAt = this.requestedAt,
            completedAt = this.completedAt,
        )

        SignerMethodType.Nip04Encrypt,
        SignerMethodType.Nip44Encrypt,
        -> {
            SessionEvent.Encrypt(
                eventId = this.eventId,
                sessionId = this.sessionId,
                requestState = this.requestState.asDomain(),
                requestedAt = this.requestedAt,
                completedAt = this.completedAt,
                requestTypeId = this.getRequestTypeId(),
            )
        }

        SignerMethodType.Nip04Decrypt,
        SignerMethodType.Nip44Decrypt,
        -> {
            SessionEvent.Decrypt(
                eventId = this.eventId,
                sessionId = this.sessionId,
                requestState = this.requestState.asDomain(),
                requestedAt = this.requestedAt,
                completedAt = this.completedAt,
                requestTypeId = this.getRequestTypeId(),
            )
        }

        SignerMethodType.SignEvent -> {
            val eventKind = this.eventKind?.decrypted ?: return null
            val requestMethod = requestPayload?.decodeFromJsonStringOrNull<RemoteSignerMethod>()

            val unsignedEventJson = requestMethod
                .getIfTypeOrNull(RemoteSignerMethod.SignEvent::unsignedEvent)
                ?.withPubKey(this.signerPubKey.decrypted)
                ?.encodeToJsonString()
                ?: ""

            SessionEvent.SignEvent(
                eventId = this.eventId,
                sessionId = this.sessionId,
                requestState = this.requestState.asDomain(),
                requestedAt = this.requestedAt,
                completedAt = this.completedAt,
                eventKind = eventKind,
                signedNostrEventJson = getResponseBody(responsePayload),
                unsignedNostrEventJson = unsignedEventJson,
            )
        }

        SignerMethodType.Connect, SignerMethodType.Ping -> null
    }
}

private fun getResponseBody(responsePayload: String?) =
    when (val response = responsePayload.decodeFromJsonStringOrNull<RemoteSignerMethodResponse>()) {
        is RemoteSignerMethodResponse.Error -> response.error
        is RemoteSignerMethodResponse.Success -> response.result
        null -> null
    }

fun RemoteSignerMethod.getRequestType(): SignerMethodType {
    return when (this) {
        is RemoteSignerMethod.Connect -> SignerMethodType.Connect
        is RemoteSignerMethod.GetPublicKey -> SignerMethodType.GetPublicKey
        is RemoteSignerMethod.Nip04Decrypt -> SignerMethodType.Nip04Decrypt
        is RemoteSignerMethod.Nip04Encrypt -> SignerMethodType.Nip04Encrypt
        is RemoteSignerMethod.Nip44Decrypt -> SignerMethodType.Nip44Decrypt
        is RemoteSignerMethod.Nip44Encrypt -> SignerMethodType.Nip44Encrypt
        is RemoteSignerMethod.Ping -> SignerMethodType.Ping
        is RemoteSignerMethod.SignEvent -> SignerMethodType.SignEvent
    }
}

fun SessionEventData.getRequestTypeId() =
    when (this.requestType) {
        SignerMethodType.Connect -> PERM_ID_CONNECT
        SignerMethodType.Ping -> PERM_ID_PING
        SignerMethodType.SignEvent -> "$PERM_ID_PREFIX_SIGN_EVENT${this.eventKind?.decrypted}"
        SignerMethodType.GetPublicKey -> PERM_ID_GET_PUBLIC_KEY
        SignerMethodType.Nip04Encrypt -> PERM_ID_NIP04_ENCRYPT
        SignerMethodType.Nip04Decrypt -> PERM_ID_NIP04_DECRYPT
        SignerMethodType.Nip44Encrypt -> PERM_ID_NIP44_ENCRYPT
        SignerMethodType.Nip44Decrypt -> PERM_ID_NIP44_DECRYPT
    }

private fun RequestStatePO.asDomain(): RequestStateDO =
    when (this) {
        RequestStatePO.PendingUserAction, RequestStatePO.PendingResponse -> RequestStateDO.Pending
        RequestStatePO.Approved -> RequestStateDO.Approved
        RequestStatePO.Rejected -> RequestStateDO.Rejected
    }
