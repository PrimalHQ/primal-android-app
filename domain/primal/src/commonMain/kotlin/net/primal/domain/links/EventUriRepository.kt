package net.primal.domain.links

interface EventUriRepository {
    suspend fun loadEventLinks(noteId: String, types: List<EventUriType>): List<EventLink>
}
