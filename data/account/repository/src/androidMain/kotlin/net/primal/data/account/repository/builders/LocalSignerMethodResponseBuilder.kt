package net.primal.data.account.repository.builders

import net.primal.core.utils.fold
import net.primal.data.account.signer.local.model.LocalSignerMethod
import net.primal.data.account.signer.local.model.LocalSignerMethodResponse
import net.primal.domain.nostr.cryptography.NostrEncryptionHandler
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.utils.getOrNull

class LocalSignerMethodResponseBuilder(
    private val nostrEventSignatureHandler: NostrEventSignatureHandler,
    private val nostrEncryptionHandler: NostrEncryptionHandler,
) {
    suspend fun build(method: LocalSignerMethod): LocalSignerMethodResponse =
        when (method) {
            is LocalSignerMethod.DecryptZapEvent -> LocalSignerMethodResponse.Error(
                eventId = method.eventId,
                message = "Decrypting zap events is not yet implemented.",
            )

            is LocalSignerMethod.GetPublicKey -> error(
                "We shouldn't use this to process `get_public_key` methods as they are special sign in methods.",
            )

            is LocalSignerMethod.Nip04Decrypt -> nip04Decrypt(method)
            is LocalSignerMethod.Nip04Encrypt -> nip04Encrypt(method)
            is LocalSignerMethod.Nip44Decrypt -> nip44Decrypt(method)
            is LocalSignerMethod.Nip44Encrypt -> nip44Encrypt(method)
            is LocalSignerMethod.SignEvent -> signEvent(method)
        }

    private fun nip44Encrypt(method: LocalSignerMethod.Nip44Encrypt): LocalSignerMethodResponse =
        nostrEncryptionHandler.nip44Encrypt(
            userId = method.userPubKey,
            participantId = method.otherPubKey,
            plaintext = method.plaintext,
        ).fold(
            onSuccess = {
                LocalSignerMethodResponse.Success.Nip44Encrypt(
                    eventId = method.eventId,
                    ciphertext = it,
                )
            },
            onFailure = { it.asErrorResponse(eventId = method.eventId) },
        )

    private fun nip04Encrypt(method: LocalSignerMethod.Nip04Encrypt): LocalSignerMethodResponse =
        nostrEncryptionHandler.nip04Encrypt(
            userId = method.userPubKey,
            participantId = method.otherPubKey,
            plaintext = method.plaintext,
        ).fold(
            onSuccess = {
                LocalSignerMethodResponse.Success.Nip04Encrypt(
                    eventId = method.eventId,
                    ciphertext = it,
                )
            },
            onFailure = { it.asErrorResponse(eventId = method.eventId) },
        )

    private fun nip44Decrypt(method: LocalSignerMethod.Nip44Decrypt): LocalSignerMethodResponse =
        nostrEncryptionHandler.nip44Decrypt(
            userId = method.userPubKey,
            participantId = method.otherPubKey,
            ciphertext = method.ciphertext,
        ).fold(
            onSuccess = {
                LocalSignerMethodResponse.Success.Nip44Encrypt(
                    eventId = method.eventId,
                    ciphertext = it,
                )
            },
            onFailure = { it.asErrorResponse(eventId = method.eventId) },
        )

    private fun nip04Decrypt(method: LocalSignerMethod.Nip04Decrypt): LocalSignerMethodResponse =
        nostrEncryptionHandler.nip04Decrypt(
            userId = method.userPubKey,
            participantId = method.otherPubKey,
            ciphertext = method.ciphertext,
        ).fold(
            onSuccess = {
                LocalSignerMethodResponse.Success.Nip04Encrypt(
                    eventId = method.eventId,
                    ciphertext = it,
                )
            },
            onFailure = { it.asErrorResponse(eventId = method.eventId) },
        )

    private suspend fun signEvent(method: LocalSignerMethod.SignEvent): LocalSignerMethodResponse {
        val signedEvent = nostrEventSignatureHandler
            .signNostrEvent(unsignedNostrEvent = method.unsignedEvent).getOrNull()
            ?: return LocalSignerMethodResponse.Error(
                eventId = method.eventId,
                message = "Something went wrong while signing event.",
            )

        return LocalSignerMethodResponse.Success.SignEvent(
            eventId = method.eventId,
            signedEvent = signedEvent,
        )
    }
}

private fun Throwable.asErrorResponse(eventId: String) =
    LocalSignerMethodResponse.Error(
        eventId = eventId,
        message = this.message ?: "Something went wrong while processing this event.",
    )
