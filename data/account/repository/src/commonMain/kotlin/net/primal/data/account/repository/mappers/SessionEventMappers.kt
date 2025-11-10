package net.primal.data.account.repository.mappers

import net.primal.data.account.local.dao.RequestState as RequestStatePO
import net.primal.data.account.local.dao.SessionEventData
import net.primal.data.account.local.dao.SignerMethodType
import net.primal.data.account.remote.method.model.RemoteSignerMethod
import net.primal.data.account.remote.method.model.RemoteSignerMethodResponse
import net.primal.domain.account.model.RequestState as RequestStateDO
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

    val domainRequestState = if (response is RemoteSignerMethodResponse.Success) {
        RequestStateDO.Approved
    } else {
        RequestStateDO.Rejected
    }

    val baseData = SessionEventData(
        eventId = method.id,
        sessionId = sessionId,
        clientPubKey = method.clientPubKey.asEncryptable(),
        requestState = domainRequestState.asPersistence(),
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
        SignerMethodType.GetPublicKey -> SessionEvent.GetPublicKey(
            eventId = this.eventId,
            sessionId = this.sessionId,
            requestState = this.requestState.asDomain(),
            requestedAt = this.requestedAt,
            completedAt = this.completedAt,
            publicKey = if (this.requestState == RequestStatePO.Approved) responsePayload else null,
        )

        SignerMethodType.Nip04Encrypt,
        SignerMethodType.Nip44Encrypt,
        -> {
            val thirdPartyPubKey = this.thirdPartyPubKey?.decrypted ?: return null
            val plaintext = this.plaintext?.decrypted ?: return null
            val requestTypeId = this.requestTypeId.decrypted ?: return null
            SessionEvent.Encrypt(
                eventId = this.eventId,
                sessionId = this.sessionId,
                requestState = this.requestState.asDomain(),
                requestedAt = this.requestedAt,
                completedAt = this.completedAt,
                requestTypeId = requestTypeId,
                thirdPartyPubKey = thirdPartyPubKey,
                plaintext = plaintext,
                encryptedPayload = if (this.requestState == RequestStatePO.Approved) responsePayload else null,
            )
        }

        SignerMethodType.Nip04Decrypt,
        SignerMethodType.Nip44Decrypt,
        -> {
            val thirdPartyPubKey = this.thirdPartyPubKey?.decrypted ?: return null
            val ciphertext = this.ciphertext?.decrypted ?: return null
            val requestTypeId = this.requestTypeId.decrypted ?: return null
            SessionEvent.Decrypt(
                eventId = this.eventId,
                sessionId = this.sessionId,
                requestState = this.requestState.asDomain(),
                requestedAt = this.requestedAt,
                completedAt = this.completedAt,
                requestTypeId = requestTypeId,
                thirdPartyPubKey = thirdPartyPubKey,
                ciphertext = ciphertext,
                decryptedPayload = if (this.requestState == RequestStatePO.Approved) responsePayload else null,
            )
        }

        SignerMethodType.SignEvent -> {
            val eventKind = this.eventKind?.decrypted ?: return null
            SessionEvent.SignEvent(
                eventId = this.eventId,
                sessionId = this.sessionId,
                requestState = this.requestState.asDomain(),
                requestedAt = this.requestedAt,
                completedAt = this.completedAt,
                eventKind = eventKind,
                signedNostrEventJson = if (this.requestState == RequestStatePO.Approved) responsePayload else null,
            )
        }

        SignerMethodType.Connect, SignerMethodType.Ping -> null
    }
}

private fun RemoteSignerMethod.toDataType(): SignerMethodType {
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

private fun RequestStateDO.asPersistence(): RequestStatePO =
    when (this) {
        RequestStateDO.Pending -> RequestStatePO.Pending
        RequestStateDO.Approved -> RequestStatePO.Approved
        RequestStateDO.Rejected -> RequestStatePO.Rejected
    }

private fun RequestStatePO.asDomain(): RequestStateDO =
    when (this) {
        RequestStatePO.Pending -> RequestStateDO.Pending
        RequestStatePO.Approved -> RequestStateDO.Approved
        RequestStatePO.Rejected -> RequestStateDO.Rejected
    }
