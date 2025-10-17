package net.primal.data.account.remote.command.model

import net.primal.domain.nostr.NostrUnsignedEvent

sealed class NostrCommand(open val id: String, open val clientPubKey: String) {
    data class Connect(
        override val id: String,
        override val clientPubKey: String,
        val remoteSignerPubkey: String,
        val secret: String,
        val requestedPermissions: List<String>,
    ) : NostrCommand(id = id, clientPubKey = clientPubKey)

    data class SignEvent(
        override val id: String,
        override val clientPubKey: String,
        val unsignedEvent: NostrUnsignedEvent,
    ) : NostrCommand(id = id, clientPubKey = clientPubKey)

    data class Ping(
        override val id: String,
        override val clientPubKey: String,
    ) : NostrCommand(id = id, clientPubKey = clientPubKey)

    data class GetPublicKey(
        override val id: String,
        override val clientPubKey: String,
    ) : NostrCommand(id = id, clientPubKey = clientPubKey)

    data class Nip04Encrypt(
        override val id: String,
        override val clientPubKey: String,
        val thirdPartyPubKey: String,
        val plaintext: String,
    ) : NostrCommand(id = id, clientPubKey = clientPubKey)

    data class Nip04Decrypt(
        override val id: String,
        override val clientPubKey: String,
        val thirdPartyPubKey: String,
        val ciphertext: String,
    ) : NostrCommand(id = id, clientPubKey = clientPubKey)

    data class Nip44Encrypt(
        override val id: String,
        override val clientPubKey: String,
        val thirdPartyPubKey: String,
        val plaintext: String,
    ) : NostrCommand(id = id, clientPubKey = clientPubKey)

    data class Nip44Decrypt(
        override val id: String,
        override val clientPubKey: String,
        val thirdPartyPubKey: String,
        val ciphertext: String,
    ) : NostrCommand(id = id, clientPubKey = clientPubKey)
}

/*
 * 1) Treba da oslukujemo relays za odredjene eventove.
 * 2) Kada dobijemo event, treba da ga dekriptujemo - remote (ukoliko nema deps)
 * 3) Na osnovu toga na treba info sta hoce remote app - dobijamo NostrCommand
 * Ovo iznad bi trebalo da ide u remote-u. Ispod repository:
 * 4) Treba da obradimo komandu i da vratimo rezultat (deps na nsec usera) -> repository level
 */
