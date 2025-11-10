package net.primal.data.account.repository.mappers

import net.primal.data.account.local.dao.RemoteSignerMethodDataType
import net.primal.data.account.local.dao.SessionEventData
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse
import net.primal.domain.account.model.RequestState
import net.primal.domain.account.model.SessionEvent
import net.primal.shared.data.local.encryption.asEncryptable

private const val REQ_ID_GET_PUBLIC_KEY = "get_public_key"
private const val REQ_ID_NIP04_DECRYPT = "nip04_decrypt"
private const val REQ_ID_NIP04_ENCRYPT = "nip04_encrypt"
private const val REQ_ID_NIP44_DECRYPT = "nip44_decrypt"
private const val REQ_ID_NIP44_ENCRYPT = "nip44_encrypt"
private const val REQ_ID_PREFIX_SIGN_EVENT = "sign_event:"

fun buildSessionEventData(
    sessionId: String,
    requestedAt: Long,
    completedAt: Long,
    method: RemoteSignerMethod,
    response: RemoteSignerMethodResponse,
): SessionEventData? {
    val requestTypeId = method.toRequestTypeId() ?: return null
    val requestType = method.toDataType()

    val baseData = SessionEventData(
        eventId = method.id,
        sessionId = sessionId,
        clientPubKey = method.clientPubKey.asEncryptable(),
        requestState = if (response is RemoteSignerMethodResponse.Success) {
            RequestState.APPROVED
        } else {
            RequestState.REJECTED
        },
        requestedAt = requestedAt,
        completedAt = completedAt,
        requestType = requestType,
        requestTypeId = requestTypeId.asEncryptable(),
        responsePayload = when (response) {
            is RemoteSignerMethodResponse.Success -> response.result.asEncryptable()
            is RemoteSignerMethodResponse.Error -> response.error.asEncryptable()
        },
        eventKind = null,
        thirdPartyPubKey = null,
        plaintext = null,
        ciphertext = null,
    )

    return when (method) {
        is RemoteSignerMethod.GetPublicKey -> baseData

        is RemoteSignerMethod.Nip04Encrypt -> baseData.copy(
            thirdPartyPubKey = method.thirdPartyPubKey.asEncryptable(),
            plaintext = method.plaintext.asEncryptable(),
        )

        is RemoteSignerMethod.Nip44Encrypt -> baseData.copy(
            thirdPartyPubKey = method.thirdPartyPubKey.asEncryptable(),
            plaintext = method.plaintext.asEncryptable(),
        )

        is RemoteSignerMethod.Nip04Decrypt -> baseData.copy(
            thirdPartyPubKey = method.thirdPartyPubKey.asEncryptable(),
            ciphertext = method.ciphertext.asEncryptable(),
        )

        is RemoteSignerMethod.Nip44Decrypt -> baseData.copy(
            thirdPartyPubKey = method.thirdPartyPubKey.asEncryptable(),
            ciphertext = method.ciphertext.asEncryptable(),
        )

        is RemoteSignerMethod.SignEvent -> baseData.copy(
            eventKind = method.unsignedEvent.kind.asEncryptable(),
        )

        is RemoteSignerMethod.Connect, is RemoteSignerMethod.Ping -> null
    }
}

