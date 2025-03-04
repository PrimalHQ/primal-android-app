package net.primal.android.nostr.utils

data class Nevent(
    val eventId: String,
    val kind: Int? = null,
    val userId: String? = null,
    val relays: List<String> = emptyList(),
)
