package net.primal.domain.repository

import net.primal.domain.EventLink
import net.primal.domain.EventUriType

interface EventUriRepository {
    suspend fun loadEventLinks(noteId: String, types: List<EventUriType>): List<EventLink>
}
