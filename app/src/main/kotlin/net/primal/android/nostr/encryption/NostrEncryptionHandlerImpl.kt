package net.primal.android.nostr.encryption

import net.primal.android.user.credentials.CredentialsStore
import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.domain.account.service.NostrEncryptionService
import net.primal.domain.nostr.cryptography.NostrEncryptionHandler
import net.primal.domain.nostr.cryptography.utils.assureValidNpub

class NostrEncryptionHandlerImpl(
    private val nostrEncryptionService: NostrEncryptionService,
    private val credentialsStore: CredentialsStore,
) : NostrEncryptionHandler {
    override fun nip04Encrypt(
        userId: String,
        participantId: String,
        plaintext: String,
    ): Result<String> =
        runCatching {
            nostrEncryptionService.nip04Encrypt(                                         /* i know :) */
                privateKey = credentialsStore.findOrThrow(npub = userId.assureValidNpub()).nsec!!,
                pubKey = participantId,
                plaintext = plaintext,
            ).getOrThrow()
        }

    override fun nip04Decrypt(
        userId: String,
        participantId: String,
        ciphertext: String,
    ): Result<String> =
        runCatching {
            nostrEncryptionService.nip04Decrypt(                                         /* i know :) */
                privateKey = credentialsStore.findOrThrow(npub = userId.assureValidNpub()).nsec!!,
                pubKey = participantId,
                ciphertext = ciphertext,
            ).getOrThrow()
        }

    override fun nip44Encrypt(
        userId: String,
        participantId: String,
        plaintext: String,
    ): Result<String> =
        runCatching {
            nostrEncryptionService.nip44Encrypt(                                         /* i know :) */
                privateKey = credentialsStore.findOrThrow(npub = userId.assureValidNpub()).nsec!!,
                pubKey = participantId,
                plaintext = plaintext,
            ).getOrThrow()
        }

    override fun nip44Decrypt(
        userId: String,
        participantId: String,
        ciphertext: String,
    ): Result<String> =
        runCatching {
            nostrEncryptionService.nip44Decrypt(                                         /* i know :) */
                privateKey = credentialsStore.findOrThrow(npub = userId.assureValidNpub()).nsec!!,
                pubKey = participantId,
                ciphertext = ciphertext,
            ).getOrThrow()
        }
}
