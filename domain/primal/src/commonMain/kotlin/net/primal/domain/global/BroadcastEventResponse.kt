package net.primal.domain.global

data class BroadcastEventResponse(
    val eventId: String,
    val responses: List<List<String>> = emptyList(),
)