fun SessionEventData.asDomain(): SessionEvent? {
    val responsePayload = this.responsePayload?.decrypted

    return when (this.requestType) {
        RemoteSignerMethodDataType.GET_PUBLIC_KEY -> SessionEvent.GetPublicKey(
            eventId = this.eventId,
            sessionId = this.sessionId,
            requestState = this.requestState,
            requestedAt = this.requestedAt,
            completedAt = this.completedAt,
            publicKey = if (this.requestState == RequestState.APPROVED) responsePayload else null,
        )

        RemoteSignerMethodDataType.NIP04_ENCRYPT,
        RemoteSignerMethodDataType.NIP44_ENCRYPT,
        -> {
            val thirdPartyPubKey = this.thirdPartyPubKey?.decrypted ?: return null
            val plaintext = this.plaintext?.decrypted ?: return null
            SessionEvent.Encrypt(
                eventId = this.eventId,
                sessionId = this.sessionId,
                requestState = this.requestState,
                requestedAt = this.requestedAt,
                completedAt = this.completedAt,
                requestTypeId = this.requestTypeId.decrypted,
                thirdPartyPubKey = thirdPartyPubKey,
                plaintext = plaintext,
                encryptedPayload = if (this.requestState == RequestState.APPROVED) responsePayload else null,
            )
        }

        RemoteSignerMethodDataType.NIP04_DECRYPT,
        RemoteSignerMethodDataType.NIP44_DECRYPT,
        -> {
            val thirdPartyPubKey = this.thirdPartyPubKey?.decrypted ?: return null
            val ciphertext = this.ciphertext?.decrypted ?: return null
            SessionEvent.Decrypt(
                eventId = this.eventId,
                sessionId = this.sessionId,
                requestState = this.requestState,
                requestedAt = this.requestedAt,
                completedAt = this.completedAt,
                requestTypeId = this.requestTypeId.decrypted,
                thirdPartyPubKey = thirdPartyPubKey,
                ciphertext = ciphertext,
                decryptedPayload = if (this.requestState == RequestState.APPROVED) responsePayload else null,
            )
        }

        RemoteSignerMethodDataType.SIGN_EVENT -> {
            val eventKind = this.eventKind?.decrypted ?: return null
            SessionEvent.SignEvent(
                eventId = this.eventId,
                sessionId = this.sessionId,
                requestState = this.requestState,
                requestedAt = this.requestedAt,
                completedAt = this.completedAt,
                eventKind = eventKind,
                signedNostrEventJson = if (this.requestState == RequestState.APPROVED) responsePayload else null,
            )
        }

        RemoteSignerMethodDataType.CONNECT, RemoteSignerMethodDataType.PING -> null
    }
}

private fun RemoteSignerMethod.toDataType(): RemoteSignerMethodDataType {
    return when (this) {
        is RemoteSignerMethod.Connect -> RemoteSignerMethodDataType.CONNECT
        is RemoteSignerMethod.GetPublicKey -> RemoteSignerMethodDataType.GET_PUBLIC_KEY
        is RemoteSignerMethod.Nip04Decrypt -> RemoteSignerMethodDataType.NIP04_DECRYPT
        is RemoteSignerMethod.Nip04Encrypt -> RemoteSignerMethodDataType.NIP04_ENCRYPT
        is RemoteSignerMethod.Nip44Decrypt -> RemoteSignerMethodDataType.NIP44_DECRYPT
        is RemoteSignerMethod.Nip44Encrypt -> RemoteSignerMethodDataType.NIP44_ENCRYPT
        is RemoteSignerMethod.Ping -> RemoteSignerMethodDataType.PING
        is RemoteSignerMethod.SignEvent -> RemoteSignerMethodDataType.SIGN_EVENT
    }
}

private fun RemoteSignerMethod.toRequestTypeId(): String? {
    return when (this) {
        is RemoteSignerMethod.GetPublicKey -> REQ_ID_GET_PUBLIC_KEY
        is RemoteSignerMethod.Nip04Decrypt -> REQ_ID_NIP04_DECRYPT
        is RemoteSignerMethod.Nip04Encrypt -> REQ_ID_NIP04_ENCRYPT
        is RemoteSignerMethod.Nip44Decrypt -> REQ_ID_NIP44_DECRYPT
        is RemoteSignerMethod.Nip44Encrypt -> REQ_ID_NIP44_ENCRYPT
        is RemoteSignerMethod.SignEvent -> "$REQ_ID_PREFIX_SIGN_EVENT${this.unsignedEvent.kind}"
        is RemoteSignerMethod.Connect, is RemoteSignerMethod.Ping -> null
    }
}
