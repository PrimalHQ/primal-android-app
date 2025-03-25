package net.primal.android.signer

import android.content.ContentResolver
import androidx.core.net.toUri
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.nostr.notary.NostrUnsignedEvent
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.nostr.NostrEvent

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
 * Requests Amber to sign the given [NostrUnsignedEvent] via its `sign_event` method.
 *
 * If the user has already granted permission for this event kind, a signed [NostrEvent] is returned.
 * Otherwise, the method returns `null`. An optional [onResult] callback may also be supplied to handle
 * the signing outcome—this callback will receive either the signed event or `null` if signing was not authorized.
 *
 * @param event The unsigned Nostr event to be signed.
 * @param onResult An optional callback invoked with the signed [NostrEvent],
 *                 or `null` if signing was declined or failed.
 * @return The signed [NostrEvent], or `null` if the user has not approved signing for this event kind.
 */
fun ContentResolver.signEventWithAmber(
    event: NostrUnsignedEvent,
    onResult: ((nostrEvent: NostrEvent?) -> Unit)? = null,
): NostrEvent? =
    query(
        (AMBER_PREFIX + SignerMethod.SIGN_EVENT.method.uppercase()).toUri(),
        arrayOf(event.encodeToJsonString(), "", event.pubKey.hexToNpubHrp()),
        "1",
        null,
        null,
    )?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndex("event")
            if (index < 0) return null
            val nostrEvent = it.getString(index).decodeFromJsonStringOrNull<NostrEvent>()

            onResult?.invoke(nostrEvent)
            return nostrEvent
        } else {
            return null
        }
    }
