package net.primal.domain.events

data class EventRelayHints(
    val eventId: String,
    val relays: List<String> = emptyList(),
)
