package net.primal.domain.nostr

data class Nevent(
    val eventId: String,
    val kind: Int? = null,
    val userId: String? = null,
    val relays: List<String> = emptyList(),
)
