package net.primal.android.signer.client

import android.content.ContentResolver
import android.database.Cursor
import androidx.core.net.toUri
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.account.signer.local.model.SignerMethod
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp

private fun signerUri(signerPackageName: String, method: SignerMethod) =
    "content://$signerPackageName.${method.method.uppercase()}".toUri()

/**
 * Reads the value of the given [column] from the cursor, falling back to Amber's legacy
 * `signature` column when the NIP-55 `result` column is not present. The spec says signers
 * answer with a `result` column; Amber additionally duplicates it into `signature`, which is
 * what older clients used to read.
 */
private fun Cursor.getStringOrLegacySignature(column: String): String? {
    val index = getColumnIndex(column).takeIf { it >= 0 }
        ?: getColumnIndex("signature").takeIf { it >= 0 }
        ?: return null
    return getString(index)
}

/**
 * Decrypts the specified [content] using the external signer's `nip04_decrypt` method.
 *
 * NIP-04 is a standard for secure private messages on the Nostr protocol, and this method leverages
 * a NIP-55 external signer (e.g. Amber, Cambium) to perform the decryption. Upon completion, an
 * optional [onResult] callback may be invoked with the decrypted message or `null` if the process
 * fails or is not authorized.
 *
 * @param content The NIP-04 message content to be decrypted.
 * @param participantId The hex identifier for the participant involved in this secure message exchange.
 * @param userNpub The user's Nostr public key (npub), required to authorize and perform the decryption.
 * @param signerPackageName The package name of the signer app the user logged in with.
 * @param onResult An optional callback invoked with the decrypted message as a [String], or `null` if decryption fails.
 * @return The decrypted message as a [String], or `null` if the decryption could not be performed.
 */
fun ContentResolver.decryptNip04WithSigner(
    content: String,
    participantId: String,
    userNpub: String,
    signerPackageName: String = AMBER_PACKAGE_NAME,
    onResult: ((String?) -> Unit)? = null,
): String? =
    query(
        signerUri(signerPackageName, SignerMethod.NIP04_DECRYPT),
        arrayOf(content, participantId, userNpub),
        "1",
        null,
        null,
    )?.use {
        if (it.moveToFirst()) {
            val decryptedText = it.getStringOrLegacySignature(column = "result") ?: return null
            onResult?.invoke(decryptedText)
            return decryptedText
        } else {
            return null
        }
    }

/**
 * Encrypts the specified [content] using the external signer's `nip04_encrypt` method.
 *
 * NIP-04 is a standard for secure private messages on the Nostr protocol, and this method leverages
 * a NIP-55 external signer (e.g. Amber, Cambium) to perform the encryption. Upon completion, an
 * optional [onResult] callback may be invoked with the encrypted message, or `null` if the
 * encryption process fails or is not authorized.
 *
 * @param content The NIP-04 message content to be encrypted.
 * @param participantId The hex identifier for the participant involved in this secure message exchange.
 * @param userNpub The user's Nostr public key (npub), required to authorize and perform the encryption.
 * @param signerPackageName The package name of the signer app the user logged in with.
 * @param onResult An optional callback invoked with the encrypted message as a [String], or `null` if encryption fails.
 * @return The encrypted message as a [String], or `null` if the encryption could not be performed.
 */
fun ContentResolver.encryptNip04WithSigner(
    content: String,
    participantId: String,
    userNpub: String,
    signerPackageName: String = AMBER_PACKAGE_NAME,
    onResult: ((String?) -> Unit)? = null,
): String? =
    query(
        signerUri(signerPackageName, SignerMethod.NIP04_ENCRYPT),
        arrayOf(content, participantId, userNpub),
        "1",
        null,
        null,
    )?.use {
        if (it.moveToFirst()) {
            val encryptedText = it.getStringOrLegacySignature(column = "result") ?: return null
            onResult?.invoke(encryptedText)
            return encryptedText
        } else {
            return null
        }
    }

/**
 * Requests the external signer to sign the specified [NostrUnsignedEvent] by invoking its
 * `sign_event` method.
 *
 * The signing request can result in one of the following outcomes:
 *  - [ExternalSignResult.Signed]: The event was successfully signed and a valid [NostrEvent] is returned.
 *  - [ExternalSignResult.Undecided]: The user has not yet made a decision regarding signing this event.
 *  - [ExternalSignResult.Rejected]: The user has explicitly declined to sign the event.
 *
 * The outcome is encapsulated in an [ExternalSignResult] that represents the exact state of the signing operation.
 *
 * @param event The unsigned Nostr event that is to be signed.
 * @param signerPackageName The package name of the signer app the user logged in with.
 * @return An [ExternalSignResult] indicating whether the event was signed, left undecided, or rejected.
 */
fun ContentResolver.signEventWithSigner(
    event: NostrUnsignedEvent,
    signerPackageName: String = AMBER_PACKAGE_NAME,
): ExternalSignResult =
    query(
        signerUri(signerPackageName, SignerMethod.SIGN_EVENT),
        arrayOf(event.encodeToJsonString(), "", event.pubKey.hexToNpubHrp()),
        "1",
        null,
        null,
    )?.use {
        if (it.moveToFirst()) {
            val rejectedIndex = it.getColumnIndex("rejected")
            val eventIndex = it.getColumnIndex("event")

            if (eventIndex < 0 || rejectedIndex >= 0) return ExternalSignResult.Rejected
            val nostrEvent = it.getString(eventIndex).decodeFromJsonStringOrNull<NostrEvent>()

            nostrEvent?.let { ExternalSignResult.Signed(it) }
        } else {
            ExternalSignResult.Undecided
        }
    } ?: ExternalSignResult.Undecided

sealed class ExternalSignResult {
    data class Signed(val nostrEvent: NostrEvent) : ExternalSignResult()
    data object Undecided : ExternalSignResult()
    data object Rejected : ExternalSignResult()
}
