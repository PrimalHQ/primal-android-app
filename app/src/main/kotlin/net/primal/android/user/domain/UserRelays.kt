package net.primal.android.user.domain

data class UserRelays(
    val pubkey: String,
    val relays: List<Relay>,
)
