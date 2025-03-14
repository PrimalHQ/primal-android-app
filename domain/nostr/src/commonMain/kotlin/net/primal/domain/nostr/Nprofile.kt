package net.primal.domain.nostr

data class Nprofile(
    val pubkey: String,
    val relays: List<String> = emptyList(),
)
