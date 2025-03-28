package net.primal.domain.model

data class EventRelayHints(
    val eventId: String,
    val relays: List<String> = emptyList(),
)
