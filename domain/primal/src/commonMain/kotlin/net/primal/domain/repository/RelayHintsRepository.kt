package net.primal.domain.repository

import net.primal.domain.model.EventRelayHints

interface RelayHintsRepository {
    suspend fun findRelaysByIds(eventIds: List<String>): List<EventRelayHints>
}
