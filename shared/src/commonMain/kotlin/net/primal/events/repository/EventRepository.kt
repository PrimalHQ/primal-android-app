package net.primal.events.repository


import kotlinx.coroutines.flow.Flow
import net.primal.events.db.Event

interface EventRepository {
    
    suspend fun insertRandomEvent()

    suspend fun insertEvent(event: Event)

    suspend fun getAllEvents(): List<Event>

    suspend fun getAllEvents2(): List<Event>

    fun observeAllEvents(): Flow<List<Event>>
}
