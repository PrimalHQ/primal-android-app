package net.primal.data.account.repository.mappers

import net.primal.core.utils.getIfTypeOrNull
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.account.local.dao.apps.AppRequestState
import net.primal.data.account.local.dao.apps.local.LocalAppSessionEventData
import net.primal.data.account.local.dao.apps.local.LocalSignerMethodType
import net.primal.data.account.remote.utils.PERM_ID_DECRYPT_ZAP_EVENT
import net.primal.data.account.remote.utils.PERM_ID_GET_PUBLIC_KEY
import net.primal.data.account.remote.utils.PERM_ID_NIP04_DECRYPT
import net.primal.data.account.remote.utils.PERM_ID_NIP04_ENCRYPT
import net.primal.data.account.remote.utils.PERM_ID_NIP44_DECRYPT
import net.primal.data.account.remote.utils.PERM_ID_NIP44_ENCRYPT
import net.primal.data.account.remote.utils.PERM_ID_PREFIX_SIGN_EVENT
import net.primal.domain.account.model.LocalSignerMethod
import net.primal.domain.account.model.LocalSignerMethodResponse
import net.primal.domain.account.model.RequestState as RequestStateDO
import net.primal.domain.account.model.SessionEvent
import net.primal.domain.account.model.SessionEvent.Decrypt
import net.primal.domain.account.model.SessionEvent.Encrypt
import net.primal.domain.account.model.SessionEvent.GetPublicKey
import net.primal.domain.account.model.SessionEvent.SignEvent
import net.primal.shared.data.local.encryption.asEncryptable

fun buildSessionEventData(
    sessionId: String,
    requestedAt: Long,
    requestType: LocalSignerMethodType,
    completedAt: Long?,
    method: LocalSignerMethod?,
    response: LocalSignerMethodResponse?,
    requestState: AppRequestState?,
): LocalAppSessionEventData? {
    val appIdentifier = method?.getIdentifier() ?: return null

    val resolvedRequestState = requestState ?: when (response) {
        is LocalSignerMethodResponse.Error -> AppRequestState.Rejected
        is LocalSignerMethodResponse.Success -> AppRequestState.Approved
        else -> AppRequestState.PendingUserAction
    }

    return LocalAppSessionEventData(
        eventId = method.eventId,
        sessionId = sessionId,
        appIdentifier = appIdentifier,
        requestState = resolvedRequestState,
        requestedAt = requestedAt,
        completedAt = completedAt,
        requestType = requestType,
        requestPayload = method.encodeToJsonString().asEncryptable(),
        responsePayload = response?.encodeToJsonString()?.asEncryptable(),
        eventKind = method.getIfTypeOrNull(LocalSignerMethod.SignEvent::unsignedEvent)?.kind?.asEncryptable(),
    )
}

