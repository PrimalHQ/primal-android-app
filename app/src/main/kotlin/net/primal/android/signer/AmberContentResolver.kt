package net.primal.android.signer

import android.content.ContentResolver
import androidx.core.net.toUri
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp

private const val AMBER_PREFIX = "content://com.greenart7c3.nostrsigner."

/**
 * Decrypts the specified [content] using Amber's `nip04_decrypt` method.
 *
 * NIP-04 is a standard for secure private messages on the Nostr protocol, and this method leverages
 * Amber to perform the decryption. Upon completion, an optional [onResult] callback may be invoked
 * with the decrypted message or `null` if the process fails or is not authorized.
 *
 * @param content The NIP-04 message content to be decrypted.
 * @param participantId The hex identifier for the participant involved in this secure message exchange.
 * @param userNpub The user's Nostr public key (npub), required to authorize and perform the decryption.
 * @param onResult An optional callback invoked with the decrypted message as a [String], or `null` if decryption fails.
 * @return The decrypted message as a [String], or `null` if the decryption could not be performed.
 */
fun ContentResolver.decryptNip04WithAmber(
    content: String,
    participantId: String,
    userNpub: String,
    onResult: ((String?) -> Unit)? = null,
): String? =
    query(
        (AMBER_PREFIX + SignerMethod.NIP04_DECRYPT.method.uppercase()).toUri(),
        arrayOf(content, participantId, userNpub),
        "1",
        null,
        null,
    )?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndex("signature")
            if (index < 0) return null

            val decryptedText = it.getString(index)
            onResult?.invoke(decryptedText)
            return decryptedText
        } else {
            return null
        }
    }

/**
 * Encrypts the specified [content] using Amber's `nip04_encrypt` method.
 *
 * NIP-04 is a standard for secure private messages on the Nostr protocol, and this method leverages
 * Amber to perform the encryption. Upon completion, an optional [onResult] callback may be invoked
 * with the encrypted message, or `null` if the encryption process fails or is not authorized.
 *
 * @param content The NIP-04 message content to be encrypted.
 * @param participantId The hex identifier for the participant involved in this secure message exchange.
 * @param userNpub The user's Nostr public key (npub), required to authorize and perform the encryption.
 * @param onResult An optional callback invoked with the encrypted message as a [String], or `null` if encryption fails.
 * @return The encrypted message as a [String], or `null` if the encryption could not be performed.
 */
fun ContentResolver.encryptNip04WithAmber(
    content: String,
    participantId: String,
    userNpub: String,
    onResult: ((String?) -> Unit)? = null,
): String? =
    query(
        (AMBER_PREFIX + SignerMethod.NIP04_ENCRYPT.method.uppercase()).toUri(),
        arrayOf(content, participantId, userNpub),
        "1",
        null,
        null,
    )?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndex("signature")
            if (index < 0) return null

            val encryptedText = it.getString(index)
            onResult?.invoke(encryptedText)
            return encryptedText
        } else {
            return null
        }
    }

/**
 * Requests Amber to sign the specified [NostrUnsignedEvent] by invoking its `sign_event` method.
 *
 * The signing request can result in one of the following outcomes:
 *  - [AmberSignResult.Signed]: The event was successfully signed and a valid [NostrEvent] is returned.
 *  - [AmberSignResult.Undecided]: The user has not yet made a decision regarding signing this event.
 *  - [AmberSignResult.Rejected]: The user has explicitly declined to sign the event.
 *
 * The outcome is encapsulated in an [AmberSignResult] that represents the exact state of the signing operation.
 *
 * @param event The unsigned Nostr event that is to be signed.
 * @return An [AmberSignResult] indicating whether the event was signed, left undecided, or rejected.
 */
fun ContentResolver.signEventWithAmber(event: NostrUnsignedEvent): AmberSignResult =
    query(
        (AMBER_PREFIX + SignerMethod.SIGN_EVENT.method.uppercase()).toUri(),
        arrayOf(event.encodeToJsonString(), "", event.pubKey.hexToNpubHrp()),
        "1",
        null,
        null,
    )?.use {
        if (it.moveToFirst()) {
            val rejectedIndex = it.getColumnIndex("rejected")
            val eventIndex = it.getColumnIndex("event")

            if (eventIndex < 0 || rejectedIndex >= 0) return AmberSignResult.Rejected
            val nostrEvent = it.getString(eventIndex).decodeFromJsonStringOrNull<NostrEvent>()

            nostrEvent?.let { AmberSignResult.Signed(it) }
        } else {
            AmberSignResult.Undecided
        }
    } ?: AmberSignResult.Undecided

sealed class AmberSignResult {
    data class Signed(val nostrEvent: NostrEvent) : AmberSignResult()
    data object Undecided : AmberSignResult()
    data object Rejected : AmberSignResult()
}
