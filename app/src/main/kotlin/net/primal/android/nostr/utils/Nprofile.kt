package net.primal.android.nostr.utils

data class Nprofile(
    val pubkey: String,
    val relays: List<String> = emptyList(),
)
