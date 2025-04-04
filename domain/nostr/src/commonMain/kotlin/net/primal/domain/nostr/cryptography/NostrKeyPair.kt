package net.primal.domain.nostr.cryptography

data class NostrKeyPair(
    val privateKey: String,
    val pubKey: String,
)
