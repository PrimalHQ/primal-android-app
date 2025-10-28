package net.primal.android.user.domain

import net.primal.android.user.db.Relay as RelayPO
import net.primal.domain.nostr.cryptography.NostrKeyPair
import net.primal.domain.nostr.cryptography.utils.bech32ToHexOrThrow
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp
import net.primal.domain.nostr.cryptography.utils.hexToNsecHrp

fun Relay.mapToRelayPO(userId: String, kind: RelayKind) =
    RelayPO(
        userId = userId,
        kind = kind,
        url = this.url.cleanWebSocketUrl(),
        read = this.read,
        write = this.write,
    )

fun RelayPO.mapToRelayDO() =
    Relay(
        url = this.url,
        read = this.read,
        write = this.write,
    )

fun String.cleanWebSocketUrl(): String {
    return replace("https://", "wss://", ignoreCase = true)
        .replace("http://", "ws://", ignoreCase = true)
        .let { if (it.endsWith("/")) it.dropLast(1) else it }
}

fun Credential.asKeyPair() =
    NostrKeyPair(
        privateKey = this.nsec?.bech32ToHexOrThrow() ?: throw IllegalArgumentException("nsec was null."),
        pubKey = this.npub.bech32ToHexOrThrow(),
    )

fun NostrKeyPair.asCredential(type: CredentialType) =
    Credential(
        nsec = this.privateKey.hexToNsecHrp(),
        npub = this.pubKey.hexToNpubHrp(),
        type = type,
    )
