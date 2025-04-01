package net.primal.android.messages.security

import android.content.ContentResolver
import javax.inject.Inject
import net.primal.android.crypto.CryptoUtils
import net.primal.android.crypto.bechToBytesOrThrow
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.nostr.notary.exceptions.MissingPrivateKey
import net.primal.android.signer.decryptNip04WithAmber
import net.primal.android.signer.encryptNip04WithAmber
import net.primal.android.user.credentials.CredentialsStore
import net.primal.domain.nostr.cryptography.MessageCipher
import net.primal.domain.nostr.cryptography.MessageEncryptException

class Nip04MessageCipher @Inject constructor(
    private val credentialsStore: CredentialsStore,
    private val contentResolver: ContentResolver,
) : MessageCipher {

    /**
     * Encrypts the given [content] for the specified [userId] and [participantId].
     *
     * If the user is logged in via an external signer (Amber), encryption is delegated to
     * [ContentResolver.encryptNip04WithAmber]. Otherwise, encryption is performed locally using
     * the user’s private key and the participant's public key. If the encryption process fails,
     * a [MessageEncryptException] is thrown.
     *
     * @param userId The user's ID in hexadecimal format, used to derive the user’s public key or retrieve credentials.
     * @param participantId The participant's ID in hexadecimal format, used to derive the participant’s public key.
     * @param content The plaintext message to be encrypted.
     *
     * @return The encrypted message if successful.
     *
     * @throws IllegalArgumentException If the [userId] is invalid.
     * @throws MissingPrivateKey If no private key is found for the user.
     * @throws MessageEncryptException If the encryption process fails (either from the external signer or locally).
     */
    @Throws(IllegalArgumentException::class, MissingPrivateKey::class, MessageEncryptException::class)
    override fun encryptMessage(
        userId: String,
        participantId: String,
        content: String,
    ): String {
        val npub = userId.hexToNpubHrp()
        return if (credentialsStore.isExternalSignerLogin(npub = npub)) {
            contentResolver.encryptNip04WithAmber(
                content = content,
                participantId = participantId,
                userNpub = npub,
            ) ?: throw MessageEncryptException()
        } else {
            encryptMessageLocally(userId = userId, participantId = participantId, content = content)
        }
    }

    /**
     * Attempts to decrypt the given [content] for the specified [userId] and [participantId].
     *
     * If the user is logged in with an external signer (Amber), decryption is delegated to
     * [ContentResolver.decryptNip04WithAmber]. Otherwise, decryption is performed locally using
     * the user's private key (retrieved via [credentialsStore]) and the participant's public key.
     *
     * In either case, if decryption fails or is not authorized, the original [content] is returned
     * unmodified.
     *
     * @param userId The user's ID in hex form, used to derive the user's public key or retrieve their credentials.
     * @param participantId The participant's ID in hex form,
     *                      used to derive the participant's public key for decryption.
     * @param content The encrypted message to be decrypted.
     *
     * @return The decrypted message, or the original [content] if decryption could not be performed.
     */
    override fun decryptMessage(
        userId: String,
        participantId: String,
        content: String,
    ): String {
        val npub = userId.hexToNpubHrp()

        return if (credentialsStore.isExternalSignerLogin(npub = npub)) {
            contentResolver.decryptNip04WithAmber(
                content = content,
                participantId = participantId,
                userNpub = npub,
            ) ?: content
        } else {
            runCatching {
                CryptoUtils.decrypt(
                    message = content,
                    privateKey = credentialsStore.findOrThrow(npub = npub).nsec?.bechToBytesOrThrow(hrp = "nsec")
                        ?: throw MissingPrivateKey(),
                    pubKey = participantId.hexToNpubHrp().bechToBytesOrThrow(hrp = "npub"),
                )
            }.getOrDefault(content)
        }
    }

    @Throws(IllegalArgumentException::class, MissingPrivateKey::class, MessageEncryptException::class)
    private fun encryptMessageLocally(
        userId: String,
        participantId: String,
        content: String,
    ): String {
        val nsec = credentialsStore.findOrThrow(npub = userId.hexToNpubHrp()).nsec
            ?: throw MissingPrivateKey()

        return try {
            CryptoUtils.encrypt(
                msg = content,
                privateKey = nsec.bechToBytesOrThrow(hrp = "nsec"),
                pubKey = participantId.hexToNpubHrp().bechToBytesOrThrow(hrp = "npub"),
            )
        } catch (_: RuntimeException) {
            throw MessageEncryptException()
        }
    }
}
