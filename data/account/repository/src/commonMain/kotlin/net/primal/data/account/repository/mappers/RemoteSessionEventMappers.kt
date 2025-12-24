package net.primal.data.account.repository.mappers

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import net.primal.core.utils.getIfTypeOrNull
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.account.local.dao.apps.AppRequestState as RemoteAppRequestStatePO
import net.primal.data.account.local.dao.apps.remote.RemoteAppSessionEventData
import net.primal.data.account.local.dao.apps.remote.RemoteSignerMethodType
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
    requestType: RemoteSignerMethodType,
    completedAt: Long?,
    method: RemoteSignerMethod?,
    response: RemoteSignerMethodResponse?,
    requestState: RemoteAppRequestStatePO?,
): RemoteAppSessionEventData? {
    val clientPubkey = method?.clientPubKey ?: response?.clientPubKey
    if (requestType == RemoteSignerMethodType.Ping || clientPubkey == null) return null

    val resolvedRequestState = requestState ?: when (response) {
        is RemoteSignerMethodResponse.Error -> RemoteAppRequestStatePO.Rejected
        is RemoteSignerMethodResponse.Success -> RemoteAppRequestStatePO.Approved
        null -> RemoteAppRequestStatePO.PendingUserAction
    }

    return RemoteAppSessionEventData(
        eventId = method?.id ?: response?.id ?: Uuid.random().toString(),
        sessionId = sessionId,
        signerPubKey = signerPubKey,
        clientPubKey = clientPubkey,
        requestState = resolvedRequestState,
        requestedAt = requestedAt,
        completedAt = completedAt,
        requestType = requestType,
        requestPayload = method.encodeToJsonString().asEncryptable(),
        responsePayload = response?.encodeToJsonString()?.asEncryptable(),
        eventKind = method.getIfTypeOrNull(RemoteSignerMethod.SignEvent::unsignedEvent)?.kind?.asEncryptable(),
    )
}

fun RemoteAppSessionEventData.asDomain(): SessionEvent? {
    val responsePayload = this.responsePayload?.decrypted
    val requestPayload = this.requestPayload?.decrypted
    val requestMethod = requestPayload?.decodeFromJsonStringOrNull<RemoteSignerMethod>()

    return when (this.requestType) {
        RemoteSignerMethodType.GetPublicKey -> {
            val resultKey = getResponseBody(responsePayload)

            SessionEvent.GetPublicKey(
                eventId = this.eventId,
                sessionId = this.sessionId,
                requestState = this.requestState.asDomain(),
                requestedAt = this.requestedAt,
                completedAt = this.completedAt,
                publicKey = resultKey,
            )
        }

        RemoteSignerMethodType.Nip04Encrypt,
        RemoteSignerMethodType.Nip44Encrypt,
        -> {
            val plainText = when (requestMethod) {
                is RemoteSignerMethod.Nip04Encrypt -> requestMethod.plaintext
                is RemoteSignerMethod.Nip44Encrypt -> requestMethod.plaintext
                else -> null
            }
            val encryptedText = getResponseBody(responsePayload)

            SessionEvent.Encrypt(
                eventId = this.eventId,
                sessionId = this.sessionId,
                requestState = this.requestState.asDomain(),
                requestedAt = this.requestedAt,
                completedAt = this.completedAt,
                requestTypeId = this.getRequestTypeId(),
                plainText = plainText,
                encryptedText = encryptedText,
            )
        }

        RemoteSignerMethodType.Nip04Decrypt,
        RemoteSignerMethodType.Nip44Decrypt,
        -> {
            val encryptedText = when (requestMethod) {
                is RemoteSignerMethod.Nip04Decrypt -> requestMethod.ciphertext
                is RemoteSignerMethod.Nip44Decrypt -> requestMethod.ciphertext
                else -> null
            }
            val plainText = getResponseBody(responsePayload)

            SessionEvent.Decrypt(
                eventId = this.eventId,
                sessionId = this.sessionId,
                requestState = this.requestState.asDomain(),
                requestedAt = this.requestedAt,
                completedAt = this.completedAt,
                requestTypeId = this.getRequestTypeId(),
                plainText = plainText,
                encryptedText = encryptedText,
            )
        }

        RemoteSignerMethodType.SignEvent -> {
            val eventKind = this.eventKind?.decrypted ?: return null

            val unsignedEventJson = requestMethod
                .getIfTypeOrNull(RemoteSignerMethod.SignEvent::unsignedEvent)
                ?.withPubKey(this.signerPubKey)
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

        RemoteSignerMethodType.Connect, RemoteSignerMethodType.Ping -> null
    }
}

private fun getResponseBody(responsePayload: String?) =
    when (val response = responsePayload.decodeFromJsonStringOrNull<RemoteSignerMethodResponse>()) {
        is RemoteSignerMethodResponse.Error -> response.error
        is RemoteSignerMethodResponse.Success -> response.result
        null -> null
    }

fun RemoteSignerMethod.getRequestType(): RemoteSignerMethodType {
    return when (this) {
        is RemoteSignerMethod.Connect -> RemoteSignerMethodType.Connect
        is RemoteSignerMethod.GetPublicKey -> RemoteSignerMethodType.GetPublicKey
        is RemoteSignerMethod.Nip04Decrypt -> RemoteSignerMethodType.Nip04Decrypt
        is RemoteSignerMethod.Nip04Encrypt -> RemoteSignerMethodType.Nip04Encrypt
        is RemoteSignerMethod.Nip44Decrypt -> RemoteSignerMethodType.Nip44Decrypt
        is RemoteSignerMethod.Nip44Encrypt -> RemoteSignerMethodType.Nip44Encrypt
        is RemoteSignerMethod.Ping -> RemoteSignerMethodType.Ping
        is RemoteSignerMethod.SignEvent -> RemoteSignerMethodType.SignEvent
    }
}

fun RemoteAppSessionEventData.getRequestTypeId() =
    when (this.requestType) {
        RemoteSignerMethodType.Connect -> PERM_ID_CONNECT
        RemoteSignerMethodType.Ping -> PERM_ID_PING
        RemoteSignerMethodType.SignEvent -> "$PERM_ID_PREFIX_SIGN_EVENT${this.eventKind?.decrypted}"
        RemoteSignerMethodType.GetPublicKey -> PERM_ID_GET_PUBLIC_KEY
        RemoteSignerMethodType.Nip04Encrypt -> PERM_ID_NIP04_ENCRYPT
        RemoteSignerMethodType.Nip04Decrypt -> PERM_ID_NIP04_DECRYPT
        RemoteSignerMethodType.Nip44Encrypt -> PERM_ID_NIP44_ENCRYPT
        RemoteSignerMethodType.Nip44Decrypt -> PERM_ID_NIP44_DECRYPT
    }

private fun RemoteAppRequestStatePO.asDomain(): RequestStateDO =
    when (this) {
        RemoteAppRequestStatePO.PendingUserAction, RemoteAppRequestStatePO.PendingResponse -> RequestStateDO.Pending
        RemoteAppRequestStatePO.Approved -> RequestStateDO.Approved
        RemoteAppRequestStatePO.Rejected -> RequestStateDO.Rejected
    }
