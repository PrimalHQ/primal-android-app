package net.primal.data.account.remote.command.model

import net.primal.domain.nostr.NostrUnsignedEvent

sealed class NostrCommand {
    data class SignEvent(val unsignedEvent: NostrUnsignedEvent) : NostrCommand()
    data object Ping : NostrCommand()
    data class GetPublicKey(val clientPubKey: String) : NostrCommand()
    data class Nip04Encrypt(val clientPubKey: String, val plaintext: String) : NostrCommand()
    data class Nip04Decrypt(val clientPubKey: String, val ciphertext: String) : NostrCommand()
    data class Nip44Encrypt(val clientPubKey: String, val plaintext: String) : NostrCommand()
    data class Nip44Decrypt(val clientPubKey: String, val ciphertext: String) : NostrCommand()
}

/*
 * 1) Treba da oslukujemo relays za odredjene eventove.
 * 2) Kada dobijemo event, treba da ga dekriptujemo - remote (ukoliko nema deps)
 * 3) Na osnovu toga na treba info sta hoce remote app - dobijamo NostrCommand
 * Ovo iznad bi trebalo da ide u remote-u. Ispod repository:
 * 4) Treba da obradimo komandu i da vratimo rezultat (deps na nsec usera) -> repository level
 */
