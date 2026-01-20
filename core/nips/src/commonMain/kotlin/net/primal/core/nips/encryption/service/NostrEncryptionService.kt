package net.primal.core.nips.encryption.service

import net.primal.core.utils.Result

interface NostrEncryptionService {
    fun nip04Encrypt(
        privateKey: String,
        pubKey: String,
        plaintext: String,
    ): Result<String>

    fun nip04Decrypt(
        privateKey: String,
        pubKey: String,
        ciphertext: String,
    ): Result<String>

    fun nip44Encrypt(
        privateKey: String,
        pubKey: String,
        plaintext: String,
    ): Result<String>

    fun nip44Decrypt(
        privateKey: String,
        pubKey: String,
        ciphertext: String,
    ): Result<String>
}
