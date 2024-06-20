package net.primal.android.nostr.utils

data class Naddr(
    val identifier: String,
    val relays: List<String> = emptyList(),
    val userId: String,
    val kind: Int,
)
