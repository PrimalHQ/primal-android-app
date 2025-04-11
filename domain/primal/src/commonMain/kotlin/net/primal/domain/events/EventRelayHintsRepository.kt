package net.primal.domain.events

interface EventRelayHintsRepository {
    suspend fun findRelaysByIds(eventIds: List<String>): List<EventRelayHints>
}
