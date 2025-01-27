package net.primal.android.nostr.utils

data class Nevent(
    val kind: Int?,
    val userId: String,
    val eventId: String,
    val relays: List<String> = emptyList(),
)
