package net.primal.data.account.repository.builder

import net.primal.core.utils.Result
import net.primal.core.utils.map
import net.primal.core.utils.runCatching
import net.primal.domain.account.model.LocalSignerMethod
import net.primal.domain.account.model.LocalSignerMethodResponse
import net.primal.domain.nostr.cryptography.NostrEncryptionHandler
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.utils.getOrNull

class LocalSignerMethodResponseBuilder(
    private val nostrEventSignatureHandler: NostrEventSignatureHandler,
    private val nostrEncryptionHandler: NostrEncryptionHandler,
) {
    suspend fun build(method: LocalSignerMethod): Result<LocalSignerMethodResponse> =
        runCatching {
            when (method) {
                is LocalSignerMethod.DecryptZapEvent -> error("Not yet implemented.")
                is LocalSignerMethod.GetPublicKey -> error("We shouldn't use this to process `get_public_key` methods as they are special sign in methods.")
                is LocalSignerMethod.Nip04Decrypt -> nip04Decrypt(method)
                is LocalSignerMethod.Nip04Encrypt -> nip04Encrypt(method)
                is LocalSignerMethod.Nip44Decrypt -> nip44Decrypt(method)
                is LocalSignerMethod.Nip44Encrypt -> nip44Encrypt(method)
                is LocalSignerMethod.SignEvent -> signEvent(method)
            }
        }

    private fun nip44Encrypt(method: LocalSignerMethod.Nip44Encrypt): LocalSignerMethodResponse =
        nostrEncryptionHandler.nip44Encrypt(
            userId = method.userPubKey,
            participantId = method.otherPubKey,
            plaintext = method.plaintext,
        ).map {
            LocalSignerMethodResponse.Nip44Encrypt(
                eventId = method.eventId,
                ciphertext = it,
            )
        }.getOrThrow()

    private fun nip04Encrypt(method: LocalSignerMethod.Nip04Encrypt): LocalSignerMethodResponse =
        nostrEncryptionHandler.nip04Encrypt(
            userId = method.userPubKey,
            participantId = method.otherPubKey,
            plaintext = method.plaintext,
        ).map {
            LocalSignerMethodResponse.Nip04Encrypt(
                eventId = method.eventId,
                ciphertext = it,
            )
        }.getOrThrow()

    private fun nip44Decrypt(method: LocalSignerMethod.Nip44Decrypt): LocalSignerMethodResponse =
        nostrEncryptionHandler.nip44Decrypt(
            userId = method.userPubKey,
            participantId = method.otherPubKey,
            ciphertext = method.ciphertext,
        ).map {
            LocalSignerMethodResponse.Nip44Encrypt(
                eventId = method.eventId,
                ciphertext = it,
            )
        }.getOrThrow()

    private fun nip04Decrypt(method: LocalSignerMethod.Nip04Decrypt): LocalSignerMethodResponse =
        nostrEncryptionHandler.nip04Decrypt(
            userId = method.userPubKey,
            participantId = method.otherPubKey,
            ciphertext = method.ciphertext,
        ).map {
            LocalSignerMethodResponse.Nip04Encrypt(
                eventId = method.eventId,
                ciphertext = it,
            )
        }.getOrThrow()

    private suspend fun signEvent(method: LocalSignerMethod.SignEvent): LocalSignerMethodResponse {
        val signedEvent = nostrEventSignatureHandler
            .signNostrEvent(unsignedNostrEvent = method.unsignedEvent).getOrNull()
            ?: throw RuntimeException("Something went wrong while signing event.")

        return LocalSignerMethodResponse.SignEvent(
            eventId = method.eventId,
            signedEvent = signedEvent,
        )
    }
}
