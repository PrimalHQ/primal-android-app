package net.primal.nostr

data class Nprofile(
    val pubkey: String,
    val relays: List<String> = emptyList(),
)
