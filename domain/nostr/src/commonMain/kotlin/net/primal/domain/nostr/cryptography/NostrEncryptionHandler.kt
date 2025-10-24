package net.primal.domain.nostr.cryptography

import net.primal.core.utils.Result

interface NostrEncryptionHandler {
    fun nip04Encrypt(
        userId: String,
        participantId: String,
        plaintext: String,
    ): Result<String>

    fun nip04Decrypt(
        userId: String,
        participantId: String,
        ciphertext: String,
    ): Result<String>

    fun nip44Encrypt(
        userId: String,
        participantId: String,
        plaintext: String,
    ): Result<String>

    fun nip44Decrypt(
        userId: String,
        participantId: String,
        ciphertext: String,
    ): Result<String>
}