fun LocalAppSessionEventData.asDomain(): SessionEvent? {
    val responsePayload = this.responsePayload?.decrypted
    val requestPayload = this.requestPayload?.decrypted
    val requestMethod = requestPayload?.decodeFromJsonStringOrNull<LocalSignerMethod>()

    return when (this.requestType) {
        LocalSignerMethodType.SignEvent -> {
            val eventKind = this.eventKind?.decrypted ?: return null

            val unsignedEventJson = requestMethod
                .getIfTypeOrNull(LocalSignerMethod.SignEvent::unsignedEvent)
                ?.encodeToJsonString()
                ?: ""

            SignEvent(
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

        LocalSignerMethodType.GetPublicKey -> {
            val resultKey = getResponseBody(responsePayload)

            GetPublicKey(
                eventId = this.eventId,
                sessionId = this.sessionId,
                requestState = this.requestState.asDomain(),
                requestedAt = this.requestedAt,
                completedAt = this.completedAt,
                publicKey = resultKey,
            )
        }

        LocalSignerMethodType.Nip04Encrypt,
        LocalSignerMethodType.Nip44Encrypt,
        -> {
            val plainText = when (requestMethod) {
                is LocalSignerMethod.Nip04Encrypt -> requestMethod.plaintext
                is LocalSignerMethod.Nip44Encrypt -> requestMethod.plaintext
                else -> null
            }
            val encryptedText = getResponseBody(responsePayload)

            Encrypt(
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

        LocalSignerMethodType.Nip04Decrypt,
        LocalSignerMethodType.Nip44Decrypt,
        -> {
            val encryptedText = when (requestMethod) {
                is LocalSignerMethod.Nip04Decrypt -> requestMethod.ciphertext
                is LocalSignerMethod.Nip44Decrypt -> requestMethod.ciphertext
                else -> null
            }
            val plainText = getResponseBody(responsePayload)

            Decrypt(
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

        LocalSignerMethodType.DecryptZapEvent -> {
            null
        }
    }
}

private fun getResponseBody(responsePayload: String?) =
    responsePayload.decodeFromJsonStringOrNull<LocalSignerMethodResponse>()?.let {
        when (it) {
            is LocalSignerMethodResponse.Error -> it.message
            is LocalSignerMethodResponse.Success.DecryptZapEvent -> it.signedEvent.encodeToJsonString()
            is LocalSignerMethodResponse.Success.GetPublicKey -> it.pubkey
            is LocalSignerMethodResponse.Success.Nip04Decrypt -> it.plaintext
            is LocalSignerMethodResponse.Success.Nip04Encrypt -> it.ciphertext
            is LocalSignerMethodResponse.Success.Nip44Decrypt -> it.plaintext
            is LocalSignerMethodResponse.Success.Nip44Encrypt -> it.ciphertext
            is LocalSignerMethodResponse.Success.SignEvent -> it.signedEvent.encodeToJsonString()
        }
    }

fun LocalSignerMethod.getRequestType(): LocalSignerMethodType {
    return when (this) {
        is LocalSignerMethod.DecryptZapEvent -> LocalSignerMethodType.DecryptZapEvent
        is LocalSignerMethod.GetPublicKey -> LocalSignerMethodType.GetPublicKey
        is LocalSignerMethod.Nip04Decrypt -> LocalSignerMethodType.Nip04Decrypt
        is LocalSignerMethod.Nip04Encrypt -> LocalSignerMethodType.Nip04Encrypt
        is LocalSignerMethod.Nip44Decrypt -> LocalSignerMethodType.Nip44Decrypt
        is LocalSignerMethod.Nip44Encrypt -> LocalSignerMethodType.Nip44Encrypt
        is LocalSignerMethod.SignEvent -> LocalSignerMethodType.SignEvent
    }
}

fun LocalAppSessionEventData.getRequestTypeId() =
    when (this.requestType) {
        LocalSignerMethodType.SignEvent -> "$PERM_ID_PREFIX_SIGN_EVENT${this.eventKind?.decrypted}"
        LocalSignerMethodType.GetPublicKey -> PERM_ID_GET_PUBLIC_KEY
        LocalSignerMethodType.Nip04Encrypt -> PERM_ID_NIP04_ENCRYPT
        LocalSignerMethodType.Nip04Decrypt -> PERM_ID_NIP04_DECRYPT
        LocalSignerMethodType.Nip44Encrypt -> PERM_ID_NIP44_ENCRYPT
        LocalSignerMethodType.Nip44Decrypt -> PERM_ID_NIP44_DECRYPT
        LocalSignerMethodType.DecryptZapEvent -> PERM_ID_DECRYPT_ZAP_EVENT
    }

private fun AppRequestState.asDomain(): RequestStateDO =
    when (this) {
        AppRequestState.Approved -> RequestStateDO.Approved
        AppRequestState.Rejected -> RequestStateDO.Rejected
        AppRequestState.PendingUserAction -> RequestStateDO.Pending
        AppRequestState.PendingResponse -> RequestStateDO.Pending
    }
